import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StandardDirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;


class IndexTermStats {
    /**
     * the number of unlinked occurences of the term (multiple occurences in one doc are counted multiple times)
     */
    final long countOccurences;

    /**
     * the number of linkings of the term (multiple linkings in one doc are counted multiple times)
     */
    final long countLinkings;

    /**
     * the number of documents that have an unlinked occurence of the term
     */
    final long countOccurenceDocuments;

    /**
     * the number of documents that have a linked occurence of the term
     */
    final long countLinkingDocuments;

    public IndexTermStats(long countOccurences, long countLinkings,
                          long countOccurenceDocuments, long countLinkingDocuments) {
        this.countOccurences = countOccurences;
        this.countLinkings = countLinkings;
        this.countOccurenceDocuments = countOccurenceDocuments;
        this.countLinkingDocuments = countLinkingDocuments;
    }
}

/**
 * Provides methods to get stats about terms from the index
 */
public class IndexHelper {
    private final DirectoryReader directoryReader;
    final int documentCount;


    public IndexHelper(String indexDirectory) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexDirectory));
        directoryReader = StandardDirectoryReader.open(dir);
        documentCount = directoryReader.numDocs();
    }

    public IndexTermStats getStatsForDictTerm(String dictTerm) {
        Term occurenceTerm = new Term(WikiIndexingController.TERM_OCCURENCE_FIELD_NAME, dictTerm);
        Term linkingTerm = new Term(WikiIndexingController.TERM_LINKING_FIELD_NAME, dictTerm);

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


}
