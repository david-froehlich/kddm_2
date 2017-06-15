package org.kddm2.indexing;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

public class IndexingTest {

    private static final String INDEX_PATH = "/tmp/kddmTestIndex";

    @Test
    public void testIndexing() throws Exception {
        IndexingService indexingService = new IndexingService(Paths.get(INDEX_PATH));
        indexingService.start();
    }

    @Test
    public void indexStatsTest() throws Exception {
        IndexStatsHelper helper = new IndexStatsHelper(Paths.get(INDEX_PATH));
        String term_name = "is";
        IndexTermStats statsForDictTerm = helper.getStatsForDictTerm(term_name);
        System.out.println(statsForDictTerm);
        Assert.assertEquals(97, statsForDictTerm.getCountOccurences());
        Assert.assertEquals(0, statsForDictTerm.getCountLinkings());
        Assert.assertEquals(5, statsForDictTerm.getCountOccurenceDocuments());
        Assert.assertEquals(0, statsForDictTerm.getCountLinkingDocuments());
    }
}