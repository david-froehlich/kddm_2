package org.kddm2.indexing;

import java.util.regex.Pattern;

public class WikiUtils {
    public static Pattern linkRegex;

    static {
        linkRegex = Pattern.compile("\\[\\[([\\w\\s]+)(:?\\|([\\w\\s]+?))?\\]\\]");
    }

}
