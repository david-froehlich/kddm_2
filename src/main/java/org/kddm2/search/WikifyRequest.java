package org.kddm2.search;

/**
 * Created by david on 6/18/17.
 */
public class WikifyRequest {
    private String text;
    private String algorithmId;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAlgorithmId() {
        return algorithmId;
    }

    public void setAlgorithmId(String algorithmId) {
        this.algorithmId = algorithmId;
    }

    public WikifyRequest(String text, String algorithmId) {
        this.text = text;
        this.algorithmId = algorithmId;
    }

    public WikifyRequest() {
    }
}
