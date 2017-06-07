package org.kddm2.indexing;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
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
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

public class IndexingController {
    final static int QUEUE_LENGTH = 100;

    //---LUCENE CONSTANTS---
    //name of lucene field that stores the term-occurence (the term was used in the article, but not necessarily linked)
    final static String TERM_OCCURENCE_FIELD_NAME = "term_occurence";
    //name of lucene field that stores linked terms
    final static String TERM_LINKING_FIELD_NAME = "term_linking";
    final static String DIRECTORY_PATH = "/tmp/lucene_dir";
    //final static String DIRECTORY_PATH = "/home/david/mawp";
    private final static String VOCABULARY_PATH = "vocabulary.txt";

    private final static String XML_FILE_PATH = "temp.xml.bz2";
    //private final static String XML_FILE_PATH = "simplewiki-20170501-pages-meta-current.xml.bz2";
    final static int CONSUMER_COUNT = 1;

    static final FieldType INDEX_FIELD_TYPE = new FieldType();

    static {
        INDEX_FIELD_TYPE.setStored(true);
        INDEX_FIELD_TYPE.setTokenized(false);
        INDEX_FIELD_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
    }

    private Thread producer;
    private Thread[] consumers;

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
        BlockingQueue<IndexingTask> indexingTasks = new ArrayBlockingQueue<>(QUEUE_LENGTH);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(XML_FILE_PATH);

        producer = new Thread(new WikiPageProducer(indexingTasks, readVocabulary(), inputStream));
        consumers = new Thread[CONSUMER_COUNT];
        producer.start();
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            consumers[i] = new Thread(new WikiPageIndexer(indexingTasks, indexWriter));
            consumers[i].start();
        }
    }

    private void createdLuceneDirectory(Path directoryPath) throws IOException {
        File f = directoryPath.toFile();
        if (f == null || !f.isDirectory()) {
            Files.createDirectories(directoryPath);
        }
        for (File file : directoryPath.toFile().listFiles())
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
            for (Thread t : consumers) {
                t.join();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public IndexingController() throws IOException, XMLStreamException {
        Path directoryPath = Paths.get(DIRECTORY_PATH);
        this.createdLuceneDirectory(directoryPath);
    }
}
