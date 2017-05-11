import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import javax.xml.stream.*;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;

public class WikiXmlReader {
    private InputStream inputStream;
    private BZip2CompressorInputStream compressorInputStream;
    private XMLEventReader xmlEventReader;

    public WikiXmlReader(InputStream inputStream) throws IOException, XMLStreamException {
        this.inputStream = inputStream;
        compressorInputStream = new BZip2CompressorInputStream(inputStream);
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlEventReader = xmlInputFactory.createXMLEventReader(compressorInputStream);
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
}
