package org.kddm2.search.entity;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.kddm2.indexing.wiki.WikipediaTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Takes the content of a Wikipedia article and extracts the Entities from all internal links.
 */
public class EntityWikiLinkExtractor extends FilteringTokenFilter {
    private String wholeString;

    private TypeAttribute typeAttribute;
    private OffsetAttribute offsetAttribute;

    private boolean extractingDone = false;
    private List<EntityLink> extractedLinks;
    private List<EntityCandidate> extractedCandidates;

    public EntityWikiLinkExtractor(TokenStream su, String wholeString) {
        super(su);
        typeAttribute = getAttribute(TypeAttribute.class);
        offsetAttribute = addAttribute(OffsetAttribute.class);
        this.wholeString = wholeString;
        extractedLinks = new ArrayList<>();
        extractedCandidates = new ArrayList<>();
    }

    private void read() throws IOException {
        if (extractingDone) {
            return;
        }
        this.reset();
        boolean insideLink = false;
        int linkTargetStart = 0;
        int linkTargetEnd = 0;
        int linkTextStart = 0;
        int linkTextEnd = 0;
        while (incrementToken()) {
            if (WikipediaTokenizer.INTERNAL_LINK_TARGET.equals(typeAttribute.type())) {
                if (insideLink) {
                    EntityCandidate e = new EntityCandidate(linkTextStart, linkTextEnd, wholeString);
                    extractedLinks.add(new EntityLink(e, wholeString.substring(linkTargetStart, linkTargetEnd)));
                }
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
                    extractedLinks.add(new EntityLink(e, wholeString.substring(linkTargetStart, linkTargetEnd)));
                    insideLink = false;
                    linkTargetStart = 0;
                    linkTargetEnd = 0;
                    linkTextStart = 0;
                    linkTextEnd = 0;
                }
            }
        }

        for (EntityLink link : extractedLinks) {
            extractedCandidates.add(link.getEntity());
        }

        extractingDone = true;
    }

    /**
     * Returns a map of entity candidates to their actual link targets.
     *
     * @return The map of candidates to link targets.
     * @throws IOException if something is wrong with the token stream.
     */
    public List<EntityLink> getWikiLinks() throws IOException {
        if (!extractingDone) {
            read();
        }

        return extractedLinks;
    }

    public List<EntityCandidate> getCandidates() throws IOException {
        if (!extractingDone) {
            read();
        }
        return extractedCandidates;
    }

    /**
     * the stream is the last stream in the pipe, so it only gets the tokens that should be accepted anyway
     */
    @Override
    protected boolean accept() throws IOException {
        return true;
    }
}
