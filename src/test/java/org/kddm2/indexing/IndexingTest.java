package org.kddm2.indexing;

import org.junit.Assert;
import org.junit.Test;
import org.kddm2.IndexTestSuite;
import org.kddm2.lucene.IndexingUtils;
import org.springframework.core.io.FileSystemResource;

import java.io.InputStream;

public class IndexingTest {

    @Test
    public void indexStatsTest() throws Exception {
        IndexStatsHelper helper = new IndexStatsHelper(IndexTestSuite.testIndexSmall.luceneDirectory);
        String termName = "is";
        IndexTermStats statsForDictTerm = helper.getStatsForDictTerm(termName);
        System.out.println(statsForDictTerm);
        Assert.assertEquals(95, statsForDictTerm.getCountOccurences());
        Assert.assertEquals(0, statsForDictTerm.getCountLinkings());
        Assert.assertEquals(5, statsForDictTerm.getCountOccurenceDocuments());
        Assert.assertEquals(0, statsForDictTerm.getCountLinkingDocuments());
    }

    @Test
    public void extractVocabulary() throws Exception {
        InputStream inputStream = new FileSystemResource("/tmp/test-pages.xml.bz2").getInputStream();
        IndexingUtils.extractDictionary(inputStream);
    }
}
