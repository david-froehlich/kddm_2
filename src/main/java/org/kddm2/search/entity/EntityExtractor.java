package org.kddm2.search.entity;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Takes the content of a Wikipedia article and extracts the Entities from all internal links
 */
public class EntityExtractor extends FilteringTokenFilter {
    private String wholeString;
    private List<EntityCandidate> extractedEntities = new LinkedList<>();
    private TypeAttribute tAttr;
    private OffsetAttribute offsetAttribute;

    public EntityExtractor(TokenStream su, String wholeString) {
        super(su);
        tAttr = getAttribute(TypeAttribute.class);
        offsetAttribute = addAttribute(OffsetAttribute.class);
        this.wholeString = wholeString;
    }

    public List<EntityCandidate> readEntities() throws IOException {
        this.reset();
        while(incrementToken()){
            //do nothing
        }
        return extractedEntities;
    }

    /**
     * the stream is the last stream in the pipe, so it only gets the tokens that should be accepted anyway
     * @return
     * @throws IOException
     */
    @Override
    protected boolean accept() throws IOException {
        String type = tAttr.type();
        if (type.equals(WikipediaTokenizer.INTERNAL_LINK)) {
            int start = offsetAttribute.startOffset();
            int end = offsetAttribute.endOffset();
            extractedEntities.add(new EntityCandidate(start, end, wholeString));
        }
        return true;
    }
}
