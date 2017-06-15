package org.kddm2.indexing;

import org.junit.Ignore;
import org.junit.Test;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WikiXmlReaderTest {
    private final String TEST_FILE = "test-pages.xml.bz2";

    @Test
    public void testSimpleOpenClose() throws Exception {
        InputStream inputStream = new ClassPathResource(TEST_FILE).getInputStream();
        assertNotNull(inputStream);
        WikiXmlReader w = new WikiXmlReader(inputStream, null);
        w.close();
    }

    @Test
    public void testIterate() throws Exception {
        InputStream inputStream = new ClassPathResource(TEST_FILE).getInputStream();
        assertNotNull(inputStream);
        WikiXmlReader w = new WikiXmlReader(inputStream, null);
        w.iteratePages();
        w.close();
    }

    @Test
    public void testGetPage() throws Exception {
        InputStream inputStream = new ClassPathResource(TEST_FILE).getInputStream();
        assertNotNull(inputStream);

        Set<String> vocabulary = new HashSet<>();
        vocabulary.add("month");
        vocabulary.add("same day");
        vocabulary.add("flower");

        WikiXmlReader w = new WikiXmlReader(inputStream, vocabulary);
        WikiPage page = w.getNextPage();
        assertEquals("april", page.getTitle());
        assertNotNull(page.getText());
        assert(page.getText().length() > 100);
        w.close();
    }

    @Test
    @Ignore
    public void testLargeIterate() throws Exception {
        InputStream inputStream = new FileSystemResource("data/simplewiki-20170501-pages-meta-current.xml.bz2").getInputStream();
        assertNotNull(inputStream);
        WikiXmlReader w = new WikiXmlReader(inputStream, null);
        w.iteratePages();
        w.close();
    }

}