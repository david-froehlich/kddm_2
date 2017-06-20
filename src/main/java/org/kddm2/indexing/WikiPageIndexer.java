package org.kddm2.indexing;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.kddm2.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class WikiPageIndexer implements Runnable {
    public static final FieldType INDEX_FIELD_TYPE = new FieldType();
    private static final Logger LOG = LoggerFactory.getLogger(WikiPageIndexer.class);
    // set to -1 for infinite
    // also won't work for multiple indexer-threads cause i'm lazy
    private static final int MAX_INDEXED_PAGES = -1;
    // how often you want a log-message
    private static final int PRINT_INTERVAL = 500;
    private static AtomicInteger indexedPages = new AtomicInteger(0);

    static {
        INDEX_FIELD_TYPE.setStored(true);
        INDEX_FIELD_TYPE.setTokenized(false);
        INDEX_FIELD_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
    }

    private final Set<String> vocabulary;
    private final Map<String, Set<String>> documentSynonyms;
    private IndexWriter indexWriter;
    private BlockingQueue<IndexingTask> indexingTasks;


    public WikiPageIndexer(BlockingQueue<IndexingTask> indexingTasks, IndexWriter indexWriter, Set<String> vocabulary, Map<String, Set<String>> documentSynonyms) {
        this.indexingTasks = indexingTasks;
        this.indexWriter = indexWriter;
        this.vocabulary = vocabulary;
        this.documentSynonyms = documentSynonyms;
        indexedPages.set(0);
    }

    private void indexPage(WikiPage page) throws IOException {
        Document doc = new Document();
        doc.add(new StoredField(Settings.DOCUMENT_ID_FIELD_NAME, page.getTitle(), WikiPageIndexer.INDEX_FIELD_TYPE));
        doc.add(new StoredField(Settings.SYNONYMS_FIELD_NAME, page.getTitle(), WikiPageIndexer.INDEX_FIELD_TYPE));

        Map<String, Integer> occurrences = WikiUtils.parseUnlinkedOccurrences(page.getText(), vocabulary);
        for (Map.Entry<String, Integer> entry : occurrences.entrySet()) {
            String currentTerm = entry.getKey();
            Integer count = entry.getValue();
            for (int i = 0; i < count; i++) {
                doc.add(new StoredField(Settings.TERM_OCCURENCE_FIELD_NAME, currentTerm,
                        WikiPageIndexer.INDEX_FIELD_TYPE));
            }
        }

        //TODO: use tokenizer here!
        Map<WikiLink, Integer> links = WikiUtils.parseLinkedOccurrences(page.getText(), vocabulary);
        for (Map.Entry<WikiLink, Integer> entry : links.entrySet()) {
            WikiLink wikiLink = entry.getKey();
            Integer count = entry.getValue();
            for (int i = 0; i < count; i++) {
                doc.add(new StoredField(Settings.TERM_LINKING_FIELD_NAME, wikiLink.getLinkText(),
                        WikiPageIndexer.INDEX_FIELD_TYPE));
            }
            // if synonym
            String pageId = wikiLink.getPageId();
            if (!pageId.equalsIgnoreCase(wikiLink.getLinkText())) {
                addSynonymForPage(pageId, wikiLink.getLinkText().toLowerCase());
            }
        }

        indexWriter.addDocument(doc);
    }

    private void addSynonymForPage(String pageId, String synonym) {
        synchronized (documentSynonyms) {
            Set<String> otherDocSynonyms = documentSynonyms.getOrDefault(pageId, null);
            // create hash set on demand
            if (otherDocSynonyms == null) {
                otherDocSynonyms = new HashSet<>();
                documentSynonyms.put(pageId, otherDocSynonyms);
            }
            // add new link text, convert to lowercase to hopefully save some memory

            otherDocSynonyms.add(synonym);
        }
    }

    private void handlePage(WikiPage page) throws IOException {
        if (page.isRedirectPage()) {
            this.addSynonymForPage(page.getRedirectTarget(), page.getTitle());
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
