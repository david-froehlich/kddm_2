package org.kddm2.lucene;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

public class WikiField extends Field {

    static final FieldType FIELD_TYPE = new FieldType();

    static {
        FIELD_TYPE.setStored(true);
        FIELD_TYPE.setTokenized(false);
        // TODO: positions are only necessary for debugging with luke
        FIELD_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        FIELD_TYPE.freeze();
    }

    public WikiField(String name, String value) {
        super(name, value, FIELD_TYPE);
    }

}
