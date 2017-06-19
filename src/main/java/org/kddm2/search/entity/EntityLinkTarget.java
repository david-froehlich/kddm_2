package org.kddm2.search.entity;

public class EntityLinkTarget {
    private String documentId;
    private int luceneDocId;
    private float relevance;

    public EntityLinkTarget(String documentId, int luceneDocId, float relevance) {
        this.documentId = documentId;
        this.luceneDocId = luceneDocId;
        this.relevance = relevance;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public float getRelevance() {
        return relevance;
    }

    public void setRelevance(float relevance) {
        this.relevance = relevance;
    }

    public int getLuceneDocId() {
        return luceneDocId;
    }

    public void setLuceneDocId(int luceneDocId) {
        this.luceneDocId = luceneDocId;
    }

    @Override
    public String toString() {
        return String.format("'%s' %.2f", documentId, relevance);
    }
}
