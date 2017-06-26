package org.kddm2;

import java.util.List;

public class DocumentSearchResult {
    private String documentId;
    private List<String> synonyms;

    public DocumentSearchResult(String documentId, List<String> synonyms) {
        this.documentId = documentId;
        this.synonyms = synonyms;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }
}
