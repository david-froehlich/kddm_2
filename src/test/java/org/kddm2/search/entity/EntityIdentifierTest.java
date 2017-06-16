package org.kddm2.search.entity;

import org.apache.lucene.analysis.TokenStream;
import org.junit.Before;
import org.junit.Test;
import org.kddm2.Settings;
import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.WikiPage;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.kddm2.lucene.IndexingUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

public class EntityIdentifierTest {
    private static final String INDEX_PATH = "/tmp/kddmTestIndex";
    private static final String XML_FILE = "extracted_xml_files.xml.bz2";

    private static final float LINK_TO_WORD_COUNT_RATE = 0.05f;

    private Set<String> vocabulary;

    @Before
    public void readVocabulary () throws URISyntaxException {
        vocabulary = IndexingUtils.readDictionary(
                getClass().getClassLoader().getResource(Settings.VOCABULARY_PATH).toURI());
    }

//    @Before
//    public void setUp() throws IOException, XMLStreamException, URISyntaxException, InvalidWikiFileException {
//
//        Path indexPath = Paths.get(INDEX_PATH);
//        FSDirectory indexDirectory = FSDirectory.open(indexPath);
//        IndexingService indexingService = new IndexingService(indexDirectory, vocabulary,  new ClassPathResource(XML_FILE));
//        indexingService.start();
//    }

    @Test
    public void testEntityExtraction() throws Exception {
        InputStream wikiInputStream = new ClassPathResource(XML_FILE).getInputStream();
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream, vocabulary);

        WikiPage nextPage = wikiXmlReader.getNextPage();

        EntityExtractor entityExtractionTokenStream = IndexingUtils.createEntityExtractionTokenStream(
                new StringReader(nextPage.getText()), nextPage.getText());

        List<EntityCandidate> entityCandidates = entityExtractionTokenStream.readEntities();
        //TODO assert
    }

    @Test
    public void testEntityIdentification() throws Exception {
        InputStream wikiInputStream = new ClassPathResource(XML_FILE).getInputStream();
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream, vocabulary);

        IndexStatsHelper indexHelper = new IndexStatsHelper(Paths.get(INDEX_PATH));
        EntityTools entityTools = new EntityTools(vocabulary);

        //TODO check more than one page
        WikiPage nextPage = wikiXmlReader.getNextPage();

        //TODO
        int maxShingleSize = 3;
        TokenStream wikiPlaintextTokenizer = IndexingUtils.createWikiTokenizer(
                new StringReader(nextPage.getText()), vocabulary, maxShingleSize);

        String plainText = IndexingUtils.tokenStreamToString(wikiPlaintextTokenizer);
        //TODO plain-text contains "the the"
        EntityWeightingAlgorithm algorithm = new EntityWeightingTFIDF(indexHelper, entityTools);
        EntityIdentifier identifier = new EntityIdentifier(algorithm, entityTools, LINK_TO_WORD_COUNT_RATE);

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

    //TODO stem words and replace duplicates
    //TODO look at tf-idf, the is top candidate

    private float getPrecision(List<? extends EntityCandidate> expected, List<? extends EntityCandidate> actual) {
        int truePositives = 0;
        for (EntityCandidate currentActual :
                actual) {
            if (expected.contains(currentActual)) {
                truePositives++;
            }
        }
        return (float) truePositives / actual.size();
    }

    private float getRecall(List<? extends EntityCandidate> expected, List<? extends EntityCandidate> actual) {
        int truePositives = 0;
        for (EntityCandidate currentExpected :
                expected) {
            if (actual.contains(currentExpected)) {
                truePositives++;
            }
        }
        return (float) truePositives / expected.size();
    }
}
