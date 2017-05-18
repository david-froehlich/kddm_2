import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import javax.xml.stream.*;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class WikiXmlReader {
    private InputStream inputStream;
    private BZip2CompressorInputStream compressorInputStream;
    private XMLEventReader xmlEventReader;

    private Set<String> linkVocabulary;


    public WikiXmlReader(InputStream inputStream) throws IOException, XMLStreamException {
        this.inputStream = inputStream;
        compressorInputStream = new BZip2CompressorInputStream(inputStream);
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlEventReader = xmlInputFactory.createXMLEventReader(compressorInputStream);
    }

    public void findVocabulary() throws XMLStreamException {
        int numPages = 0;

        boolean inPage = false;
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                if ("page".equals(startElement.getName().getLocalPart())) {
                    numPages++;
                    inPage = true;
                }

            } else if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if ("page".equals(endElement.getName().getLocalPart())) {
                    inPage = false;
                }
            }
        }
        System.out.println("Parsed " + numPages + " pages");
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

    public void close() throws IOException {
        compressorInputStream.close();
    }

    public List<String> extractLinks(String text)
    {
        ArrayList<String> links = new ArrayList<>();




    }
}
