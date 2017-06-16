package org.kddm2.indexing;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.kddm2.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class WikiPageIndexer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WikiPageIndexer.class);
    private static final FieldType INDEX_FIELD_TYPE = new FieldType();

    static {
        INDEX_FIELD_TYPE.setStored(true);
        INDEX_FIELD_TYPE.setTokenized(false);
        INDEX_FIELD_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
    }

    // set to -1 for infinite
    // also won't work for multiple indexer-threads cause i'm lazy
    private static final int MAX_INDEXED_PAGES = -1;
    // how often you want a log-message
    private static final int PRINT_INTERVAL = 500;
    private static AtomicInteger indexedPages = new AtomicInteger(0);

    private IndexWriter indexWriter;
    private final Set<String> vocabulary;
    private BlockingQueue<IndexingTask> indexingTasks;
    private Map<String, Set<String>> documentSynonyms = new HashMap<>();


    public WikiPageIndexer(BlockingQueue<IndexingTask> indexingTasks, IndexWriter indexWriter, Set<String> vocabulary) {
        this.indexingTasks = indexingTasks;
        this.indexWriter = indexWriter;
        this.vocabulary = vocabulary;
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

        Map<WikiLink, Integer> links = WikiUtils.parseLinkedOccurrences(page.getText(), vocabulary);
        for (Map.Entry<WikiLink, Integer> entry : links.entrySet()) {
            WikiLink wikiLink = entry.getKey();
            Integer count = entry.getValue();
            for (int i = 0; i < count; i++) {
                doc.add(new StoredField(Settings.TERM_LINKING_FIELD_NAME, wikiLink.getLinkText(),
                        WikiPageIndexer.INDEX_FIELD_TYPE));
            }
            // if synonym
            if (!wikiLink.getPageId().equalsIgnoreCase(wikiLink.getLinkText())) {
                Set<String> otherDocSynonyms = documentSynonyms.getOrDefault(wikiLink.getPageId(), null);
                // create hash set on demand
                if (otherDocSynonyms == null) {
                    otherDocSynonyms = new HashSet<>();
                    documentSynonyms.put(wikiLink.getPageId(), otherDocSynonyms);
                }
                // add new link text, convert to lowercase to hopefully save some memory
                otherDocSynonyms.add(wikiLink.getLinkText().toLowerCase());
            }
        }

        indexWriter.addDocument(doc);
    }

    /**
     * Writes all collected synonyms to the lucene index.
     * TODO: batch this if out of memory
     * or use mapdb set that caches to filesystem
     */
    private void writeSynonyms() {
        try {
            DirectoryReader directoryReader = DirectoryReader.open(indexWriter);
            IndexSearcher searcher = new IndexSearcher(directoryReader);

            for (Map.Entry<String, Set<String>> entry : documentSynonyms.entrySet()) {
                String docId = entry.getKey();
                Term docTerm = new Term(Settings.DOCUMENT_ID_FIELD_NAME, docId);

                TopDocs topDocs = searcher.search(new TermQuery(docTerm), 1);
                if (topDocs.scoreDocs.length == 0) {
                    continue;
                }
                Document doc = searcher.doc(topDocs.scoreDocs[0].doc);
                for (String synonym : entry.getValue()) {
                    doc.add(new StoredField(Settings.SYNONYMS_FIELD_NAME, synonym, WikiPageIndexer.INDEX_FIELD_TYPE));
                }
                indexWriter.updateDocument(docTerm, doc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * eats baby souls
     */
    private void consume() throws InterruptedException, IOException {
        int i = 0;
        while ((i = WikiPageIndexer.indexedPages.incrementAndGet()) < MAX_INDEXED_PAGES || MAX_INDEXED_PAGES == -1) {
            IndexingTask task = indexingTasks.take();
            if (task.isEndOfStream()) {
                logger.info("Consumer has eaten all souls!");
                logger.info("Writing synonyms");
                writeSynonyms();
                logger.info("Writing synonyms finished");
                return;
            }
            this.indexPage(task.getWikiPage());
            if (i % PRINT_INTERVAL == 0) {
                logger.info("indexed " + i + " pages");
            }
        }
    }

    @Override
    public void run() {
        try {
            this.consume();
            indexWriter.close();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            //TODO think about this
        }
    }
}
