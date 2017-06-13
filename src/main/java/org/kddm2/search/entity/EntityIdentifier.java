package org.kddm2.search.entity;

import org.kddm2.indexing.IndexHelper;
import org.kddm2.lucene.IndexingUtils;

import java.util.Collections;
import java.util.List;

public class EntityIdentifier {
    private EntityWeightingAlgorithm algorithm;
    private EntityTools entityTools;
    private float cutoffRate;

    public EntityIdentifier(EntityWeightingAlgorithm algorithm, EntityTools entityTools, float cutoffRate) {
        this.algorithm = algorithm;
        this.entityTools = entityTools;
        this.cutoffRate = cutoffRate;
        if(this.cutoffRate > 1.0f) {
            throw new IllegalArgumentException("cutoff-rate > 1.0 doesn't make sense...");
        }
    }

    public List<WeightedEntityCandidate> identifyEntities(String text) {
        List<EntityCandidate> entityCandidates = entityTools.identifyEntities(text);
        List<WeightedEntityCandidate> entities = algorithm.determineWeight(entityCandidates);
        entities.sort((left, right) -> (int) Math.signum(right.weight - left.weight));
        int returnedEntitityCount = (int)Math.ceil(entities.size() * cutoffRate);
        return entities.subList(0, returnedEntitityCount);
    }
}
