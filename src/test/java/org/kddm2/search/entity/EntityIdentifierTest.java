package org.kddm2.search.entity;

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

    private static final float LINK_TO_WORD_COUNT_RATE = 0.05f;
    private static final float MIN_STATS = 0.5f;
    private final TestIndexConfig config = IndexTestSuite.testIndexValidation;

    @Test
    public void testEntityExtraction() throws Exception {
        InputStream wikiInputStream = config.wikiXmlResource.getInputStream();
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream);

        WikiPage nextPage = wikiXmlReader.getNextPage();

        EntityWikiLinkExtractor entityExtractionTokenStream = IndexingUtils.createEntityExtractionTokenStream(
                nextPage.getText());

        List<EntityCandidate> entityCandidates = entityExtractionTokenStream.getCandidates();

        Assert.assertNotEquals(0, entityCandidates.size());
        System.out.println(entityCandidates);
    }

    @Test
    public void testEntityIdentification() throws Exception {
        WikiXmlReader wikiXmlReader = new WikiXmlReader(config.wikiXmlResource.getInputStream());

        IndexStatsHelper indexHelper = new IndexStatsHelper(config.luceneDirectory);
        EntityTools entityTools = new EntityTools(config.vocabulary,
                IndexTestSuite.testDefaultSettings.getMaxShingleSize());

        //TODO aliases like "goodness" and "good" are both linked, even though they would be the same page
        EntityWeightingAlgorithm tfidf = new EntityWeightingTFIDF(indexHelper, entityTools);
        EntityWeightingAlgorithm keyphraseness = new EntityWeightingKeyphraseness(indexHelper, entityTools);

        List<ResultStats> tfidfResults = new ArrayList<>();
        List<ResultStats> keyphrasenessResults = new ArrayList<>();
        WikiPage nextPage;
        while ((nextPage = wikiXmlReader.getNextPage()) != null) {
            String plainText = IndexingUtils.getWikiPlainText(new StringReader(nextPage.getText()));

            tfidfResults.add(evaluateWeightingAlgorithm(tfidf, entityTools, nextPage, plainText));
            keyphrasenessResults.add(evaluateWeightingAlgorithm(keyphraseness, entityTools, nextPage, plainText));
        }
        ResultStats tfIdfMean = printResults(tfidf, tfidfResults);
        ResultStats keyphrasenessMean = printResults(keyphraseness, keyphrasenessResults);

        assert (tfIdfMean.getRecall() > MIN_STATS);
        assert (tfIdfMean.getPrecision() > MIN_STATS);
        assert (tfIdfMean.getF1Score() > MIN_STATS);

        assert (keyphrasenessMean.getRecall() > MIN_STATS);
        assert (keyphrasenessMean.getPrecision() > MIN_STATS);
        assert (keyphrasenessMean.getF1Score() > MIN_STATS);
    }

    private ResultStats printResults(EntityWeightingAlgorithm algorithm, List<ResultStats> results) {
        ResultStats meanResult = new ResultStats(0.0f, 0.0f);
        for (ResultStats result : results) {
            meanResult.setPrecision(meanResult.getPrecision() + result.getPrecision() / results.size());
            meanResult.setRecall(meanResult.getRecall() + result.getRecall() / results.size());
        }

        System.out.println("\nAlgorithm performance: " + algorithm.getClass().getSimpleName());
        System.out.println(meanResult);

        return meanResult;
    }

    private ResultStats evaluateWeightingAlgorithm(EntityWeightingAlgorithm weightingAlgorithm, EntityTools entityTools, WikiPage nextPage, String plainText) throws InvalidIndexException, IOException {

        EntityIdentifier identifier = new EntityIdentifier(weightingAlgorithm, entityTools, LINK_TO_WORD_COUNT_RATE);

        List<EntityCandidateWeighted> actual = identifier.identifyEntities(plainText);
        actual.sort(Comparator.comparingInt(EntityCandidate::getStartPos));

        EntityWikiLinkExtractor entityExtractionTokenStream =
                IndexingUtils.createEntityExtractionTokenStream(nextPage.getText().toLowerCase());

        List<EntityCandidate> expected = entityExtractionTokenStream.getCandidates();
        return new ResultStats(getPrecision(expected, actual), getRecall(expected, actual));
    }

    private float getPrecision(List<? extends EntityCandidate> expected, List<? extends EntityCandidate> actual) {
        int truePositives = 0;
        for (EntityCandidate currentActual :
                actual) {
            if (expected.contains(currentActual)) {
                truePositives++;
            }
        }
        if (actual.size() == 0) {
            return 0;
        } else {
            return (float) truePositives / actual.size();
        }
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
        if (expected.size() == 0) {
            return 1;
        } else {
            return (float) truePositives / expected.size();
        }
    }

}


