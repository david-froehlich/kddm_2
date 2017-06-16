package org.kddm2.indexing;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Assert;
import org.junit.Test;
import org.kddm2.Settings;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.kddm2.indexing.xml.WikiXmlWriter;
import org.kddm2.lucene.IndexingUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

public class IndexingTest {
    private static final String INDEX_PATH = "/tmp/kddmTestIndex";
    private static final String XML_FILE = "simplewiki-20170501-pages-meta-current.xml.bz2";
    private static final String XML_OUT_PATH = "/tmp/extracted_xml_files.xml";

    @Test
    public void extractTestPageSet() throws Exception {
        Set<String> vocabulary = IndexingUtils.readDictionary(
                getClass().getClassLoader().getResource(Settings.VOCABULARY_PATH).toURI()
        );
        InputStream xmlInputStream = getClass().getClassLoader().getResource(XML_FILE).openStream();
        WikiXmlReader reader = new WikiXmlReader(xmlInputStream, vocabulary);
        IndexingTestUtils utils = new IndexingTestUtils(reader);
        List<WikiPage> pages = utils.extractRandomTestPagesSet(
                10, 0.05f, 0.01f);

        WikiXmlWriter writer = new WikiXmlWriter(XML_OUT_PATH);
        writer.writePages(pages);
        }

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
