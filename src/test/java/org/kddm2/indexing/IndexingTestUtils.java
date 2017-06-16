package org.kddm2.indexing;

import org.kddm2.indexing.xml.WikiXmlReader;
import org.kddm2.lucene.IndexingUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class IndexingTestUtils {
    WikiXmlReader reader;

    public IndexingTestUtils(WikiXmlReader reader) {
        this.reader = reader;
    }

    private int countLinkSum(WikiPage page) {
        int sum = 0;
        for (Integer i : page.getWikiLinks(). values()) {
            sum += i;
        }
        return sum;
    }

    /**
     * returns a set of numPages WikiPages that have a link-ratio between optimalLInkRatio +/- maxLinkRatioDistance
     * - links with the same target are only counted once when calculating the ratio
     * - whether a WikiPage is added is random, so runtime is potentially infinite.
     * @param numPages
     * @param optimalLinkRatio
     * @param maxLinkRatioDistance
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    public List<WikiPage> extractRandomTestPagesSet(int numPages, float optimalLinkRatio, float maxLinkRatioDistance) throws IOException, XMLStreamException {
        List<WikiPage> goodWikiPages = new LinkedList<>();

        float minRand = 0.95f;

        WikiPage page = reader.getNextPage();
        while(numPages > 0) {
            int uniqueLinkCount = page.getWikiLinks().size();
            int wordCount = IndexingUtils.getWordCount(new StringReader(page.getText()));

            float linkRatio = uniqueLinkCount / (float)wordCount;
            float dist = Math.abs(linkRatio - optimalLinkRatio);

            if(dist < maxLinkRatioDistance && new Random().nextFloat() > minRand) {
                goodWikiPages.add(page);
                numPages--;
            }
            page = reader.getNextPage();
            if(page == null) {
                reader.reset();
                page = reader.getNextPage();
                minRand = Math.max(0, minRand - 0.1f);
            }
        }

        return goodWikiPages;
    }
}
