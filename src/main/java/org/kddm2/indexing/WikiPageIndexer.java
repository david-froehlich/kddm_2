package org.kddm2.indexing;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexWriter;
import org.kddm2.Settings;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class WikiPageIndexer implements Runnable {
    // set to -1 for infinite
    // also won't work for multiple indexer-threads cause i'm lazy
    private static final int MAX_INDEXED_PAGES = -1;
    // how often you want a log-message
    private static final int PRINT_INTERVAL = 500;
    private static AtomicInteger indexedPages = new AtomicInteger(0);

    private IndexWriter indexWriter;
    private BlockingQueue<IndexingTask> indexingTasks;

    public WikiPageIndexer(BlockingQueue<IndexingTask> indexingTasks, IndexWriter indexWriter) {
        this.indexingTasks = indexingTasks;
        this.indexWriter = indexWriter;
    }

    private void indexPage(WikiPage page) throws IOException {
        Document doc = new Document();
        for (Map.Entry<String, Integer> entry : page.getOccuringTerms().entrySet()) {
            String currentTerm = entry.getKey();
            Integer count = entry.getValue();
            for (int i = 0; i < count; i++) {
                doc.add(new StoredField(Settings.TERM_OCCURENCE_FIELD_NAME, currentTerm,
                        IndexingController.INDEX_FIELD_TYPE));
//                doc.add(new StringField(org.kddm2.indexing.IndexingController.TERM_OCCURENCE_FIELD_NAME, currentTerm,
//                        Field.Store.YES));
            }
        }

        for (Map.Entry<String, Integer> entry : page.getLinkedTerms().entrySet()) {
            String currentTerm = entry.getKey();
            Integer count = entry.getValue();
            for (int i = 0; i < count; i++) {
                doc.add(new StoredField(Settings.TERM_LINKING_FIELD_NAME, currentTerm,
                        IndexingController.INDEX_FIELD_TYPE));
            }
        }

        indexWriter.addDocument(doc);
    }

    /**
     * eats baby souls
     */
    private void consume() throws InterruptedException, IOException {

        int i = 0;
        while ((i = WikiPageIndexer.indexedPages.incrementAndGet()) < MAX_INDEXED_PAGES || MAX_INDEXED_PAGES == -1) {
            IndexingTask task = indexingTasks.take();
            if (task.isEndOfStream()) {
                System.out.println("Consumer has eaten all souls!");
                return;
            }
            this.indexPage(task.getWikiPage());
            if (i % PRINT_INTERVAL == 0) {
                System.out.println("indexed " + i + " pages");
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
