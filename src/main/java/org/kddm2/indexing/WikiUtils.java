package org.kddm2.indexing;

import org.apache.lucene.analysis.TokenStream;
import org.kddm2.Settings;
import org.kddm2.lucene.IndexingUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiUtils {
    public static Pattern linkRegex;

    static {
        linkRegex = Pattern.compile("\\[\\[([\\w\\s]+)(:?\\|([\\w\\s]+?))?\\]\\]");
    }

    public static Map<WikiLink, Integer> parseLinkedOccurrences(String text, Set<String> vocabulary) {
        Map<WikiLink, Integer> wikiLinks = new HashMap<>();
        Matcher matcher = WikiUtils.linkRegex.matcher(text);
        while (matcher.find()) {
            String pageId = matcher.group(1).trim();
            String linkText = pageId;
            if (matcher.groupCount() > 2) {
                linkText = matcher.group(2);
                if (linkText == null) {
                    linkText = pageId;
                } else {
                    linkText = linkText.trim();
                }
            }
            //TODO: hack for weird links that start with |
            if (pageId.startsWith("|")) {
                pageId = pageId.substring(1);
            }
            if (linkText.startsWith("|")) {
                linkText = linkText.substring(1);
            }
            WikiLink wikiLink = new WikiLink(pageId, linkText);
            Integer count = wikiLinks.get(wikiLink);
            if (count == null) {
                count = 0;
            }
            wikiLinks.put(wikiLink, count + 1);
        }
        return wikiLinks;
    }

    public static Map<String, Integer> parseUnlinkedOccurrences(String text, Set<String> vocabulary) throws IOException {
        try (Reader reader = new StringReader(text);
             TokenStream tokenStream = IndexingUtils.createWikiTokenizer(reader, vocabulary, Settings.MAX_SHINGLE_SIZE)) {
            return IndexingUtils.getTokenOccurrencesInStream(tokenStream);
        }
    }
}
