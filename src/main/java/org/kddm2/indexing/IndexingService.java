package org.kddm2.indexing;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.kddm2.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IndexingService {
    private static final Logger LOG = LoggerFactory.getLogger(IndexingService.class);
    private final Resource wikiXmlFile;
    private final int indexingConsumerCount;
    private final int maxShingleSize;
    private Thread producer;
    private List<Thread> consumers;
    private IndexWriter indexWriter;
    private DirectoryReader directoryReader;
    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicInteger numProcessedPages = new AtomicInteger();
    private BlockingQueue<IndexingTask> indexingTasks = new ArrayBlockingQueue<>(Settings.QUEUE_LENGTH);
    private Directory indexDirectory;
    private IndexingVocabulary vocabulary;
    private Map<String, Set<String>> documentSynonyms;


    public IndexingService(Directory indexDirectory,
                           IndexingVocabulary vocabulary,
                           Resource wikiXmlFile,
                           int indexingConsumerCount,
                           int maxShingleSize) {
        this.indexDirectory = indexDirectory;
        this.vocabulary = vocabulary;
        this.wikiXmlFile = wikiXmlFile;
        this.indexingConsumerCount = indexingConsumerCount;
        this.maxShingleSize = maxShingleSize;
        documentSynonyms = new HashMap<>();
    }

    public boolean isRunning() {
        return running.get();
    }

    public IndexingStatus getStatus() {
        boolean indexValid;
        int numDocumentsInIndex = 0;
        try {
            if (directoryReader == null) {
                directoryReader = DirectoryReader.open(indexDirectory);
            }
            if (!directoryReader.isCurrent()) {
                DirectoryReader tmpReader = DirectoryReader.openIfChanged(directoryReader);
                if (tmpReader != null) {
                    directoryReader.close();
                    directoryReader = tmpReader;
                }
            }
            numDocumentsInIndex = directoryReader.numDocs();
            //TODO: find better metric
            indexValid = numDocumentsInIndex > 0;
        } catch (IOException e) {
            indexValid = false;
        }

        return new IndexingStatus(indexValid, numDocumentsInIndex, numProcessedPages.get(), indexingTasks.size(), isRunning());
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

                TopDocs topDocs = searcher.search(new TermQuery(docTerm), 100);
                if (topDocs.scoreDocs.length == 0) {
                    continue;
                }
                if (topDocs.scoreDocs.length > 1) {
                    System.out.println("Error: Found multiple documents for docId " + docId);
                }
                Document doc = searcher.doc(topDocs.scoreDocs[0].doc);

                for (String synonym : entry.getValue()) {
                    doc.add(new StoredField(Settings.SYNONYMS_FIELD_NAME, synonym, WikiPageIndexer.INDEX_FIELD_TYPE));
                }
                indexWriter.updateDocument(docTerm, doc);
            }
        } catch (IOException e) {
            LOG.error("Could not write synonyms to lucene index", e);
        }
    }


    @Async
    public void start() throws InvalidWikiFileException {
        running.set(true);
        try {
            // preload the vocabulary here if its not loaded yet
            numProcessedPages.set(0);
            documentSynonyms = new HashMap<>();

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
            LOG.info("Writing collected synonyms");
            writeSynonyms();
            indexWriter.close();
            documentSynonyms.clear();
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
            producer = new Thread(new WikiPageProducer(indexingTasks, wikiInputStream, numProcessedPages, indexingConsumerCount));
        } catch (IOException | XMLStreamException e) {
            throw new InvalidWikiFileException("Could not load Wiki XML file", e);
        }
        producer.start();
        consumers = new ArrayList<>();
        for (int i = 0; i < indexingConsumerCount; i++) {
            consumers.add(new Thread(new WikiPageIndexer(indexingTasks, indexWriter, vocabulary.getVocabularySet(), documentSynonyms, maxShingleSize)));
            consumers.get(consumers.size() - 1).start();
        }
    }

    public class IndexingStatus {
        public final boolean indexIsValid;
        public final int numDocumentsInIndex;

        public final int numProcessedPages;
        public final int numPagesInQueue;
        public final boolean isRunning;

        public IndexingStatus(boolean indexIsValid, int numDocumentsInIndex, int numProcessedPages, int numPagesInQueue, boolean isRunning) {
            this.indexIsValid = indexIsValid;
            this.numDocumentsInIndex = numDocumentsInIndex;
            this.numProcessedPages = numProcessedPages;
            this.numPagesInQueue = numPagesInQueue;
            this.isRunning = isRunning;
        }
    }
}
