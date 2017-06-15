package org.kddm2.search.entity;

import org.apache.lucene.analysis.TokenStream;
import org.junit.Before;
import org.junit.Test;
import org.kddm2.Settings;
import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.IndexingService;
import org.kddm2.indexing.WikiPage;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.kddm2.lucene.IndexingUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

public class EntityIdentifierTest {
    private static final String INDEX_PATH = "/tmp/test_index/";
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
    public void testEntityExtraction() throws Exception {
        InputStream wikiInputStream = getClass().getClassLoader().getResourceAsStream(Settings.XML_FILE_PATH);
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream, vocabulary);

        WikiPage nextPage = wikiXmlReader.getNextPage();

        EntityExtractor entityExtractionTokenStream = IndexingUtils.createEntityExtractionTokenStream(
                new StringReader(nextPage.getText()), nextPage.getText());

        List<EntityCandidate> entityCandidates = entityExtractionTokenStream.readEntities();
        //TODO assert
    }

    @Test
    public void testEntityIdentification() throws Exception {
        InputStream wikiInputStream = getClass().getClassLoader().getResourceAsStream(Settings.XML_FILE_PATH);
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream, vocabulary);

        IndexStatsHelper indexHelper = new IndexStatsHelper(Paths.get(INDEX_PATH));
        EntityTools entityTools = new EntityTools(indexHelper, vocabulary);

        //TODO check more than one page
        WikiPage nextPage = wikiXmlReader.getNextPage();

        //TODO
        int maxShingleSize = 3;
        TokenStream wikiPlaintextTokenizer = IndexingUtils.createWikiTokenizer(
                new StringReader(nextPage.getText()), vocabulary, maxShingleSize);

        String plainText = IndexingUtils.tokenStreamToString(wikiPlaintextTokenizer);
        //TODO plain-text contains "the the"
        EntityWeightingAlgorithm algorithm = new EntityWeightingTFIDF(indexHelper, entityTools);
        EntityIdentifier identifier = new EntityIdentifier(algorithm, entityTools, CUTOFF_RATE);

        List<EntityCandidateWeighted> actual = identifier.identifyEntities(plainText);

        EntityExtractor entityExtractionTokenStream = IndexingUtils.createEntityExtractionTokenStream(
                new StringReader(nextPage.getText()), nextPage.getText());

        List<EntityCandidate> expected = entityExtractionTokenStream.readEntities();

        System.out.println(getFScoreForEntityIdentification(expected, actual));
    }

    private float getFScoreForEntityIdentification(List<? extends EntityCandidate> expected,
                                                   List<? extends EntityCandidate> actual) {
        float precision = getPrecision(expected, actual);
        float recall = getRecall(expected, actual);
        return 2 * precision * recall / (precision + recall);
    }

    private float getPrecision(List<? extends EntityCandidate> expected, List<? extends EntityCandidate> actual) {
        int truePositives = 0;
        for (EntityCandidate currentActual :
                actual) {
            if (expected.contains(currentActual)) {
                truePositives++;
            }
        }
        return (float)truePositives / actual.size();
    }

    private float getRecall(List<? extends EntityCandidate> expected, List<? extends EntityCandidate> actual) {
        int truePositives = 0;
        for (EntityCandidate currentExpected:
                expected) {
            if (actual.contains(currentExpected)) {
                truePositives++;
            }
        }
        return (float)truePositives / expected.size();
    }
}
