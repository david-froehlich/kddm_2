package org.kddm2.search.entity;

import org.kddm2.indexing.InvalidIndexException;
import org.kddm2.lucene.IndexingUtils;

import java.io.StringReader;
import java.util.List;

public class EntityIdentifier {
    private EntityWeightingAlgorithm algorithm;
    private EntityTools entityTools;
    //ratio of words in text to entities
    private float entityRate;

    public EntityIdentifier(EntityWeightingAlgorithm algorithm, EntityTools entityTools, float entityRate) {
        this.algorithm = algorithm;
        this.entityTools = entityTools;
        this.entityRate = entityRate;
        if (this.entityRate > 1.0f) {
            throw new IllegalArgumentException("cutoff-rate > 1.0 doesn't make sense...");
        }
    }

    public List<EntityCandidateWeighted> identifyEntities(String text) throws InvalidIndexException {
        List<EntityCandidate> entityCandidates = entityTools.identifyEntities(text);
        List<EntityCandidateWeighted> entities = algorithm.determineWeight(entityCandidates);
        entities.sort((left, right) -> (int) Math.signum(right.getWeight() - left.getWeight()));

        int wordCount = IndexingUtils.getWordCount(new StringReader(text));
        int returnedEntityCount = Math.min((int) Math.ceil(wordCount * entityRate)
                , entities.size());
        return entities.subList(0, returnedEntityCount);
    }
}
