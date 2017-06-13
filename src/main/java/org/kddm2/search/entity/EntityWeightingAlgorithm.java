package org.kddm2.search.entity;

import org.kddm2.indexing.IndexHelper;
import org.kddm2.indexing.IndexTermStats;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//i'm not good with naming stuff...
public abstract class EntityWeightingAlgorithm {
    IndexHelper indexHelper;
    EntityTools entityTools;

    public EntityWeightingAlgorithm(IndexHelper indexHelper, EntityTools entityTools) {
        this.indexHelper = indexHelper;
        this.entityTools = entityTools;
    }

    public abstract List<WeightedEntityCandidate> determineWeight(
            List<EntityCandidate> candidates);
}

class TfIDFEntityExtraction extends EntityWeightingAlgorithm {
    public TfIDFEntityExtraction(IndexHelper indexHelper, EntityTools entityTools) {
        super(indexHelper, entityTools);
    }

    private double getTfIDFForCandidate(EntityCandidate candidate, int occurenceCount) {
        IndexTermStats statsForDictTerm = indexHelper.getStatsForDictTerm(candidate.getCandidateText());
        if(statsForDictTerm.getCountOccurenceDocuments() > 1) {
            return occurenceCount / Math.log(statsForDictTerm.getCountOccurenceDocuments());
        } else {
            return occurenceCount * Math.E;
        }
    }

    @Override
    public List<WeightedEntityCandidate> determineWeight(List<EntityCandidate> candidates) {
        List<WeightedEntityCandidate> weightedCandidates = new LinkedList<>();

        Map<String, List<EntityCandidate>> groupedCandidates = entityTools.groupEntitiesByText(candidates);

        for (List<EntityCandidate> group : groupedCandidates.values()) {
            int occurence_count = group.size();
            EntityCandidate refCandidate = group.get(0);
            weightedCandidates.add(
                    new WeightedEntityCandidate(refCandidate,
                            getTfIDFForCandidate(refCandidate, occurence_count)));
        }
        return weightedCandidates;
    }
}