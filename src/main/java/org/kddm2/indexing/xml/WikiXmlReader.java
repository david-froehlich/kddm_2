package org.kddm2.indexing.xml;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.lucene.analysis.TokenStream;
import org.kddm2.Settings;
import org.kddm2.indexing.WikiLink;
import org.kddm2.indexing.WikiPage;
import org.kddm2.indexing.WikiUtils;
import org.kddm2.lucene.IndexingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

public class WikiXmlReader {
    private static final Logger logger = LoggerFactory.getLogger(WikiXmlReader.class);

    private InputStream compressorInputStream;
    private XMLEventReader xmlEventReader;
    private WikiPage currentPage;
    private int numPages = 0;
    private Set<String> vocabulary;

    public void reset() throws XMLStreamException, IOException {
        compressorInputStream.reset();
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlEventReader = xmlInputFactory.createXMLEventReader(compressorInputStream);
    }

    public WikiXmlReader(InputStream inputStream, Set<String> vocabulary) throws IOException, XMLStreamException {
        compressorInputStream = new BZip2CompressorInputStream(inputStream);
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlEventReader = xmlInputFactory.createXMLEventReader(this.compressorInputStream);
        this.vocabulary = vocabulary;
    }

    public void iteratePages() throws XMLStreamException {
        int numPages = 0;

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                if (startElement.getName().getLocalPart().equalsIgnoreCase("page")) {
                    numPages++;
                }

            }
        }
        logger.info("Parsed " + numPages + " pages");
    }


    public WikiPage getNextPage() throws XMLStreamException, IOException {
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                this.handleStartElement(xmlEvent.asStartElement());
            } else if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if ("page".equals(endElement.getName().getLocalPart()) && currentPage != null) {
                    if (!currentPage.isValid()) {
                        throw new IllegalArgumentException("org.kddm2.indexing.WikiPage wasn't correctly read");
                    }
                    numPages++;
                    return currentPage;
                }
            }
        }
        logger.info("Parsed " + numPages + " pages");

        return null;
    }

    private void handleStartElement(StartElement startElement) throws XMLStreamException, IOException {
        String tag = startElement.getName().getLocalPart();

        switch (tag) {
            case "page":
                currentPage = new WikiPage();
                break;
            case "title":
                if (currentPage != null) {
                    currentPage.setTitle(
                            xmlEventReader.nextEvent().asCharacters().getData().toLowerCase().trim());
                }
                break;
            case "text":
                if (currentPage != null) {
                    String text = this.getTextForPage();
                    this.parseText(text);
                }
                break;
            case "ns":
                String ns = xmlEventReader.nextEvent().asCharacters().getData();
                if (!"0".equals(ns)) {
                    //page is not an article
                    currentPage = null;
                }
        }
    }

    private String getTextForPage() throws XMLStreamException {
        StringBuilder builder = new StringBuilder();

        XMLEvent xmlEvent = xmlEventReader.nextEvent();
        while (xmlEvent.isCharacters()) {
            builder.append(xmlEvent.asCharacters().getData());
            xmlEvent = xmlEventReader.nextEvent();
        }
        return builder.toString().toLowerCase();
    }

    private void parseText(String text) throws IOException {
        currentPage.setText(text);
        parseUnlinkedOccurences();
        parseLinkedOccurrences();
    }

    private void parseLinkedOccurrences() {
        Map<WikiLink, Integer> wikiLinks = new HashMap<>();
        Matcher matcher = WikiUtils.linkRegex.matcher(currentPage.getText());
        while (matcher.find()) {
            String pageId = matcher.group(1);
            String linkText = pageId;
            if (matcher.groupCount() > 2) {
                linkText = matcher.group(2);
                if (linkText == null) {
                    linkText = pageId;
                }
            }
            WikiLink wikiLink = new WikiLink(pageId, linkText);
            Integer count = wikiLinks.get(wikiLink);
            if (count == null) {
                count = 0;
            }
            wikiLinks.put(wikiLink, count + 1);
        }
        currentPage.setWikiLinks(wikiLinks);
    }

    private void parseUnlinkedOccurences() throws IOException {
        Reader reader = new StringReader(currentPage.getText());

        TokenStream tokenStream = IndexingUtils.createWikiTokenizer(reader, vocabulary, Settings.MAX_SHINGLE_SIZE);
        currentPage.setOccurringTerms(IndexingUtils.getTokenOccurrencesInStream(tokenStream));
        tokenStream.close();
    }

    public void close() throws IOException {
        compressorInputStream.close();
    }
}
