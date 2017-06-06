import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class WikiIndexingController {
    final static int QUEUE_LENGTH = 100;

    //---LUCENE CONSTANTS---
    //name of lucene field that stores the term-occurence (the term was used in the article, but not necessarily linked)
    final static String TERM_OCCURENCE_FIELD_NAME = "term_occurence";
    //name of lucene field that stores linked terms
    final static String TERM_LINKING_FIELD_NAME = "term_linking";
    private final static String DIRECTORY_PATH = "/tmp/lucene_dir";

    private final static String VOCABULARY_PATH = "vocabulary.txt";

    private final static String XML_FILE_PATH = "test-pages.xml.bz2";

    private Queue<WikiPage> unindexedPages;
    private AtomicBoolean producerDone;

    final Lock lock = new ReentrantLock();
    final Condition notFull  = lock.newCondition();
    final Condition notEmpty = lock.newCondition();

    private Thread producer, consumer;
    private IndexWriter indexWriter;

    private Set<String> readVocabulary() {
        Set<String> vocabulary = new HashSet<>();

        try (Stream<String> stream = Files.lines(
                Paths.get(getClass().getClassLoader().getResource(VOCABULARY_PATH).toURI()))) {
            stream.forEach(s -> vocabulary.add(s.toLowerCase()));
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return vocabulary;
    }

    private void startThreads() throws IOException, XMLStreamException {
        producerDone = new AtomicBoolean(false);
        this.unindexedPages = new ArrayBlockingQueue<>(QUEUE_LENGTH);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(XML_FILE_PATH);

        producer = new Thread(new WikiPageProducer(unindexedPages, producerDone, readVocabulary(), inputStream, notFull, notEmpty, lock));
        consumer = new Thread(new WikiPageIndexer(unindexedPages, producerDone, indexWriter, notFull, notEmpty, lock));

        producer.start();        consumer.start();
    }

    private void createdLuceneDirectory(Path directoryPath) throws IOException {
        for(File file: directoryPath.toFile().listFiles())
            if (!file.isDirectory())
                file.delete();

        Analyzer analyzer = new StandardAnalyzer();

        Directory directory = FSDirectory.open(directoryPath);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        indexWriter = new IndexWriter(directory, config);
    }

    public void start() throws IOException, XMLStreamException {
        this.startThreads();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public WikiIndexingController() throws IOException, XMLStreamException {
        Path directoryPath = Paths.get(DIRECTORY_PATH);

        this.createdLuceneDirectory(directoryPath);


    }
}
