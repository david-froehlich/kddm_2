import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class WikiPageIndexer implements Runnable{
    // set to -1 for infinite
    // also won't work for multiple indexer-threads cause i'm lazy
    private static final int MAX_INDEXED_PAGES = 100;
    // how often you want a log-message
    private static final int PRINT_INTERVAL = MAX_INDEXED_PAGES / 10;
    private int indexedPages = 0;

    private IndexWriter indexWriter;
    private Queue<WikiPage> unindexedPages;
    private AtomicBoolean producerDone;
    final Condition notFull, notEmpty;
    final Lock lock;


    public WikiPageIndexer(Queue<WikiPage> unindexedPages, AtomicBoolean producerDone, IndexWriter indexWriter, Condition notFull, Condition notEmpty, Lock lock) {
        this.unindexedPages = unindexedPages;
        this.producerDone = producerDone;
        this.indexWriter = indexWriter;
        this.notFull = notFull;
        this.notEmpty = notEmpty;
        this.lock = lock;
    }

    private void indexPage(WikiPage page) throws IOException {
        Document doc = new Document();
        for(String currentTerm : page.getOccuringTerms()) {
            //TODO check if reusing the same fieldtype increases performance
            doc.add(new StringField(WikiIndexingController.TERM_OCCURENCE_FIELD_NAME, currentTerm, Field.Store.YES));
        }

        for(String currentTerm : page.getLinkedTerms()) {
            doc.add(new StringField(WikiIndexingController.TERM_LINKING_FIELD_NAME, currentTerm, Field.Store.YES));
        }

        indexWriter.addDocument(doc);
    }

    /**
     * eats baby souls
     */
    private void consume() throws InterruptedException, IOException {

        while(indexedPages++ < MAX_INDEXED_PAGES || MAX_INDEXED_PAGES == -1) {
            lock.lock();
            WikiPage nextPage = unindexedPages.poll();
            while (nextPage == null) {
                if(producerDone.get()) {
                    lock.unlock();
                    return;
                }
                notEmpty.await();
                nextPage = unindexedPages.poll();
            }
            lock.unlock();
            this.indexPage(nextPage);
            if (indexedPages % PRINT_INTERVAL == 0) {
                System.out.println("indexed " + indexedPages + " pages");
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
