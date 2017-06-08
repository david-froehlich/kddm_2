package org.kddm2.search.entity;

public class EntityCandidate {
    private final int startPos;
    private final int endPos;
    private final String wholeContent;

    public EntityCandidate(int startPos, int endPos, String wholeContent) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.wholeContent = wholeContent;
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
    public String toString() {
        return "EntityCandidate{" +
                "startPos=" + startPos +
                ", endPos=" + endPos +
                ", text='" + getCandidateText() + '\'' +
                '}';
    }
}
