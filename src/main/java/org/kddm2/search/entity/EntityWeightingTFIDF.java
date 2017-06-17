package org.kddm2.search.entity;

import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.IndexTermStats;
import org.kddm2.indexing.InvalidIndexException;

public class EntityWeightingTFIDF extends EntityWeightingAlgorithm {
    public EntityWeightingTFIDF(IndexStatsHelper indexHelper, EntityTools entityTools) {
        super(indexHelper, entityTools);
    }

    @Override
    protected float getWeightForCandidate(EntityCandidate candidate, int occurenceCount) throws InvalidIndexException {
        IndexTermStats statsForDictTerm = indexHelper.getStatsForDictTerm(candidate.getCandidateText());
        if (statsForDictTerm.getCountOccurenceDocuments() > 1) {
            return (float) (occurenceCount / Math.log(statsForDictTerm.getCountOccurenceDocuments()));
        } else {
            //TODO probably remove the factor, this places candidates that only occur once in the doc. and don't occur anywhere else rather high
            return (float) (occurenceCount * Math.E);
        }
    }
}
