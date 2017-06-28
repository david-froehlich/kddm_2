package org.kddm2.search.entity;

import org.junit.Test;
import org.kddm2.IndexTestSuite;
import org.kddm2.TestIndexConfig;
import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.WikiPage;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.kddm2.lucene.IndexingUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertNotEquals;

public class EntityLinkerTest {
    private TestIndexConfig validationConfig = IndexTestSuite.testIndexValidation;
    private TestIndexConfig fullConfig = IndexTestSuite.testIndexFull;

    @Test
    public void testSimpleLinking() throws Exception {
        // use validation set for the test page, but the full index for the analysis
        InputStream wikiInputStream = validationConfig.wikiXmlResource.getInputStream();
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream);

        IndexStatsHelper indexHelper = new IndexStatsHelper(fullConfig.luceneDirectory);
        EntityTools entityTools = new EntityTools(fullConfig.vocabulary, IndexTestSuite.testDefaultSettings.getMaxShingleSize());
        EntityLinker entityLinker = new EntityLinker(fullConfig.luceneDirectory);
        EntityWeightingAlgorithm algorithm = new EntityWeightingKeyphraseness(indexHelper, entityTools);

        WikiPage nextPage = wikiXmlReader.getNextPage();
        System.out.println("Testing on this wiki page:");
        System.out.println("Title: " + nextPage.getTitle());

        EntityWikiLinkExtractor entityExtractionTokenStream = IndexingUtils.createEntityExtractionTokenStream(nextPage.getText());
        List<EntityCandidate> actualCandidates = entityExtractionTokenStream.getCandidates();

        List<EntityCandidateWeighted> actualCandidatesWeighted = algorithm.determineWeightAndDeduplicate(actualCandidates);
        List<EntityLink> entityLinks = entityLinker.identifyLinksForCandidates(actualCandidatesWeighted);

        assertNotEquals(entityLinks.size(), 0);

        System.out.println("\nResulting links:");
        System.out.println(entityLinks);
    }

    @Test
    public void testLinkingStats() throws Exception {
        // use validation set for the test page, but the full index for the analysis
        InputStream wikiInputStream = validationConfig.wikiXmlResource.getInputStream();
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream);

        IndexStatsHelper indexHelper = new IndexStatsHelper(fullConfig.luceneDirectory);
        EntityTools entityTools = new EntityTools(fullConfig.vocabulary, IndexTestSuite.testDefaultSettings.getMaxShingleSize());
        EntityLinker entityLinker = new EntityLinker(fullConfig.luceneDirectory);
        EntityWeightingAlgorithm algorithm = new EntityWeightingKeyphraseness(indexHelper, entityTools);

        List<ResultStats> pageStats = new ArrayList<>();
        WikiPage nextPage;
        while ((nextPage = wikiXmlReader.getNextPage()) != null) {
            EntityWikiLinkExtractor entityExtractionTokenStream = IndexingUtils.createEntityExtractionTokenStream(nextPage.getText());
            List<EntityCandidate> actualCandidates = entityExtractionTokenStream.getCandidates();
            List<EntityLink> expectedLinks = entityExtractionTokenStream.getWikiLinks();
            actualCandidates.sort(null);

            List<EntityCandidateWeighted> actualCandidatesWeighted = algorithm.determineWeightAndDeduplicate(actualCandidates);
            List<EntityLink> actualLinks = entityLinker.identifyLinksForCandidates(actualCandidatesWeighted);
            actualLinks.sort(Comparator.comparingInt(o -> o.getEntity().getStartPos()));

            assertNotEquals(actualLinks.size(), 0);

            ResultStats stats = getStats(expectedLinks, actualLinks);
            pageStats.add(stats);
        }

        ResultStats mean = new ResultStats(0.0f, 0.0f);
        for (ResultStats result : pageStats) {
            mean.setPrecision(mean.getPrecision() + result.getPrecision() / pageStats.size());
            mean.setRecall(mean.getRecall() + result.getRecall() / pageStats.size());
        }

        System.out.println("Mean stats over " + pageStats.size() + " pages:");
        System.out.println(mean);

        assert (mean.getRecall() > 0.3f);
        assert (mean.getPrecision() > 0.3f);
        assert (mean.getF1Score() > 0.3f);
    }

    private ResultStats getStats(List<EntityLink> expected, List<EntityLink> actual) {
        int truePositives = 0;
        for (EntityLink currentActual : actual) {
            for (EntityLink currentExpected : expected) {
                if (currentActual.getEntity().equals(currentExpected.getEntity())) {
                    if (currentExpected.getTargets().get(0).getDocumentId().equalsIgnoreCase(currentActual.getTargets().get(0).getDocumentId())) {
                        truePositives++;
                    }
                }
            }
        }
        return new ResultStats((float) truePositives / actual.size(), (float) truePositives / expected.size());
    }
}
