package org.kddm2.indexing;

import org.kddm2.indexing.xml.WikiXmlReader;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class WikiPageProducer implements Runnable {
    private BlockingQueue<IndexingTask> indexingTasks;
    private Set<String> vocabulary;
    private WikiXmlReader reader;

    public WikiPageProducer(BlockingQueue<IndexingTask> unindexedPages, Set<String> vocabulary,
                            InputStream xmlFileInputStream) throws IOException, XMLStreamException {
        this.indexingTasks = unindexedPages;
        this.vocabulary = vocabulary;
        this.reader = new WikiXmlReader(xmlFileInputStream, vocabulary);
    }


    private void produce() throws InterruptedException, IOException, XMLStreamException {
        while (true) {
            WikiPage nextPage = this.reader.getNextPage();

            if (nextPage == null) {
                int i = IndexingController.CONSUMER_COUNT;
                while (i-- > 0) {
                    this.indexingTasks.put(new IndexingTask(null, true));
                }
                System.out.println("producer done");
                return;
            }
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
