import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

public class WikiXmlReaderTest {
    private final String testFileName = "test-pages.xml.bz2";

    @Test
    public void testSimpleOpenClose() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(testFileName);
        assertNotNull(inputStream);
        WikiXmlReader w = new WikiXmlReader(inputStream);
        w.close();
    }

    @Test
    public void testIterate() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(testFileName);
        assertNotNull(inputStream);
        WikiXmlReader w = new WikiXmlReader(inputStream);
        w.iteratePages();
        w.close();
    }

}