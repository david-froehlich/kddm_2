package org.kddm2.search.entity;

import org.junit.Before;
import org.junit.Test;
import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.IndexingService;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntityToolsTest {
    private static final String INDEX_PATH = "/tmp/kddmTestIndex";

    private static final String CONTENT = "This is a text. August, August. April. April. Chinese.";

    private Set<String> vocabulary;

    @Before
    public void createIndex() throws Exception {
        IndexingService indexingService = new IndexingService(Paths.get(INDEX_PATH));
        indexingService.start();
    }

    @Before
    public void readVocabulary() {
        vocabulary = new HashSet<>();

        vocabulary.add("april");
        vocabulary.add("august");
        vocabulary.add("chinese");
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
        IndexStatsHelper helper = new IndexStatsHelper(Paths.get(INDEX_PATH));
        EntityTools entityTools = new EntityTools(helper, vocabulary);
        EntityWeightingAlgorithm alg = new EntityWeightingTFIDF(helper, entityTools);
        List<EntityCandidateWeighted> weightedEntityCandidates = alg.determineWeight(entityTools.identifyEntities(CONTENT));
        System.out.println(weightedEntityCandidates);
        //TODO assert
        return;
    }

    @Test
    public void testEntityIdentification() throws Exception {
        IndexStatsHelper helper = new IndexStatsHelper(Paths.get(INDEX_PATH));
        EntityTools entityTools = new EntityTools(helper, vocabulary);
        List<EntityCandidate> candidates = entityTools.identifyEntities(CONTENT);
        System.out.println(candidates);
        //TODO assert
        return;
    }
}