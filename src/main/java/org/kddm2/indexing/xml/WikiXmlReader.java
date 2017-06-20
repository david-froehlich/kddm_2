package org.kddm2.indexing.xml;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.kddm2.indexing.WikiPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;

public class WikiXmlReader {
    private static final Logger logger = LoggerFactory.getLogger(WikiXmlReader.class);

    private InputStream compressorInputStream;
    private XMLEventReader xmlEventReader;
    private WikiPage currentPage;
    private int numPages = 0;

    public void reset() throws XMLStreamException, IOException {
        compressorInputStream.reset();
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlEventReader = xmlInputFactory.createXMLEventReader(compressorInputStream);
    }

    public WikiXmlReader(InputStream inputStream) throws IOException, XMLStreamException {
        compressorInputStream = new BZip2CompressorInputStream(inputStream);
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlEventReader = xmlInputFactory.createXMLEventReader(this.compressorInputStream);
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
                    currentPage.setTitle(getContentForElement().toLowerCase().trim());
                }
                break;
            case "text":
                if (currentPage != null) {
                    currentPage.setText(getContentForElement());
                }
                break;
            case "redirect":
                String title = startElement.getAttributeByName(QName.valueOf("title")).getValue().toLowerCase().trim();
                if(currentPage != null) {
                    currentPage.setRedirectTarget(title);
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

    private String getContentForElement() throws XMLStreamException {
        StringBuilder builder = new StringBuilder();

        XMLEvent xmlEvent = xmlEventReader.nextEvent();
        while (xmlEvent.isCharacters()) {
            builder.append(xmlEvent.asCharacters().getData());
            xmlEvent = xmlEventReader.nextEvent();
        }
        return builder.toString().toLowerCase();
    }

    public void close() throws IOException {
        compressorInputStream.close();
    }
}
