package org.kddm2.indexing;

/**
 * Created by david on 6/13/17.
 */
public class IndexTermStats {
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
