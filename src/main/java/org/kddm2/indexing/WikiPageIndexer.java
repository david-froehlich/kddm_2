package org.kddm2.indexing;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.kddm2.Settings;
import org.kddm2.lucene.WikiField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;



public class WikiPageIndexer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(WikiPageIndexer.class);
    // how often you want a log message (in pages)
    private static final int PRINT_INTERVAL = 500;
    private static AtomicInteger indexedPages = new AtomicInteger(0);


    private final Set<String> vocabulary;
    private final Map<String, Set<String>> documentSynonyms;
    private final Map<String, Set<String>> redirects;
    private final int maxShingleSize;
    private IndexWriter indexWriter;
    private BlockingQueue<IndexingTask> indexingTasks;


    public WikiPageIndexer(BlockingQueue<IndexingTask> indexingTasks, IndexWriter indexWriter, Set<String> vocabulary, Map<String, Set<String>> documentSynonyms, Map<String, Set<String>> redirects, int maxShingleSize) {
        this.indexingTasks = indexingTasks;
        this.indexWriter = indexWriter;
        this.vocabulary = vocabulary;
        this.documentSynonyms = documentSynonyms;
        this.redirects = redirects;
        this.maxShingleSize = maxShingleSize;
        indexedPages.set(0);
    }

    private void indexPage(WikiPage page) throws IOException {
        Document doc = new Document();
        doc.add(new WikiField(Settings.DOCUMENT_ID_FIELD_NAME, page.getTitle()));
        addSynonymForPage(page.getTitle(), page.getTitle().trim().toLowerCase());

        Map<String, Integer> occurrences = WikiUtils.parseUnlinkedOccurrences(page.getText(), vocabulary, maxShingleSize);
        for (Map.Entry<String, Integer> entry : occurrences.entrySet()) {
            String currentTerm = entry.getKey();
            Integer count = entry.getValue();
            for (int i = 0; i < count; i++) {
                doc.add(new WikiField(Settings.TERM_OCCURENCE_FIELD_NAME, currentTerm));
            }
        }
        //TODO: use tokenizer here!
        Map<WikiLink, Integer> links = WikiUtils.parseLinkedOccurrences(page.getText());
        for (Map.Entry<WikiLink, Integer> entry : links.entrySet()) {
            WikiLink wikiLink = entry.getKey();
            String linkTextLowercase = wikiLink.getLinkText().toLowerCase();
            Integer count = entry.getValue();
            for (int i = 0; i < count; i++) {
                doc.add(new WikiField(Settings.TERM_LINKING_FIELD_NAME, linkTextLowercase));
            }
            // if synonym; case sensitivity in pageId intentional here!
            String pageId = wikiLink.getPageId();
            if (!pageId.equalsIgnoreCase(linkTextLowercase)) {
                addSynonymForPage(pageId, linkTextLowercase);
            }
        }

        indexWriter.addDocument(doc);
    }

    @SuppressWarnings("Duplicates")
    private void addRedirectForPage(String pageId, String redirectPageId) {
        synchronized (redirects) {
            Set<String> otherDocRedirects = redirects.getOrDefault(pageId, null);
            // create hash set on demand
            if (otherDocRedirects == null) {
                otherDocRedirects = new HashSet<>();
                redirects.put(pageId, otherDocRedirects);
            }
            otherDocRedirects.add(redirectPageId);
        }
    }

    @SuppressWarnings("Duplicates")
    private void addSynonymForPage(String pageId, String synonym) {
        synchronized (documentSynonyms) {
            Set<String> otherDocSynonyms = documentSynonyms.getOrDefault(pageId, null);
            // create hash set on demand
            if (otherDocSynonyms == null) {
                otherDocSynonyms = new HashSet<>();
                documentSynonyms.put(pageId, otherDocSynonyms);
            }
            otherDocSynonyms.add(synonym);
        }
    }

    private void handlePage(WikiPage page) throws IOException {
        if (page.isRedirectPage()) {
            // pageId should be case sensitive, the synonym insensitive
            String redirectTarget = page.getRedirectTarget();
            String synonym = page.getTitle().trim().toLowerCase();
            this.addSynonymForPage(redirectTarget, synonym);
            this.addRedirectForPage(redirectTarget, synonym);
        } else {
            this.indexPage(page);
        }
    }

    /**
     * eats baby souls
     */
    private void consume() throws InterruptedException, IOException {
        while (true) {
            IndexingTask task = indexingTasks.take();
            if (task.isEndOfStream()) {
                LOG.info("Consumer has eaten all souls!");
                return;
            }
            handlePage(task.getWikiPage());
            int i = indexedPages.incrementAndGet();
            if (i % PRINT_INTERVAL == 0) {
                LOG.info("indexed " + i + " pages");
            }
        }
    }

    @Override
    public void run() {
        try {
            consume();
        } catch (InterruptedException | IOException e) {
            LOG.error("Error while indexing, stopping thread", e);
        }
    }
}
