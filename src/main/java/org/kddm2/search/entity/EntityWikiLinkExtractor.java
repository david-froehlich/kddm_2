package org.kddm2.search.entity;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.kddm2.indexing.wiki.WikipediaTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Takes the content of a Wikipedia article and extracts the Entities from all internal links
 */
public class EntityWikiLinkExtractor extends FilteringTokenFilter {
    private String wholeString;

    private TypeAttribute typeAttribute;
    private OffsetAttribute offsetAttribute;

    public EntityWikiLinkExtractor(TokenStream su, String wholeString) {
        super(su);
        typeAttribute = getAttribute(TypeAttribute.class);
        offsetAttribute = addAttribute(OffsetAttribute.class);
        this.wholeString = wholeString;
    }

    /**
     * Returns a map of entity candidates to their actual link targets.
     * @return The map of candidates to link targets.
     * @throws IOException if something is wrong with the token stream.
     */
    public Map<EntityCandidate, String> readWikiLinks() throws IOException {
        this.reset();

        Map<EntityCandidate, String> extractedLinks = new HashMap<>();

        boolean insideLink = false;
        int linkTargetStart = 0;
        int linkTargetEnd = 0;
        int linkTextStart = 0;
        int linkTextEnd = 0;
        while (incrementToken()) {
            if (WikipediaTokenizer.INTERNAL_LINK_TARGET.equals(typeAttribute.type())) {
                linkTargetStart = offsetAttribute.startOffset();
                linkTargetEnd = offsetAttribute.endOffset();
                linkTextStart = linkTargetStart;
                linkTextEnd = linkTargetEnd;
                insideLink = true;
            } else if (WikipediaTokenizer.INTERNAL_LINK.equals(typeAttribute.type())) {
                insideLink = true;
                linkTextStart = offsetAttribute.startOffset();
                linkTextEnd = offsetAttribute.endOffset();
            } else {
                if (insideLink) {
                    EntityCandidate e = new EntityCandidate(linkTextStart, linkTextEnd, wholeString);
                    extractedLinks.put(e, wholeString.substring(linkTargetStart, linkTargetEnd));
                    insideLink = false;
                    linkTargetStart = 0;
                    linkTargetEnd = 0;
                    linkTextStart = 0;
                    linkTextEnd = 0;
                }
            }
        }
        return extractedLinks;
    }

    /**
     * the stream is the last stream in the pipe, so it only gets the tokens that should be accepted anyway
     */
    @Override
    protected boolean accept() throws IOException {
        return true;
    }
}
