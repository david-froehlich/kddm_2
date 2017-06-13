package org.kddm2.indexing;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StandardDirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kddm2.Settings;

import java.io.IOException;
import java.nio.file.Path;


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
        dictTerm = dictTerm.toLowerCase();
        Term occurenceTerm = new Term(Settings.TERM_OCCURENCE_FIELD_NAME, dictTerm);
        Term linkingTerm = new Term(Settings.TERM_LINKING_FIELD_NAME, dictTerm);

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
