import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class Indexer {

    public static void main(String[] args) throws IOException, XMLStreamException {
        long start = System.currentTimeMillis();
        WikiIndexingController wikiIndexingController = new WikiIndexingController();
        wikiIndexingController.start();
        System.out.println("Indexing took " + (System.currentTimeMillis() - start) / 1000 + " seconds");
    }
}
