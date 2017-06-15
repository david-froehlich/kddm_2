package org.kddm2.search.entity;

import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.kddm2.Settings;
import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.IndexingService;
import org.kddm2.indexing.WikiPage;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.kddm2.lucene.IndexingUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

public class EntityLinkerTest {
    private static final String INDEX_PATH = "/tmp/test_index/";
    private static final String TEST_XML_PATH = "test-pages.xml.bz2";
    private static final float CUTOFF_RATE = 0.06f;

    private Set<String> vocabulary;

    @Before
    public void setUpVocabulary() throws URISyntaxException {
        vocabulary = IndexingUtils.readDictionary(
                getClass().getClassLoader().getResource(Settings.VOCABULARY_PATH).toURI());
    }

    @Before
    public void setUpDirectory() throws IOException, XMLStreamException {
        IndexingService indexingService = new IndexingService(Paths.get(INDEX_PATH));
        indexingService.start();
    }

    @Test
    public void testSimpleLinking() throws Exception {
        InputStream wikiInputStream = getClass().getClassLoader().getResourceAsStream(TEST_XML_PATH);
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream, vocabulary);

        WikiPage nextPage = wikiXmlReader.getNextPage();
        System.out.println("Testing on this wiki page:");
        System.out.println("Title: " + nextPage.getTitle());
        System.out.println(nextPage.getText());

        FSDirectory directory = FSDirectory.open(Paths.get(INDEX_PATH));

        EntityExtractor entityExtractionTokenStream = IndexingUtils.createEntityExtractionTokenStream(nextPage.getText());
        List<EntityCandidate> entityCandidates = entityExtractionTokenStream.readEntities();

        IndexStatsHelper indexHelper = new IndexStatsHelper(directory);
        EntityTools entityTools = new EntityTools(indexHelper, vocabulary);

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