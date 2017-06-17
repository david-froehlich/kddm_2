package org.kddm2.indexing;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.kddm2.IndexTestSuite;
import org.kddm2.lucene.IndexingUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.Set;

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
    @Ignore
    public void extractVocabulary() throws Exception {
        InputStream inputStream = new ClassPathResource("simplewiki-20170501-pages-meta-current.xml.bz2").getInputStream();
        Set<String> vocabulary = IndexingUtils.extractVocabulary(inputStream);

        String dictPath = "/tmp/vocabulary.txt";
        try (Writer w = new BufferedWriter(new FileWriter(dictPath))) {
            for (String s : vocabulary) {
                w.write(s);
                w.write("\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
