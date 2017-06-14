package org.kddm2.search.entity;

public class EntityCandidate {
    final int startPos;
    final int endPos;
    final String wholeContent;

    public EntityCandidate(int startPos, int endPos, String wholeContent) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.wholeContent = wholeContent;
    }

    public EntityCandidate(EntityCandidate source) {
        this.startPos = source.startPos;
        this.endPos = source.endPos;
        this.wholeContent = source.wholeContent;
    }

    public boolean overlaps(EntityCandidate other)
    {
        return Math.max(startPos, other.startPos) < Math.min(endPos, other.endPos);
    }

    public String getCandidateText() {
        return wholeContent.substring(startPos, endPos);
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        try {
            EntityCandidate candidate = (EntityCandidate) o;
            return this.getCandidateText().equals(candidate.getCandidateText());
        } catch (ClassCastException ex) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = startPos;
        result = 31 * result + endPos;
        return result;
    }

    @Override
    public String toString() {
        return "EntityCandidate{" +
                "startPos=" + startPos +
                ", endPos=" + endPos +
                ", text='" + getCandidateText() + '\'' +
                "}\n";
    }
}
