package org.kddm2.indexing;

import org.junit.Assert;
import org.junit.Test;

public class IndexingTest {

    @Test
    public void testIndexing() throws Exception {
        long start = System.currentTimeMillis();
        IndexingController indexingController = new IndexingController();
        indexingController.start();
    }

    @Test
    public void indexStatsTest() throws Exception {
        IndexHelper helper = new IndexHelper(IndexingController.DIRECTORY_PATH);
        String term_name = "is";
        IndexTermStats statsForDictTerm = helper.getStatsForDictTerm(term_name);
        System.out.println(statsForDictTerm);
        Assert.assertEquals(2, statsForDictTerm.getCountOccurences());
        Assert.assertEquals(0, statsForDictTerm.getCountLinkings());
        Assert.assertEquals(1, statsForDictTerm.getCountOccurenceDocuments());
        Assert.assertEquals(0, statsForDictTerm.getCountLinkingDocuments());
    }
}