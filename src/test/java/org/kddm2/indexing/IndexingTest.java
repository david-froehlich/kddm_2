package org.kddm2.indexing;

import org.junit.Assert;
import org.junit.Test;
import org.kddm2.IndexTestSuite;

public class IndexingTest {

    @Test
    public void testTermNotInVocabulary() throws Exception {
        IndexStatsHelper helper = new IndexStatsHelper(IndexTestSuite.testIndexSmall.luceneDirectory);
        String term = "is";
        Assert.assertEquals(false, IndexTestSuite.testIndexSmall.vocabulary.getVocabularySet().contains(term));
        IndexTermStats statsForDictTerm = helper.getStatsForDictTerm(term);
        System.out.println("term: '" + term + "' " + statsForDictTerm);
        Assert.assertEquals(0, statsForDictTerm.getCountOccurences());
        Assert.assertEquals(0, statsForDictTerm.getCountLinkings());
        Assert.assertEquals(0, statsForDictTerm.getCountOccurenceDocuments());
        Assert.assertEquals(0, statsForDictTerm.getCountLinkingDocuments());
    }

    @Test
    public void testInVocabulary() throws Exception {
        IndexStatsHelper helper = new IndexStatsHelper(IndexTestSuite.testIndexSmall.luceneDirectory);
        String term = "beauty";
        Assert.assertEquals(true, IndexTestSuite.testIndexSmall.vocabulary.getVocabularySet().contains(term));
        IndexTermStats statsForDictTerm = helper.getStatsForDictTerm(term);
        System.out.println("term: '" + term + "' " + statsForDictTerm);
        Assert.assertEquals(2, statsForDictTerm.getCountOccurences());
        Assert.assertEquals(0, statsForDictTerm.getCountLinkings());
        Assert.assertEquals(2, statsForDictTerm.getCountOccurenceDocuments());
        Assert.assertEquals(0, statsForDictTerm.getCountLinkingDocuments());
    }
}
