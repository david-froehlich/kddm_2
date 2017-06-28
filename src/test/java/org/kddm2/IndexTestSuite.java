package org.kddm2;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.kddm2.indexing.*;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.kddm2.indexing.xml.WikiXmlWriter;
import org.kddm2.search.entity.EntityIdentifierTest;
import org.kddm2.search.entity.EntityLinkerTest;
import org.kddm2.search.entity.EntityToolsTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        IndexingTest.class,
        VocabTokenizerTest.class,
        WikiXmlReaderTest.class,
        EntityIdentifierTest.class,
        EntityLinkerTest.class,
        EntityToolsTest.class
})

/**
 * Creates test lucene indices before all JUnit tests if they do not exist yet
 */
public class IndexTestSuite {

    public static TestIndexConfig testIndexFull;
    public static TestIndexConfig testIndexSmall;
    public static TestIndexConfig testIndexValidation;
    public static Settings testDefaultSettings;

    private static String validationTestPagesPath = "file:/tmp/wikificationTestValidationPages.xml.bz2";
    private static String dataSourcePath = "file:data/simplewiki-20170501-pages-meta-current.xml.bz2";
    private static ResourceLoader resourceLoader;

    // this is here so we can run single tests
    static {
        try {
            testDefaultSettings = new Settings(null, null, 5, 5, 0.5f);
            createLuceneIndices();
        } catch (Exception | InvalidWikiFileException e) {
            e.printStackTrace();
        }
    }

    public static void createLuceneIndices() throws Exception, InvalidWikiFileException {
        resourceLoader = new DefaultResourceLoader();

        System.out.println("Loading indices");
        testIndexFull = new TestIndexConfig(
                "/tmp/wikificationTestIndexFull",
                dataSourcePath,
                "file:/tmp/wikificationTestVocabularyFull.txt"
        );
        testIndexSmall = new TestIndexConfig(
                "/tmp/wikificationTestIndexSmall",
                "classpath:test-pages.xml.bz2",
                "file:/tmp/wikificationTestVocabularySmall.txt"
        );
        testIndexValidation = new TestIndexConfig(
                "/tmp/wikificationTestIndexValidation",
                validationTestPagesPath,
                "file:/tmp/wikificationTestVocabularyValidation.txt"
        );

        createValidationTestPages();

        testIndexSmall.createIndexIfNotExists();
        testIndexValidation.createIndexIfNotExists();
        testIndexFull.createIndexIfNotExists();
    }

    private static void createValidationTestPages() throws IOException, XMLStreamException {
        Resource validationTestPages = resourceLoader.getResource(validationTestPagesPath);
        if (!validationTestPages.exists()) {
            System.out.println("Creating validation test pages");
            int numPages = 400;
            InputStream xmlInputStream = resourceLoader.getResource(dataSourcePath).getInputStream();
            WikiXmlReader reader = new WikiXmlReader(xmlInputStream);
            IndexingTestUtils utils = new IndexingTestUtils(reader);
            List<WikiPage> extractedPages = utils.extractRandomTestPagesSet(
                    numPages, 0.05f, 0.01f);
            //TODO: use spring resource for this
            WikiXmlWriter writer = new WikiXmlWriter(validationTestPages.getFile().getAbsolutePath());
            writer.writePages(extractedPages);
            System.out.println("Validation test pages created");
        }
    }

}
