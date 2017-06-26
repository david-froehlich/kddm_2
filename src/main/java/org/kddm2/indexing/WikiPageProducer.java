package org.kddm2.indexing;

import org.kddm2.indexing.xml.WikiXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class WikiPageProducer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WikiPageProducer.class);
    private BlockingQueue<IndexingTask> indexingTasks;
    private AtomicInteger numProcessedPages;
    private WikiXmlReader reader;
    private final int numConsumers;


    public WikiPageProducer(BlockingQueue<IndexingTask> unindexedPages,
                            InputStream inputStream, AtomicInteger numProcessedPages, int indexingConsumerCount) throws IOException, XMLStreamException {
        this.indexingTasks = unindexedPages;
        this.numProcessedPages = numProcessedPages;
        this.reader = new WikiXmlReader(inputStream);
        this.numConsumers = indexingConsumerCount;
    }

    private void produce() throws InterruptedException, IOException, XMLStreamException {
        while (true) {
            WikiPage nextPage = this.reader.getNextPage();

            if (nextPage == null) {
                int i = numConsumers;
                while (i-- > 0) {
                    this.indexingTasks.put(new IndexingTask(null, true));
                }
                logger.info("producer done");
                return;
            }
            numProcessedPages.incrementAndGet();
            this.indexingTasks.put(new IndexingTask(nextPage, false));
        }
    }

    @Override
    public void run() {
        try {
            this.produce();
        } catch (InterruptedException | XMLStreamException | IOException e) {
            e.printStackTrace();
        }
    }
}
