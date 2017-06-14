package org.kddm2.search.entity;

import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.IndexTermStats;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//i'm not good with naming stuff...
public abstract class EntityWeightingAlgorithm {
    IndexStatsHelper indexHelper;
    EntityTools entityTools;

    public EntityWeightingAlgorithm(IndexStatsHelper indexHelper, EntityTools entityTools) {
        this.indexHelper = indexHelper;
        this.entityTools = entityTools;
    }

    protected abstract double getWeightForCandidate(EntityCandidate candidate, int occurenceCount);

    public List<EntityCandidateWeighted> determineWeight(List<EntityCandidate> candidates) {
        List<EntityCandidateWeighted> weightedCandidates = new LinkedList<>();
        Map<String, List<EntityCandidate>> groupedCandidates = entityTools.groupEntitiesByText(candidates);
        for (List<EntityCandidate> group : groupedCandidates.values()) {
            int occurence_count = group.size();
            EntityCandidate refCandidate = group.get(0);
            weightedCandidates.add(
                    new EntityCandidateWeighted(refCandidate,
                            getWeightForCandidate(refCandidate, occurence_count)));
        }
        return weightedCandidates;
    }
}

class KeyphrasenessEntityExtraction extends EntityWeightingAlgorithm {
    public KeyphrasenessEntityExtraction(IndexStatsHelper indexHelper, EntityTools entityTools) {
        super(indexHelper, entityTools);
    }

    @Override
    protected double getWeightForCandidate(EntityCandidate candidate, int occurrenceCount) {
        IndexTermStats statsForDictTerm = indexHelper.getStatsForDictTerm(candidate.getCandidateText());

        long linkings = statsForDictTerm.getCountLinkings();
        long occurences = statsForDictTerm.getCountOccurenceDocuments();
        if(linkings + occurences == 0) {
            return 0.0;
        }
        return ((double) linkings / occurences + linkings);
    }
}
