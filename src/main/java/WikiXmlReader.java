import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.stream.*;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiXmlReader {
    private InputStream inputStream;
    private BZip2CompressorInputStream compressorInputStream;
    private XMLEventReader xmlEventReader;
    private WikiPage currentPage;
    private int numPages = 0;
    private Set<String> vocabulary;


    private int maxPages = -1;

    public WikiXmlReader(InputStream inputStream, Set<String> vocabulary, int maxPages) throws IOException, XMLStreamException {
        this.inputStream = inputStream;
        compressorInputStream = new BZip2CompressorInputStream(inputStream);
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlEventReader = xmlInputFactory.createXMLEventReader(compressorInputStream);
        this.vocabulary = vocabulary;
        this.maxPages = maxPages;
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
        while (xmlEventReader.hasNext() && numPages++ != maxPages) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                this.handleStartElement(xmlEvent.asStartElement());
            } else if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();

                if ("page".equals(endElement.getName().getLocalPart())) {
                    if (!currentPage.isValid()) {
                        throw new IllegalArgumentException("WikiPage wasn't correctly read");
                    }
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
                numPages++;
                break;
            case "title":
                currentPage.setTitle(xmlEventReader.nextEvent().asCharacters().getData());
                break;
            case "text":
                String text = this.getTextForPage();
                this.parseText(text);
                break;
        }
    }

    private String getTextForPage() throws XMLStreamException {
        StringBuilder builder = new StringBuilder();

        XMLEvent xmlEvent = xmlEventReader.nextEvent();
        while(xmlEvent.isCharacters()) {
            builder.append(xmlEvent.asCharacters().getData());
            xmlEvent = xmlEventReader.nextEvent();
        }
        return builder.toString();
    }

    private void parseText(String text) throws IOException {
        currentPage.setText(text);
        parseUnlinkedOccurences();
        parseLinkedOccurences();
    }

    private void parseLinkedOccurences() {
        Set<String> linkedTerms = new HashSet<>();
        Matcher matcher = WikiUtils.linkRegex.matcher(currentPage.getText());
        while(matcher.find()) {
            linkedTerms.add(matcher.group(1));
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
