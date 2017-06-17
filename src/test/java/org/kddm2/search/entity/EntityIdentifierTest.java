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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EntityIdentifierTest {

    private static final float LINK_TO_WORD_COUNT_RATE = 0.1f;
    private final TestIndexConfig config = IndexTestSuite.testIndexFull;

    @Test
    public void testEntityExtraction() throws Exception {
        InputStream wikiInputStream = config.dataSourceResource.getInputStream();
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream);

        WikiPage nextPage = wikiXmlReader.getNextPage();

        EntityWikiLinkExtractor entityExtractionTokenStream = IndexingUtils.createEntityExtractionTokenStream(
                nextPage.getText());

        List<EntityCandidate> entityCandidates = entityExtractionTokenStream.readEntities();
        Assert.assertNotEquals(0, entityCandidates.size());
        //TODO assert more
    }

    @Test
    public void testEntityIdentification() throws Exception {
        WikiXmlReader wikiXmlReader = new WikiXmlReader(IndexTestSuite.testIndexValidation.dataSourceResource.getInputStream());

        IndexStatsHelper indexHelper = new IndexStatsHelper(config.luceneDirectory);
        EntityTools entityTools = new EntityTools(config.vocabulary);

        //TODO plain-text contains "goodness good" for a link like [[Goodness|Good]] "
        //TODO aliases like "goodness" and "good" are both linked, even though they would be the same page

        EntityWeightingAlgorithm tfidf = new EntityWeightingTFIDF(indexHelper, entityTools);
        EntityWeightingAlgorithm keyphraseness = new EntityWeightingKeyphraseness(indexHelper, entityTools);

        List<IdentificationResults> tfidfResults = new ArrayList<>();
        List<IdentificationResults> keyphrasenessResults = new ArrayList<>();
        WikiPage nextPage;
        while ((nextPage = wikiXmlReader.getNextPage()) != null) {
            TokenStream wikiPlaintextTokenizer = IndexingUtils.createWikiToPlaintextTokenizer(new StringReader(nextPage.getText()));
            String plainText = IndexingUtils.tokenStreamToString(wikiPlaintextTokenizer);

            tfidfResults.add(evaluateWeightingAlgorithm(tfidf, entityTools, nextPage, plainText));
            keyphrasenessResults.add(evaluateWeightingAlgorithm(keyphraseness, entityTools, nextPage, plainText));
        }
        printResults(tfidf, tfidfResults);
        printResults(keyphraseness, keyphrasenessResults);
    }

    private void printResults(EntityWeightingAlgorithm algorithm, List<IdentificationResults> results) {
        float meanPrecision = 0.0f;
        float meanRecall = 0.0f;

        for (IdentificationResults result : results) {
            meanPrecision += result.precision / results.size();
            meanRecall += result.recall / results.size();
        }

        System.out.println("\nAlgorithm performance: " + algorithm.getClass().getSimpleName());
        System.out.format("Precision: %.3f\n", meanPrecision);
        System.out.format("Recall   : %.3f\n", meanRecall);
        System.out.format("F1 score : %.3f\n", calculateF1Score(meanPrecision, meanRecall));
    }

    private IdentificationResults evaluateWeightingAlgorithm(EntityWeightingAlgorithm weightingAlgorithm, EntityTools entityTools, WikiPage nextPage, String plainText) throws InvalidIndexException, IOException {

        EntityIdentifier identifier = new EntityIdentifier(weightingAlgorithm, entityTools, LINK_TO_WORD_COUNT_RATE);

        List<EntityCandidateWeighted> actual = identifier.identifyEntities(plainText);
        actual.sort(Comparator.comparingInt(EntityCandidate::getStartPos));

        EntityWikiLinkExtractor entityExtractionTokenStream =
                IndexingUtils.createEntityExtractionTokenStream(nextPage.getText());

        List<EntityCandidate> expected = entityExtractionTokenStream.readEntities();
        return new IdentificationResults(getPrecision(expected, actual), getRecall(expected, actual));
    }

    private float calculateF1Score(float precision,
                                   float recall) {
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
        return (float) truePositives / actual.size();
    }

    //TODO stem words and replace duplicates
    //TODO look at tf-idf, the is top candidate

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

    class IdentificationResults {
        public final float precision;
        public final float recall;

        public IdentificationResults(float precision, float recall) {
            this.precision = precision;
            this.recall = recall;
        }
    }
}
