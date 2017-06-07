package org.kddm2.indexing;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

public class IndexingTest {

    private static final String INDEX_PATH = "/tmp/kddmTestIndex";

    @Test
    public void testIndexing() throws Exception {
        IndexingController indexingController = new IndexingController(Paths.get(INDEX_PATH));
        indexingController.start();
    }

    @Test
    public void indexStatsTest() throws Exception {
        IndexHelper helper = new IndexHelper(Paths.get(INDEX_PATH));
        String term_name = "is";
        IndexTermStats statsForDictTerm = helper.getStatsForDictTerm(term_name);
        System.out.println(statsForDictTerm);
        Assert.assertEquals(2, statsForDictTerm.getCountOccurences());
        Assert.assertEquals(0, statsForDictTerm.getCountLinkings());
        Assert.assertEquals(1, statsForDictTerm.getCountOccurenceDocuments());
        Assert.assertEquals(0, statsForDictTerm.getCountLinkingDocuments());
    }
}