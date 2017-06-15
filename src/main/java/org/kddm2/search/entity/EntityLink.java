package org.kddm2.search.entity;

import java.util.List;

public class EntityLink {
    private EntityCandidateWeighted entity;
    private List<EntityDocument> targets;

    public EntityLink(EntityCandidateWeighted entity, List<EntityDocument> targets) {
        this.entity = entity;
        this.targets = targets;
    }

    public EntityCandidateWeighted getEntity() {
        return entity;
    }

    public void setEntity(EntityCandidateWeighted entity) {
        this.entity = entity;
    }

    public List<EntityDocument> getTargets() {
        return targets;
    }

    public void setTargets(List<EntityDocument> targets) {
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
