package org.kddm2.search.entity;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;
import org.kddm2.IndexTestSuite;
import org.kddm2.Settings;
import org.kddm2.TestIndexConfig;
import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.WikiPage;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.kddm2.lucene.IndexingUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;

import static org.junit.Assert.assertNotEquals;

public class EntityLinkerTest {
    private static final float WEIGHT_RATIO = 1.0f;
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

    private void removeDeadLinks(List<EntityLink> links) {
        try (IndexReader reader = DirectoryReader.open(fullConfig.luceneDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            for (Iterator<EntityLink> link_iter = links.iterator(); link_iter.hasNext();) {
                EntityLink link = link_iter.next();

                Term docTerm = new Term(Settings.DOCUMENT_ID_FIELD_NAME, link.getTargets().get(0).getDocumentId());
                TopDocs topDocs = searcher.search(new TermQuery(docTerm), 100);
                if (topDocs.scoreDocs.length == 0) {
                    link_iter.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void replaceRedirectLinksWithTargetPage(List<EntityLink> links) {
        try (IndexReader reader = DirectoryReader.open(fullConfig.luceneDirectory)) {
            Set<String> fieldsToRetrieve = new HashSet<>();
            fieldsToRetrieve.add(Settings.DOCUMENT_ID_FIELD_NAME);

            IndexSearcher searcher = new IndexSearcher(reader);
            for (EntityLink link : links) {
                Term docTerm = new Term(Settings.REDIRECTS_FIELD_NAME, link.getTargets().get(0).getDocumentId());
                TopDocs topDocs = searcher.search(new TermQuery(docTerm), 100);
                if (topDocs.scoreDocs.length != 0) {
                    int luceneDocId = topDocs.scoreDocs[0].doc;
                    Document doc = searcher.doc(luceneDocId, fieldsToRetrieve);

                    String[] docIdValues = doc.getValues(Settings.DOCUMENT_ID_FIELD_NAME);

                    EntityLinkTarget newTarget = new EntityLinkTarget(docIdValues[0], luceneDocId, 0.0f);
                    link.setTargets(Collections.singletonList(newTarget));
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testIdentificationAndLinkingStats() throws Exception {
        // use validation set for the test page, but the full index for the analysis
        InputStream wikiInputStream = validationConfig.wikiXmlResource.getInputStream();
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream);

        IndexStatsHelper indexHelper = new IndexStatsHelper(fullConfig.luceneDirectory);
        EntityTools entityTools = new EntityTools(fullConfig.vocabulary, IndexTestSuite.testDefaultSettings.getMaxShingleSize());
        EntityLinker entityLinker = new EntityLinker(fullConfig.luceneDirectory);
        EntityWeightingAlgorithm algorithm = new EntityWeightingKeyphraseness(indexHelper, entityTools);

        int i = 0;

        List<ResultStats> pageStats = new ArrayList<>();
        WikiPage nextPage;
        while ((nextPage = wikiXmlReader.getNextPage()) != null) {
            EntityWikiLinkExtractor entityExtractionTokenStream = IndexingUtils.createEntityExtractionTokenStream(nextPage.getText());
            List<EntityCandidate> actualCandidates = entityExtractionTokenStream.getCandidates();
            List<EntityLink> expectedLinks = entityExtractionTokenStream.getWikiLinks();
            replaceRedirectLinksWithTargetPage(expectedLinks);
            expectedLinks = deduplicateExpectedLinks(expectedLinks);

            actualCandidates.sort(null);

            EntityWeightingAlgorithm weightingAlgorithm = new EntityWeightingTFIDF(indexHelper, entityTools);
            EntityIdentifier identifier = new EntityIdentifier(weightingAlgorithm, entityTools, 0.1f);
            String plainText = IndexingUtils.getWikiPlainText(new StringReader(nextPage.getText()));

            List<EntityCandidateWeighted> actualCandidatesWeighted = identifier.identifyEntities(plainText);

            if(++i % 20 == 0) {
                System.out.println(i);
            }

            List<EntityLink> actualLinks = entityLinker.identifyLinksForCandidates(actualCandidatesWeighted);
            int wordCount = IndexingUtils.getWordCount(new StringReader(plainText));

            int maxLinkCount = (int) (wordCount * 0.1f);
            actualLinks = entityTools.cutoffCombinedWeightLinks(
                    entityTools.calculateCombinedWeightsForEntityLinks(
                            actualLinks, WEIGHT_RATIO), maxLinkCount);
            actualLinks.sort(Comparator.comparingInt(o -> o.getEntity().getStartPos()));

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
            replaceRedirectLinksWithTargetPage(expectedLinks);
            expectedLinks = deduplicateExpectedLinks(expectedLinks);

            actualCandidates.sort(null);

            List<EntityCandidateWeighted> actualCandidatesWeighted = algorithm.determineWeightAndDeduplicate(actualCandidates);
            List<EntityLink> actualLinks = entityLinker.identifyLinksForCandidates(actualCandidatesWeighted);
            actualLinks.sort(Comparator.comparingInt(o -> o.getEntity().getStartPos()));

            //assertNotEquals(actualLinks.size(), 0);

            ResultStats stats = getStats(expectedLinks, actualLinks);

            List<EntityLink> noDeadLinks = new ArrayList<>(expectedLinks);
//            removeDeadLinks(noDeadLinks);
            ResultStats stats2 = getStats(noDeadLinks, actualLinks);


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

    private List<EntityLink> deduplicateExpectedLinks(List<EntityLink> expectedLinks) {
        List<EntityLink> dedupLinks = new LinkedList<>();
        for (EntityLink link : expectedLinks) {
            if (!dedupLinks.contains(link)) {
                dedupLinks.add(link);
            }
        }
        return dedupLinks;
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
        int actual_size = actual.size();
        int expected_size = expected.size();

        float precision = (float) truePositives / actual_size;
        if (actual_size == 0) {
            precision = 0.0f;
        }

        float recall = (float) truePositives / expected_size;
        if (expected_size == 0) {
            recall = 1.0f;
        }
        return new ResultStats(precision, recall);
    }
}
