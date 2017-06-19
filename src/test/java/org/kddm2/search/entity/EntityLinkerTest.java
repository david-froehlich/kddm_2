package org.kddm2.search.entity;

import org.junit.Test;
import org.kddm2.IndexTestSuite;
import org.kddm2.TestIndexConfig;
import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.WikiPage;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.kddm2.lucene.IndexingUtils;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertNotEquals;

public class EntityLinkerTest {
    private TestIndexConfig config = IndexTestSuite.testIndexFull;

    @Test
    public void testSimpleLinking() throws Exception {
        // use validation set for the test page, but the full index for the analysis
        InputStream wikiInputStream = IndexTestSuite.testIndexValidation.dataSourceResource.getInputStream();
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream);

        WikiPage nextPage = wikiXmlReader.getNextPage();
        System.out.println("Testing on this wiki page:");
        System.out.println("Title: " + nextPage.getTitle());

        EntityWikiLinkExtractor entityExtractionTokenStream = IndexingUtils.createEntityExtractionTokenStream(nextPage.getText());
        List<EntityCandidate> entityCandidates = entityExtractionTokenStream.readEntities();

        IndexStatsHelper indexHelper = new IndexStatsHelper(config.luceneDirectory);
        EntityTools entityTools = new EntityTools(config.vocabulary);

        EntityWeightingAlgorithm algorithm = new EntityWeightingKeyphraseness(indexHelper, entityTools);
        List<EntityCandidateWeighted> entityCandidatesWeighted = algorithm.determineWeightAndDeduplicate(entityCandidates);

        EntityLinker entityLinker = new EntityLinker(config.luceneDirectory);
        List<EntityLink> entityLinks = entityLinker.identifyLinksForCandidates(entityCandidatesWeighted);

        assertNotEquals(entityLinks.size(), 0);

        System.out.println("\nResulting links:");
        System.out.println(entityLinks);
    }
}
