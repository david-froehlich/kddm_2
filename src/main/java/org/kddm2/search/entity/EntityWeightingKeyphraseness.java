package org.kddm2.search.entity;

import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.IndexTermStats;
import org.kddm2.indexing.InvalidIndexException;

public class EntityWeightingKeyphraseness extends EntityWeightingAlgorithm {
    public EntityWeightingKeyphraseness(IndexStatsHelper indexHelper, EntityTools entityTools) {
        super(indexHelper, entityTools);
    }

    @Override
    protected float getWeightForCandidate(EntityCandidate candidate, int occurrenceCount) throws InvalidIndexException {
        IndexTermStats statsForDictTerm = indexHelper.getStatsForDictTerm(candidate.getCandidateText());

        long linkings = statsForDictTerm.getCountLinkings();
        long occurrences = statsForDictTerm.getCountOccurenceDocuments();
        if (linkings + occurrences == 0) {
            return 0.0f;
        }
        return ((float) linkings / occurrences + linkings);
    }
}
