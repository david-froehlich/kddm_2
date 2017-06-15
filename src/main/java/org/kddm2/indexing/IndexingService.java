package org.kddm2.indexing;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kddm2.Settings;
import org.kddm2.lucene.IndexingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class IndexingService {

    private Thread producer;
    private List<Thread> consumers;
    private IndexWriter indexWriter;
    private AtomicBoolean running = new AtomicBoolean(false);
    private Path indexDirectory;
    private static final Logger logger = LoggerFactory.getLogger(IndexingService.class);


    public IndexingService(Path indexDirectory) {
        this.indexDirectory = indexDirectory;
    }

    public Set<String> readVocabulary() {
        try {
            return IndexingUtils.readDictionary(getClass().getClassLoader().getResource(Settings.VOCABULARY_PATH).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isRunning() {
        return running.get();
    }

    @Async
    public void start() {
        running.set(true);
        try {
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
        BlockingQueue<IndexingTask> indexingTasks = new ArrayBlockingQueue<>(Settings.QUEUE_LENGTH);
        InputStream wikiInputStream = getClass().getClassLoader().getResourceAsStream(Settings.XML_FILE_PATH);

        producer = new Thread(new WikiPageProducer(indexingTasks, readVocabulary(), wikiInputStream));
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
