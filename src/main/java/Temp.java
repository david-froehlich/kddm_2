import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class Temp {

    public static void main(String[] args) throws IOException, XMLStreamException {
        WikiIndexingController wikiIndexingController = new WikiIndexingController();
        wikiIndexingController.start();
    }
}
