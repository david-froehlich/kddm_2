package org.kddm2.search.entity;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;
import org.kddm2.Settings;
import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.IndexingService;
import org.kddm2.indexing.InvalidWikiFileException;
import org.kddm2.indexing.WikiPage;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.kddm2.lucene.IndexingUtils;
import org.springframework.core.io.ClassPathResource;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class EntityLinkerTest {
    private static final String INDEX_PATH = "/tmp/test_index/";
    private static final String XML_FILE = "test-pages.xml.bz2";
    private static final float CUTOFF_RATE = 0.06f;

    private Set<String> vocabulary;
    private Directory indexDirectory;


    @Before
    public void setUp() throws IOException, XMLStreamException, URISyntaxException, InvalidWikiFileException {
        vocabulary = IndexingUtils.readDictionary(
                getClass().getClassLoader().getResource(Settings.VOCABULARY_PATH).toURI());
        Path indexPath = Paths.get(INDEX_PATH);
        indexDirectory = FSDirectory.open(indexPath);
        IndexingService indexingService = new IndexingService(indexDirectory, vocabulary, new ClassPathResource(XML_FILE));
        indexingService.start();
    }

    @Test
    public void testSimpleLinking() throws Exception {
        InputStream wikiInputStream = getClass().getClassLoader().getResourceAsStream(XML_FILE);
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream, vocabulary);

        WikiPage nextPage = wikiXmlReader.getNextPage();
        System.out.println("Testing on this wiki page:");
        System.out.println("Title: " + nextPage.getTitle());
        System.out.println(nextPage.getText());

        FSDirectory directory = FSDirectory.open(Paths.get(INDEX_PATH));

        EntityExtractor entityExtractionTokenStream = IndexingUtils.createEntityExtractionTokenStream(nextPage.getText());
        List<EntityCandidate> entityCandidates = entityExtractionTokenStream.readEntities();

        IndexStatsHelper indexHelper = new IndexStatsHelper(directory);
        EntityTools entityTools = new EntityTools(vocabulary);

        EntityWeightingAlgorithm algorithm = new EntityWeightingTFIDF(indexHelper, entityTools);
        List<EntityCandidateWeighted> entityCandidatesWeighted = algorithm.determineWeight(entityCandidates);

        EntityLinker entityLinker = new EntityLinker(directory);
        List<EntityLink> entityLinks = entityLinker.identifyLinksForCandidates(entityCandidatesWeighted);

        assertNotEquals(entityLinks.size(), 0);
        assertEquals(entityLinks.size(), 3);

        String title = nextPage.getTitle().trim().toLowerCase();
        for (EntityLink link : entityLinks) {
            String linkText = link.getEntity().getCandidateText().trim().toLowerCase();
            if (title.equalsIgnoreCase(linkText)) {
                EntityDocument topDoc = link.getTargets().get(0);
                assertEquals(title, topDoc.getDocumentId());
            }
        }

        System.out.println("\nResults:");
        System.out.println(entityLinks);
    }
}
