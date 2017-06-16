package org.kddm2.indexing;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.kddm2.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IndexingService {
    private static final Logger LOG = LoggerFactory.getLogger(IndexingService.class);
    private Thread producer;
    private List<Thread> consumers;
    private IndexWriter indexWriter;
    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicInteger numProcessedPages = new AtomicInteger();
    private BlockingQueue<IndexingTask> indexingTasks = new ArrayBlockingQueue<>(Settings.QUEUE_LENGTH);
    private Directory indexDirectory;
    private Set<String> vocabulary;

    private Resource wikiXmlFile;

    @Autowired
    public IndexingService(Directory indexDirectory, Set<String> vocabulary, @Value("${wiki_xml_file}")
            Resource wikiXmlFile) {
        this.indexDirectory = indexDirectory;
        this.vocabulary = vocabulary;
        this.wikiXmlFile = wikiXmlFile;
    }

    public boolean isRunning() {
        return running.get();
    }

    public IndexingStatus getStatus() {
        return new IndexingStatus(numProcessedPages.get(), indexingTasks.size(), isRunning());
    }

    @Async
    public void start() throws InvalidWikiFileException {
        running.set(true);
        try {
            numProcessedPages.set(0);
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            indexWriter = new IndexWriter(indexDirectory, config);
            indexWriter.deleteAll();

            long start = System.currentTimeMillis();

            startThreads();
            producer.join();
            for (Thread thread : consumers) {
                thread.join();
            }
            LOG.info("Indexing took " + (System.currentTimeMillis() - start) / 1000 + " seconds");
        } catch (InterruptedException | IOException e) {
            LOG.error("Exception while indexing:", e);
        }
        running.set(false);
    }

    private void startThreads() throws InvalidWikiFileException {
        indexingTasks.clear();
        InputStream wikiInputStream;

        try {
            wikiInputStream = wikiXmlFile.getInputStream();
        } catch (IOException e) {
            throw new InvalidWikiFileException("Could not load Wiki XML file");
        }

        try {
            producer = new Thread(new WikiPageProducer(indexingTasks, vocabulary, wikiInputStream, numProcessedPages));
        } catch (IOException | XMLStreamException e) {
            throw new InvalidWikiFileException("Could not load Wiki XML file", e);
        }
        producer.start();
        consumers = new ArrayList<>();
        for (int i = 0; i < Settings.CONSUMER_COUNT; i++) {
            consumers.add(new Thread(new WikiPageIndexer(indexingTasks, indexWriter)));
            consumers.get(consumers.size() - 1).start();
        }
    }

    public class IndexingStatus {
        public final int numProcessedPages;
        public final int numPagesInQueue;
        public final boolean isRunning;

        public IndexingStatus(int numProcessedPages, int numPagesInQueue, boolean isRunning) {
            this.numProcessedPages = numProcessedPages;
            this.numPagesInQueue = numPagesInQueue;
            this.isRunning = isRunning;
        }
    }
}
