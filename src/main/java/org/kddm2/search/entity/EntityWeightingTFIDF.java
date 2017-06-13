package org.kddm2.search.entity;

import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.IndexTermStats;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EntityWeightingTFIDF extends EntityWeightingAlgorithm {
    public EntityWeightingTFIDF(IndexStatsHelper indexHelper, EntityTools entityTools) {
        super(indexHelper, entityTools);
    }

    @Override
    protected double getWeightForCandidate(EntityCandidate candidate, int occurenceCount) {
        IndexTermStats statsForDictTerm = indexHelper.getStatsForDictTerm(candidate.getCandidateText());
        if(statsForDictTerm.getCountOccurenceDocuments() > 1) {
            return occurenceCount / Math.log(statsForDictTerm.getCountOccurenceDocuments());
        } else {
            return occurenceCount * Math.E;
        }
    }
}
