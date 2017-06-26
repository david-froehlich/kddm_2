package org.kddm2.search.entity;

public class ResultStats {
    private float precision;
    private float recall;

    public ResultStats(float precision, float recall) {
        this.precision = precision;
        this.recall = recall;
    }

    public float getF1Score() {
        return 2 * precision * recall / (precision + recall);
    }


    public float getPrecision() {
        return precision;
    }

    public void setPrecision(float precision) {
        this.precision = precision;
    }

    public float getRecall() {
        return recall;
    }

    public void setRecall(float recall) {
        this.recall = recall;
    }

    @Override
    public String toString() {
        return String.format("Precision: %.3f\nRecall   : %.3f\nF1 score : %.3f", precision, recall, getF1Score());
    }
}
