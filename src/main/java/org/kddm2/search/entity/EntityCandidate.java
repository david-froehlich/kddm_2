package org.kddm2.search.entity;

class WeightedEntityCandidate extends EntityCandidate {
    double weight;

    public WeightedEntityCandidate(EntityCandidate source, double weight) {
        super(source);
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "WeightedEntityCandidate{" +
                "startPos=" + startPos +
                ", endPos=" + endPos +
                ", text='" + getCandidateText() + '\'' +
                ", weight=" + weight + "}\n";
    }
}

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
    public String toString() {
        return "EntityCandidate{" +
                "startPos=" + startPos +
                ", endPos=" + endPos +
                ", text='" + getCandidateText() + '\'' +
                "}\n";
    }
}
