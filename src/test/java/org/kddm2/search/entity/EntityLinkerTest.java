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
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;

public class EntityLinkerTest {
    private TestIndexConfig config = IndexTestSuite.testIndexFull;

    @Test
    public void testSimpleLinking() throws Exception {
        // use validation set for the test page, but the full index for the analysis
        InputStream wikiInputStream = IndexTestSuite.testIndexValidation.dataSourceResource.getInputStream();
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream);

        IndexStatsHelper indexHelper = new IndexStatsHelper(config.luceneDirectory);
        EntityTools entityTools = new EntityTools(config.vocabulary);
        EntityLinker entityLinker = new EntityLinker(config.luceneDirectory);
        EntityWeightingAlgorithm algorithm = new EntityWeightingKeyphraseness(indexHelper, entityTools);

        WikiPage nextPage = wikiXmlReader.getNextPage();
        System.out.println("Testing on this wiki page:");
        System.out.println("Title: " + nextPage.getTitle());

        EntityWikiLinkExtractor entityExtractionTokenStream = IndexingUtils.createEntityExtractionTokenStream(nextPage.getText());
        Map<EntityCandidate, String> actualLinks = entityExtractionTokenStream.readWikiLinks();
        ArrayList<EntityCandidate> actualCandidates = new ArrayList<>(actualLinks.keySet());

        List<EntityCandidateWeighted> actualCandidatesWeighted = algorithm.determineWeightAndDeduplicate(actualCandidates);

        List<EntityLink> entityLinks = entityLinker.identifyLinksForCandidates(actualCandidatesWeighted);

        assertNotEquals(entityLinks.size(), 0);

        System.out.println("\nResulting links:");
        System.out.println(entityLinks);
    }

    @Test
    public void testLinkingStats() throws Exception {
        // use validation set for the test page, but the full index for the analysis
        InputStream wikiInputStream = IndexTestSuite.testIndexValidation.dataSourceResource.getInputStream();
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream);


        IndexStatsHelper indexHelper = new IndexStatsHelper(config.luceneDirectory);
        EntityTools entityTools = new EntityTools(config.vocabulary);
        EntityLinker entityLinker = new EntityLinker(config.luceneDirectory);
        EntityWeightingAlgorithm algorithm = new EntityWeightingKeyphraseness(indexHelper, entityTools);


        List<ResultStats> pageStats = new ArrayList<>();
        WikiPage nextPage;
        while ((nextPage = wikiXmlReader.getNextPage()) != null) {
            EntityWikiLinkExtractor entityExtractionTokenStream = IndexingUtils.createEntityExtractionTokenStream(nextPage.getText());
            Map<EntityCandidate, String> actualLinks = entityExtractionTokenStream.readWikiLinks();
            ArrayList<EntityCandidate> actualCandidates = new ArrayList<>(actualLinks.keySet());
            actualCandidates.sort(null);

            List<EntityCandidateWeighted> actualCandidatesWeighted = algorithm.determineWeightAndDeduplicate(actualCandidates);
            List<EntityLink> entityLinks = entityLinker.identifyLinksForCandidates(actualCandidatesWeighted);


            assertNotEquals(entityLinks.size(), 0);

            ResultStats stats = getStats(actualLinks, entityLinks);
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

    private ResultStats getStats(Map<EntityCandidate, String> expected, List<EntityLink> actual) {
        int truePositives = 0;
        for (EntityLink currentActual : actual) {
            EntityCandidate currentActualEntity = currentActual.getEntity();
            String expectedPage = expected.get(currentActualEntity);
            if (expectedPage != null) {
                if (expectedPage.equals(currentActual.getTargets().get(0).getDocumentId())) {
                   truePositives++;
                }
            }
        }
        return new ResultStats((float)truePositives / actual.size(), (float)truePositives/expected.size());
    }
}
