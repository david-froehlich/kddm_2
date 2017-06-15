package org.kddm2.search.entity;

import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.IndexTermStats;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class EntityWeightingAlgorithm {
    protected IndexStatsHelper indexHelper;
    private EntityTools entityTools;

    public EntityWeightingAlgorithm(IndexStatsHelper indexHelper, EntityTools entityTools) {
        this.indexHelper = indexHelper;
        this.entityTools = entityTools;
    }

    public List<EntityCandidateWeighted> determineWeight(List<EntityCandidate> candidates) {
        List<EntityCandidateWeighted> weightedCandidates = new LinkedList<>();
        Map<String, List<EntityCandidate>> groupedCandidates = entityTools.groupEntitiesByText(candidates);
        for (List<EntityCandidate> group : groupedCandidates.values()) {
            int occurrenceCount = group.size();
            EntityCandidate refCandidate = group.get(0);
            weightedCandidates.add(
                    new EntityCandidateWeighted(refCandidate,
                            getWeightForCandidate(refCandidate, occurrenceCount)));
        }
        return weightedCandidates;
    }

    protected abstract float getWeightForCandidate(EntityCandidate candidate, int occurenceCount);
}

