package org.kddm2.search.entity;

public class EntityCandidateWeighted extends EntityCandidate {
    private float weight;

    public EntityCandidateWeighted(EntityCandidate source, float weight) {
        super(source);
        this.weight = weight;
    }

    public float getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "ECW{'" +
                getCandidateText() + "', " +
                "s=" + startPos +
                ", e=" + endPos +
                ", w=" + weight +
                "}\n";
    }
}
