package org.kddm2.lucene;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.kddm2.indexing.wiki.WikipediaTokenizer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class WikiToPlaintextFilter extends FilteringTokenFilter {
    private static Set<String> acceptedTypes;

    static {
        acceptedTypes = new HashSet<>();
        acceptedTypes.add("<ALPHANUM>");
        acceptedTypes.add(WikipediaTokenizer.BOLD);
        acceptedTypes.add(WikipediaTokenizer.ITALICS);
        acceptedTypes.add(WikipediaTokenizer.BOLD_ITALICS);
    }

    private final boolean keepInternalLinks;

    public WikiToPlaintextFilter(TokenStream in, boolean keepInternalLinks) {
        super(in);
        this.keepInternalLinks = keepInternalLinks;
    }

    @Override
    protected boolean accept() throws IOException {
        TypeAttribute tAttr = this.getAttribute(TypeAttribute.class);
        String type = tAttr.type();
        boolean accepted = acceptedTypes.contains(type);
        if (!accepted && keepInternalLinks) {
            accepted = type.equals(WikipediaTokenizer.INTERNAL_LINK) || type.equals(WikipediaTokenizer.INTERNAL_LINK_TARGET);
        }
        return accepted;
    }
}
