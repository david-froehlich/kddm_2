package org.kddm2.indexing;

import org.junit.Ignore;
import org.junit.Test;
import org.kddm2.indexing.xml.WikiXmlReader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class WikiXmlReaderTest {
    private final String testFileName = "test-pages.xml.bz2";

    @Test
    public void testSimpleOpenClose() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(testFileName);
        assertNotNull(inputStream);
        WikiXmlReader w = new WikiXmlReader(inputStream, null);
        w.close();
    }

    @Test
    public void testIterate() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(testFileName);
        assertNotNull(inputStream);
        WikiXmlReader w = new WikiXmlReader(inputStream, null);
        w.iteratePages();
        w.close();
    }

    @Test
    public void testGetPage() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(testFileName);
        assertNotNull(inputStream);

        Set<String> vocabulary = new HashSet<>();
        vocabulary.add("month");
        vocabulary.add("same day");
        vocabulary.add("flower");

        WikiXmlReader w = new WikiXmlReader(inputStream, vocabulary);
        WikiPage page = w.getNextPage();
        w.close();
    }

    @Test
    @Ignore
    public void testLargeIterate() throws Exception {
        InputStream inputStream = new FileInputStream("/home/sko/workspace/uni/kddm2/data/simplewiki-20170501-pages-meta-current.xml.bz2");
        assertNotNull(inputStream);
        WikiXmlReader w = new WikiXmlReader(inputStream, null);
        w.iteratePages();
        w.close();
    }

}