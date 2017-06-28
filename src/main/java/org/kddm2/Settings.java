package org.kddm2;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.yml")
@ConfigurationProperties(prefix = "wikification")
public class Settings {
    public final static int QUEUE_LENGTH = 100;

    //---LUCENE CONSTANTS---
    // name of lucene field that stores the term-occurence (the term was used in the article, but not necessarily linked)
    public final static String TERM_OCCURENCE_FIELD_NAME = "term_occurence";
    // name of lucene field that stores linked terms
    public final static String TERM_LINKING_FIELD_NAME = "term_linking";
    public final static String SYNONYMS_FIELD_NAME = "synonyms";
    public final static String REDIRECTS_FIELD_NAME = "redirects";
    public final static String DOCUMENT_ID_FIELD_NAME = "document_id";
    //---------------------

    private  Resource wikiXmlFile;
    private  Resource vocabularyFile;
    private  int indexingConsumerCount;
    private  int maxShingleSize;
    private  float entityCutoffRateAfterIdentification;

    public Settings() {
    }

    public Settings(Resource wikiXmlFile, Resource vocabularyFile, int indexingConsumerCount, int maxShingleSize, float entityCutoffRateAfterIdentification) {
        this.wikiXmlFile = wikiXmlFile;
        this.vocabularyFile = vocabularyFile;
        this.indexingConsumerCount = indexingConsumerCount;
        this.maxShingleSize = maxShingleSize;
        this.entityCutoffRateAfterIdentification = entityCutoffRateAfterIdentification;
    }

    public Resource getWikiXmlFile() {
        return wikiXmlFile;
    }

    public Resource getVocabularyFile() {
        return vocabularyFile;
    }

    public int getIndexingConsumerCount() {
        return indexingConsumerCount;
    }

    public int getMaxShingleSize() {
        return maxShingleSize;
    }

    public float getEntityCutoffRateAfterIdentification() {
        return entityCutoffRateAfterIdentification;
    }

    public void setWikiXmlFile(Resource wikiXmlFile) {
        this.wikiXmlFile = wikiXmlFile;
    }

    public void setVocabularyFile(Resource vocabularyFile) {
        this.vocabularyFile = vocabularyFile;
    }

    public void setIndexingConsumerCount(int indexingConsumerCount) {
        this.indexingConsumerCount = indexingConsumerCount;
    }

    public void setMaxShingleSize(int maxShingleSize) {
        this.maxShingleSize = maxShingleSize;
    }

    public void setEntityCutoffRateAfterIdentification(float entityCutoffRateAfterIdentification) {
        this.entityCutoffRateAfterIdentification = entityCutoffRateAfterIdentification;
    }
}

