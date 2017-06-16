package org.kddm2.search.entity;

import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.InvalidIndexException;

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

    /**
     * Assigns weights to candidates and deletes duplicates by choosing the first occurrence.
     * @param candidates The candidates to weigh and de-duplicate.
     * @return The weighted and de-duplicated candidates.
     * @throws InvalidIndexException If the lucene index is invalid.
     */
    public List<EntityCandidateWeighted> determineWeightAndDeduplicate(List<EntityCandidate> candidates) throws InvalidIndexException {
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

    protected abstract float getWeightForCandidate(EntityCandidate candidate, int occurenceCount) throws InvalidIndexException;
}

