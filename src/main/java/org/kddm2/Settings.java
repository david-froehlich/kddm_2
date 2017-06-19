package org.kddm2;

public class Settings {
    public final static int QUEUE_LENGTH = 100;

    //---LUCENE CONSTANTS---
    //name of lucene field that stores the term-occurence (the term was used in the article, but not necessarily linked)
    public final static String TERM_OCCURENCE_FIELD_NAME = "term_occurence";
    //name of lucene field that stores linked terms
    public final static String TERM_LINKING_FIELD_NAME = "term_linking";
    public final static String SYNONYMS_FIELD_NAME = "synonyms";
    public final static String DOCUMENT_ID_FIELD_NAME = "document_id";

    public final static int CONSUMER_COUNT = 5;
    public final static int MAX_SHINGLE_SIZE = 3;
    public final static float ENTITY_CUTOFF_RATE_AFTER_IDENTIFICATION = 0.5f;
}

