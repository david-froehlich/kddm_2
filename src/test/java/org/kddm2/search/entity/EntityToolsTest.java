package org.kddm2.search.entity;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;
import org.kddm2.IndexTestSuite;
import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.IndexingService;
import org.kddm2.indexing.IndexingVocabulary;
import org.kddm2.indexing.InvalidWikiFileException;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntityToolsTest {
    private static final String INDEX_PATH = "/tmp/wikificationTestIndexEntityTools";
    private static final String XML_FILE = "test-pages.xml.bz2";
    private static final String CONTENT = "This is a text. August, August. April. April. Chinese.";
    Directory indexDirectory;
    private IndexingVocabulary vocabulary;

    @Before
    public void createIndex() throws InvalidWikiFileException, IOException {
        indexDirectory = FSDirectory.open(Paths.get(INDEX_PATH));
        Set<String> fixedVocab = new HashSet<>();
        fixedVocab.add("april");
        fixedVocab.add("august");
        fixedVocab.add("chinese");
        vocabulary = new IndexingVocabulary(fixedVocab);
        IndexingService indexingService = new IndexingService(indexDirectory, vocabulary,
                new ClassPathResource(XML_FILE), IndexTestSuite.testDefaultSettings.getIndexingConsumerCount(),
                IndexTestSuite.testDefaultSettings.getMaxShingleSize());
        indexingService.start();
    }

    @Test
    public void testOverlap() throws Exception {
        String srcText = "a b c d e f g";
        EntityCandidate a = new EntityCandidate(3, 4, srcText);
        EntityCandidate b = new EntityCandidate(4, 5, srcText);
        assertTrue(a.overlaps(a));
        assertFalse(a.overlaps(b));
        assertFalse(b.overlaps(a));
    }

    @Test
    public void testEntityWeighting() throws Exception {
        IndexStatsHelper helper = new IndexStatsHelper(indexDirectory);
        EntityTools entityTools = new EntityTools(vocabulary, IndexTestSuite.testDefaultSettings.getMaxShingleSize());
        EntityWeightingAlgorithm alg = new EntityWeightingTFIDF(helper, entityTools);
        List<EntityCandidateWeighted> weightedEntityCandidates = alg.determineWeightAndDeduplicate(entityTools.identifyEntities(CONTENT));
        System.out.println(weightedEntityCandidates);
        //TODO assert
        return;
    }

    @Test
    public void testEntityIdentification() throws Exception {
        IndexStatsHelper helper = new IndexStatsHelper(indexDirectory);
        EntityTools entityTools = new EntityTools(vocabulary, IndexTestSuite.testDefaultSettings.getMaxShingleSize());
        List<EntityCandidate> candidates = entityTools.identifyEntities(CONTENT);
        System.out.println(candidates);
        //TODO assert
        return;
    }
}
