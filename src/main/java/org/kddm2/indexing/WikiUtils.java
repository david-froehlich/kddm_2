package org.kddm2.indexing;

import java.util.regex.Pattern;

public class WikiUtils {
    protected static Pattern linkRegex;

    static {
        linkRegex = Pattern.compile("\\[\\[([\\w\\s]+)(:?\\|([\\w\\s]+?))?\\]\\]");
    }

}
