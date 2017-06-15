package org.kddm2.lucene;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class WikiReplacerTokenFilter extends FilteringTokenFilter {
    static Set<String> acceptedTypes;

    static {
        acceptedTypes = new HashSet<>();
        acceptedTypes.add("<ALPHANUM>");
        acceptedTypes.add(WikipediaTokenizer.BOLD);
        acceptedTypes.add(WikipediaTokenizer.BOLD_ITALICS);
        acceptedTypes.add(WikipediaTokenizer.INTERNAL_LINK);

    }

    public WikiReplacerTokenFilter(TokenStream in) {
        super(in);
    }

    @Override
    protected boolean accept() throws IOException {
        TypeAttribute tAttr = this.getAttribute(TypeAttribute.class);
        String type = tAttr.type();
        return acceptedTypes.contains(type);
    }
}
