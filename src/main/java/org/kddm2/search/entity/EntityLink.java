package org.kddm2.search.entity;

import java.util.List;

public class EntityLink {
    private EntityCandidate entity;
    private List<EntityLinkTarget> targets;
    private float combinedWeight;

    public EntityLink(EntityCandidate entity, List<EntityLinkTarget> targets) {
        this.entity = entity;
        this.targets = targets;
    }

    public float getCombinedWeight() {
        return combinedWeight;
    }

    public void setCombinedWeight(float combinedWeight) {
        this.combinedWeight = combinedWeight;
    }

    public EntityCandidate getEntity() {
        return entity;
    }

    public void setEntity(EntityCandidate entity) {
        this.entity = entity;
    }

    public List<EntityLinkTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<EntityLinkTarget> targets) {
        this.targets = targets;
    }

    @Override
    public String toString() {
        return "EntityLink{" +
                "entity=" + entity +
                "    , targets=" + targets +
                "}\n";
    }
}
