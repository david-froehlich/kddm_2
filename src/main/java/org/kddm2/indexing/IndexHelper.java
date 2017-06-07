package org.kddm2.indexing;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StandardDirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;


class IndexTermStats {
    /**
     * the number of unlinked occurences of the term (multiple occurences in one doc are counted multiple times)
     */
    private final long countOccurences;

    /**
     * the number of linkings of the term (multiple linkings in one doc are counted multiple times)
     */
    private final long countLinkings;

    /**
     * the number of documents that have an unlinked occurence of the term
     */
    private final long countOccurenceDocuments;

    /**
     * the number of documents that have a linked occurence of the term
     */
    private final long countLinkingDocuments;

    public IndexTermStats(long countOccurences, long countLinkings,
                          long countOccurenceDocuments, long countLinkingDocuments) {
        this.countOccurences = countOccurences;
        this.countLinkings = countLinkings;
        this.countOccurenceDocuments = countOccurenceDocuments;
        this.countLinkingDocuments = countLinkingDocuments;
    }

    public long getCountOccurences() {
        return countOccurences;
    }

    public long getCountLinkings() {
        return countLinkings;
    }

    public long getCountOccurenceDocuments() {
        return countOccurenceDocuments;
    }

    public long getCountLinkingDocuments() {
        return countLinkingDocuments;
    }

    @Override
    public String toString() {
        return "IndexTermStats{" +
                "countOccurences=" + countOccurences +
                ", countLinkings=" + countLinkings +
                ", countOccurenceDocuments=" + countOccurenceDocuments +
                ", countLinkingDocuments=" + countLinkingDocuments +
                '}';
    }
}

/**
 * Provides methods to get stats about terms from the index
 */
public class IndexHelper {
    private final DirectoryReader directoryReader;
    private final int documentCount;


    public IndexHelper(Path indexDirectory) throws IOException {
        Directory dir = FSDirectory.open(indexDirectory);
        directoryReader = StandardDirectoryReader.open(dir);
        documentCount = directoryReader.numDocs();
    }

    public IndexTermStats getStatsForDictTerm(String dictTerm) {
        Term occurenceTerm = new Term(IndexingController.TERM_OCCURENCE_FIELD_NAME, dictTerm);
        Term linkingTerm = new Term(IndexingController.TERM_LINKING_FIELD_NAME, dictTerm);

        try {
            long countOccurences = directoryReader.totalTermFreq(occurenceTerm);
            long countLinkings = directoryReader.totalTermFreq(linkingTerm);
            long countOccurenceDocuments = directoryReader.docFreq(occurenceTerm);
            long countLinkingDocuments = directoryReader.docFreq(linkingTerm);
            return new IndexTermStats(countOccurences, countLinkings, countOccurenceDocuments, countLinkingDocuments);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getDocumentCount() {
        return documentCount;
    }
}
