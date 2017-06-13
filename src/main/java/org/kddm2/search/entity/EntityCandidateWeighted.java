package org.kddm2.search.entity;

public class EntityCandidateWeighted extends EntityCandidate {
    double weight;

    public EntityCandidateWeighted(EntityCandidate source, double weight) {
        super(source);
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
