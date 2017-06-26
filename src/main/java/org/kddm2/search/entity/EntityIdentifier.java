package org.kddm2.search.entity;

import org.kddm2.indexing.InvalidIndexException;
import org.kddm2.lucene.IndexingUtils;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityIdentifier {
    private EntityWeightingAlgorithm algorithm;
    private EntityTools entityTools;
    //ratio of words in text to entities
    private float entityRate;

    private Map<String, EntityWeightingAlgorithm> availableAlgorithms;

    public void setUsedAlgorithm(String algorithmId) {
        this.algorithm = availableAlgorithms.get(algorithmId);
    }

    public EntityIdentifier(Map<String, EntityWeightingAlgorithm> availableAlgorithms, EntityTools entityTools, float entityRate) {
        this.availableAlgorithms = availableAlgorithms;
        this.algorithm = availableAlgorithms.values().iterator().next();
        this.entityTools = entityTools;
        this.entityRate = entityRate;
        if (this.entityRate > 1.0f) {
            throw new IllegalArgumentException("cutoff-rate > 1.0 doesn't make sense...");
        }
    }

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
        List<EntityCandidateWeighted> weighted = algorithm.determineWeightAndDeduplicate(entityCandidates);
        weighted.sort((left, right) -> (int) Math.signum(right.getWeight() - left.getWeight()));

        int wordCount = IndexingUtils.getWordCount(new StringReader(text));

        int returnedEntityCount = Math.min((int) Math.ceil(wordCount * entityRate)
                , weighted.size());

        List<EntityCandidateWeighted> chosenCandidates = new ArrayList<>(returnedEntityCount);


        for (EntityCandidateWeighted toAdd : weighted) {
            boolean isValid = true;
            for (EntityCandidateWeighted chosen : chosenCandidates) {
                if (chosen.overlaps(toAdd)) {
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                chosenCandidates.add(toAdd);
            }
            if (chosenCandidates.size() >= returnedEntityCount) {
                break;
            }
        }

        return chosenCandidates;
    }
}
