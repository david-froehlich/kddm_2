package org.kddm2.indexing;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StandardDirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kddm2.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;


/**
 * Provides methods to get stats about terms from the index
 */
@Component
public class IndexStatsHelper {
    private final Directory indexDirectory;
    private DirectoryReader directoryReader;

    @Autowired
    public IndexStatsHelper(Directory indexDirectory)  {
        this.indexDirectory = indexDirectory;
    }

    public IndexStatsHelper(Path indexDirectory) throws IOException {
        this(FSDirectory.open(indexDirectory));
    }

    public IndexTermStats getStatsForDictTerm(String dictTerm) throws InvalidIndexException {
        try {
            if (directoryReader == null) {
                directoryReader = StandardDirectoryReader.open(indexDirectory);
            }
            dictTerm = dictTerm.toLowerCase();
            Term occurenceTerm = new Term(Settings.TERM_OCCURENCE_FIELD_NAME, dictTerm);
            Term linkingTerm = new Term(Settings.TERM_LINKING_FIELD_NAME, dictTerm);

            long countOccurences = directoryReader.totalTermFreq(occurenceTerm);
            long countLinkings = directoryReader.totalTermFreq(linkingTerm);
            long countOccurenceDocuments = directoryReader.docFreq(occurenceTerm);
            long countLinkingDocuments = directoryReader.docFreq(linkingTerm);
            return new IndexTermStats(countOccurences, countLinkings, countOccurenceDocuments, countLinkingDocuments);
        } catch (IOException e) {
            throw new InvalidIndexException("Invalid lucene index", e);
        }
    }
}
