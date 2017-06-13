package org.kddm2.lucene;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizer;

import java.io.IOException;

public class WikiReplacerTokenFilter extends FilteringTokenFilter {
    public WikiReplacerTokenFilter(TokenStream in) {
        super(in);
    }

    @Override
    protected boolean accept() throws IOException {
        TypeAttribute tAttr = this.getAttribute(TypeAttribute.class);
        String type = tAttr.type();
        return type.equals("<ALPHANUM>") || type.equals(WikipediaTokenizer.INTERNAL_LINK);
    }
}
