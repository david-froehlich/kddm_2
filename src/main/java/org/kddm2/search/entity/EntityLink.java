package org.kddm2.search.entity;

import java.util.List;

public class EntityLink {
    private EntityCandidateWeighted entity;
    private List<EntityDocument> possibleTargetDocuments;

    public EntityLink(EntityCandidateWeighted entity, List<EntityDocument> possibleTargetDocuments) {
        this.entity = entity;
        this.possibleTargetDocuments = possibleTargetDocuments;
    }

    public EntityCandidateWeighted getEntity() {
        return entity;
    }

    public void setEntity(EntityCandidateWeighted entity) {
        this.entity = entity;
    }

    public List<EntityDocument> getPossibleTargetDocuments() {
        return possibleTargetDocuments;
    }

    public void setPossibleTargetDocuments(List<EntityDocument> possibleTargetDocuments) {
        this.possibleTargetDocuments = possibleTargetDocuments;
    }
}
