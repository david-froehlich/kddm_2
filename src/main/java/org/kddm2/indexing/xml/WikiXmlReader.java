package org.kddm2.indexing.xml;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.kddm2.indexing.WikiPage;
import org.kddm2.indexing.WikiUtils;
import org.kddm2.indexing.lucene.VocabTokenizer;

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
    private BZip2CompressorInputStream compressorInputStream;
    private XMLEventReader xmlEventReader;
    private WikiPage currentPage;
    private int numPages = 0;
    private Set<String> vocabulary;


    public WikiXmlReader(InputStream inputStream, Set<String> vocabulary) throws IOException, XMLStreamException {
        compressorInputStream = new BZip2CompressorInputStream(inputStream);
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlEventReader = xmlInputFactory.createXMLEventReader(compressorInputStream);
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
        System.out.println("Parsed " + numPages + " pages");
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
        System.out.println("Parsed " + numPages + " pages");

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
        parseLinkedOccurences();
    }

    private void parseLinkedOccurences() {
        Map<String, Integer> linkedTerms = new HashMap<>();
        Matcher matcher = WikiUtils.linkRegex.matcher(currentPage.getText());
        while (matcher.find()) {
            String term = matcher.group(1);
            Integer count = linkedTerms.get(term);
            if (count == null) {
                count = 0;
            }
            linkedTerms.put(term, count + 1);
        }
        currentPage.setLinkedTerms(linkedTerms);
    }

    private void parseUnlinkedOccurences() throws IOException {
        int max_n = 3; //TODO
        Reader reader = new StringReader(currentPage.getText());
        VocabTokenizer tokenizer = new VocabTokenizer(reader, max_n, this.vocabulary);
        currentPage.setOccuringTerms(tokenizer.getTokensInStream());
    }

    public void close() throws IOException {
        compressorInputStream.close();
    }
}
