package org.kddm2.lucene;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizer;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class WikiLinkAliasExtractor extends FilteringTokenFilter {
    int start;
    int end;

    private Set<String> extractedAliases = new HashSet<>();
    private TypeAttribute tAttr;
    private CharTermAttribute cAttr;
    private OffsetAttribute oAttr;

    public WikiLinkAliasExtractor(TokenStream su) {
        super(su);
        tAttr = getAttribute(TypeAttribute.class);
        cAttr = getAttribute(CharTermAttribute.class);
        oAttr = addAttribute(OffsetAttribute.class);
    }

    public List<String> readAliases() throws IOException {
        this.reset();
        while (incrementToken()) {
            //do nothing
        }

        List<String> ret = new LinkedList<>();
        //TODO
        return ret;
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
        if (type.equals(WikipediaTokenizer.INTERNAL_LINK)) {
            String temp = cAttr.toString();
            extractedAliases.add(temp);
            start = oAttr.startOffset();
            end = oAttr.endOffset();
        }
        return true;
    }
}
