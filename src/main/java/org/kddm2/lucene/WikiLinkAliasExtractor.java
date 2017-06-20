package org.kddm2.lucene;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.kddm2.indexing.wiki.WikipediaTokenizer;

import java.io.IOException;
import java.util.Set;

public class WikiLinkAliasExtractor extends FilteringTokenFilter {
    private Set<String> extractedAliases;
    private TypeAttribute tAttr;
    private CharTermAttribute cAttr;
    private String simpleNumberRegex = "-?\\d+(\\.\\d+)?";

    public WikiLinkAliasExtractor(TokenStream su) {
        super(su);
        tAttr = getAttribute(TypeAttribute.class);
        cAttr = getAttribute(CharTermAttribute.class);
    }

    public Set<String> readAliases(Set<String> previouslyExtractedAliases) throws IOException {
        this.extractedAliases = previouslyExtractedAliases;
        this.reset();
        while (incrementToken()) {
            //do nothing
        }

        return extractedAliases;
    }

    private boolean isNumeric(String test) {
        //TODO replace with lib
        return test.matches(simpleNumberRegex);
    }

    /**
     * the stream is the last stream in the pipe, so it only gets the tokens that should be accepted anyway
     *
     * @return
     * @throws IOException
     */
    @Override
    protected boolean accept() throws IOException {
        String type = tAttr.type();
        if (type.equals(WikipediaTokenizer.INTERNAL_LINK) || type.equals(WikipediaTokenizer.INTERNAL_LINK_TARGET)) {
            String alias = cAttr.toString().trim();

            if (alias.length() >= 2 && !isNumeric(alias)) {
                extractedAliases.add(alias);
            }
        }
        return true;
    }
}
