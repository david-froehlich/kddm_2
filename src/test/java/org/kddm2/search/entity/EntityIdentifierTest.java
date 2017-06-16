package org.kddm2.search.entity;

import org.apache.lucene.analysis.TokenStream;
import org.junit.Assert;
import org.junit.Test;
import org.kddm2.IndexTestSuite;
import org.kddm2.TestIndexConfig;
import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.InvalidIndexException;
import org.kddm2.indexing.WikiPage;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.kddm2.lucene.IndexingUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Comparator;
import java.util.List;

public class EntityIdentifierTest {

    private static final float LINK_TO_WORD_COUNT_RATE = 0.1f;

    private final TestIndexConfig config = IndexTestSuite.testIndexFull;

    @Test
    public void testEntityExtraction() throws Exception {
        InputStream wikiInputStream = config.dataSourceResource.getInputStream();
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream, config.vocabulary);

        WikiPage nextPage = wikiXmlReader.getNextPage();

        EntityWikiLinkExtractor entityExtractionTokenStream = IndexingUtils.createEntityExtractionTokenStream(
                nextPage.getText());

        List<EntityCandidate> entityCandidates = entityExtractionTokenStream.readEntities();
        Assert.assertNotEquals(0, entityCandidates.size());
        //TODO assert more
    }

    @Test
    public void testEntityIdentification() throws Exception {
        WikiXmlReader wikiXmlReader = new WikiXmlReader(IndexTestSuite.testIndexValidation.dataSourceResource.getInputStream(), config.vocabulary);

        IndexStatsHelper indexHelper = new IndexStatsHelper(config.luceneDirectory);
        EntityTools entityTools = new EntityTools(config.vocabulary);

        //TODO check more than one page
        WikiPage nextPage = wikiXmlReader.getNextPage();
        //TODO plain-text contains "goodness good" for a link like [[Goodness|Good]] "
        TokenStream wikiPlaintextTokenizer = IndexingUtils.createWikiToPlaintextTokenizer(new StringReader(nextPage.getText()));
        String plainText = IndexingUtils.tokenStreamToString(wikiPlaintextTokenizer);
        //TODO aliases like "goodness" and "good" are both linked, even though they would be the same page
        
        EntityWeightingAlgorithm tfidf = new EntityWeightingTFIDF(indexHelper, entityTools);
        EntityWeightingAlgorithm keyphraseness = new EntityWeightingKeyphraseness(indexHelper, entityTools);
        evaluateWeightingAlgorithm(tfidf, entityTools, nextPage, plainText);
        evaluateWeightingAlgorithm(keyphraseness, entityTools, nextPage, plainText);
    }

    private void evaluateWeightingAlgorithm(EntityWeightingAlgorithm weightingAlgorithm, EntityTools entityTools, WikiPage nextPage, String plainText) throws InvalidIndexException, IOException {

        EntityIdentifier identifier = new EntityIdentifier(weightingAlgorithm, entityTools, LINK_TO_WORD_COUNT_RATE);

        List<EntityCandidateWeighted> actual = identifier.identifyEntities(plainText);
        actual.sort(Comparator.comparingInt(EntityCandidate::getStartPos));

        EntityWikiLinkExtractor entityExtractionTokenStream =
                IndexingUtils.createEntityExtractionTokenStream(nextPage.getText());

        List<EntityCandidate> expected = entityExtractionTokenStream.readEntities();

        System.out.println("Algorithm performance: " + weightingAlgorithm.getClass().getSimpleName());
        System.out.println("Precision: " + getPrecision(expected, actual));
        System.out.println("Recall   : " + getRecall(expected, actual));
        System.out.println("F1 score : " + getFScoreForEntityIdentification(expected, actual));
        System.out.println("\n");
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
