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

    public void setWeight(float weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "EntityCandidateWeighted{" +
                "startPos=" + startPos +
                ", endPos=" + endPos +
                ", text='" + getCandidateText() + '\'' +
                ", weight=" + weight + "}\n";
    }
}
