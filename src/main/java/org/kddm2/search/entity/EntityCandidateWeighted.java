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
        return String.format("ECW{%s, s=%d, e=%d, w=%.2f}\n", getCandidateText(), getStartPos(), getEndPos(), getWeight());
    }
}
