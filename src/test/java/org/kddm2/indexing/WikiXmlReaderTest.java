package org.kddm2.indexing;

import org.junit.Test;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WikiXmlReaderTest {
    private final String TEST_FILE = "test-pages.xml.bz2";

    @Test
    public void testSimpleOpenClose() throws Exception {
        InputStream inputStream = new ClassPathResource(TEST_FILE).getInputStream();
        assertNotNull(inputStream);
        WikiXmlReader w = new WikiXmlReader(inputStream);
        w.close();
    }

    @Test
    public void testIterate() throws Exception {
        InputStream inputStream = new ClassPathResource(TEST_FILE).getInputStream();
        assertNotNull(inputStream);
        WikiXmlReader w = new WikiXmlReader(inputStream);
        w.iteratePages();
        w.close();
    }

    @Test
    public void testGetPage() throws Exception {
        InputStream inputStream = new ClassPathResource(TEST_FILE).getInputStream();
        assertNotNull(inputStream);

        WikiXmlReader w = new WikiXmlReader(inputStream);
        WikiPage page = w.getNextPage();
        assertEquals("April", page.getTitle());
        assertNotNull(page.getText());
        assert (page.getText().length() > 100);
        w.close();
    }
}