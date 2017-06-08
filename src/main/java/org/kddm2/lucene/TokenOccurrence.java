package org.kddm2.lucene;

import org.kddm2.search.entity.EntityCandidate;

public class TokenOccurrence {
    public final int startOffset;
    public final int endOffset;

    public TokenOccurrence(int startOffset, int endOffset) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }
    public boolean overlaps(TokenOccurrence other)
    {
        return Math.max(startOffset, other.startOffset) < Math.min(endOffset, other.endOffset);
    }
}
