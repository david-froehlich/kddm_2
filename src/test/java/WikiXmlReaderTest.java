import org.junit.Test;

import java.io.FileInputStream;
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

    @Test
    public void testLargeIterate() throws Exception {
        InputStream inputStream = new FileInputStream("/home/sko/workspace/uni/kddm2/data/simplewiki-20170501-pages-meta-current.xml.bz2");
        assertNotNull(inputStream);
        WikiXmlReader w = new WikiXmlReader(inputStream);
        w.iteratePages();
        w.close();
    }

}