package org.kddm2.lucene;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.Set;

public class VocabTokenFilter extends FilteringTokenFilter {
    private Set<String> vocabulary;

    public VocabTokenFilter(TokenStream in, Set<String> vocabulary) {
        super(in);
        this.vocabulary = vocabulary;
    }

    @Override
    protected boolean accept() throws IOException {
        CharTermAttribute attribute = this.getAttribute(CharTermAttribute.class);
        String potentialTerm = attribute.toString();
        return this.vocabulary.contains(potentialTerm);
    }
}
