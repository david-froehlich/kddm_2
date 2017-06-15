package org.kddm2.indexing;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kddm2.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IndexingService {
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

    private Thread producer;
    private List<Thread> consumers;
    private IndexWriter indexWriter;
    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicInteger numProcessedPages = new AtomicInteger();
    private BlockingQueue<IndexingTask> indexingTasks = new ArrayBlockingQueue<>(Settings.QUEUE_LENGTH);
    private Path indexDirectory;
    private Set<String> vocabulary;
    private static final Logger logger = LoggerFactory.getLogger(IndexingService.class);


    @Autowired
    public IndexingService(Path indexDirectory, Set<String> vocabulary) {
        this.indexDirectory = indexDirectory;
        this.vocabulary = vocabulary;
    }


    public boolean isRunning() {
        return running.get();
    }

    public IndexingStatus getStatus() {
        return new IndexingStatus(numProcessedPages.get(), indexingTasks.size(), isRunning());
    }

    @Async
    public void start() {
        running.set(true);
        try {
            numProcessedPages.set(0);
            createIndexDirectory(indexDirectory);
            long start = System.currentTimeMillis();

            startThreads();
            producer.join();
            for (Thread thread : consumers) {
                thread.join();
            }

            logger.info("Indexing took " + (System.currentTimeMillis() - start) / 1000 + " seconds");
        } catch (InterruptedException | XMLStreamException | IOException e) {
            logger.error("Exception while indexing:", e);
        }
        running.set(false);
    }


    private void startThreads() throws IOException, XMLStreamException {
        indexingTasks.clear();
        InputStream wikiInputStream = getClass().getClassLoader().getResourceAsStream(Settings.XML_FILE_PATH);

        producer = new Thread(new WikiPageProducer(indexingTasks, vocabulary, wikiInputStream, numProcessedPages));
        producer.start();
        consumers = new ArrayList<>();
        for (int i = 0; i < Settings.CONSUMER_COUNT; i++) {
            consumers.add(new Thread(new WikiPageIndexer(indexingTasks, indexWriter)));
            consumers.get(consumers.size() - 1).start();
        }
    }

    private void createIndexDirectory(Path directoryPath) throws IOException {
        File f = directoryPath.toFile();
        if (f == null || !f.isDirectory()) {
            Files.createDirectories(directoryPath);
        }
        //noinspection ResultOfMethodCallIgnored
        Files.walk(directoryPath).map(Path::toFile).filter(File::isFile).forEach(File::delete);

        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = FSDirectory.open(directoryPath);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        indexWriter = new IndexWriter(directory, config);
    }


}
