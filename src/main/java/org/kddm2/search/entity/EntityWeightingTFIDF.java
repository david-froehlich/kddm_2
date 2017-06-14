package org.kddm2.search.entity;

import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.IndexTermStats;

public class EntityWeightingTFIDF extends EntityWeightingAlgorithm {
    public EntityWeightingTFIDF(IndexStatsHelper indexHelper, EntityTools entityTools) {
        super(indexHelper, entityTools);
    }

    @Override
    protected float getWeightForCandidate(EntityCandidate candidate, int occurenceCount) {
        IndexTermStats statsForDictTerm = indexHelper.getStatsForDictTerm(candidate.getCandidateText());
        if(statsForDictTerm.getCountOccurenceDocuments() > 1) {
            return (float) (occurenceCount / Math.log(statsForDictTerm.getCountOccurenceDocuments()));
        } else {
            return (float) (occurenceCount * Math.E);
        }
    }
}
