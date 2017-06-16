package org.kddm2.indexing;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Assert;
import org.junit.Test;
import org.kddm2.Settings;
import org.kddm2.lucene.IndexingUtils;
import org.springframework.core.io.ClassPathResource;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Set;

public class IndexingTest {
    private static final String INDEX_PATH = "/tmp/kddmTestIndex";
    private static final String XML_FILE = "test-pages.xml.bz2";


    @Test
    public void testIndexing() throws Exception, InvalidWikiFileException {
        URL resource = getClass().getClassLoader().getResource(Settings.VOCABULARY_PATH);
        Set<String> vocabulary = IndexingUtils.readDictionary(resource.toURI());
        Directory indexDirectory = FSDirectory.open(Paths.get(INDEX_PATH));
        IndexingService indexingService = new IndexingService(indexDirectory, vocabulary, new ClassPathResource(XML_FILE) {
        });
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
