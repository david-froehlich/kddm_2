import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class WikiPageProducer implements Runnable {
    private BlockingQueue<WikiPage> unindexedPages;
    private Set<String> vocabulary;
    private WikiXmlReader reader;

    public WikiPageProducer(BlockingQueue<WikiPage> unindexedPages, Set<String> vocabulary,
                            InputStream xmlFileInputStream) throws IOException, XMLStreamException {
        this.unindexedPages = unindexedPages;
        this.vocabulary = vocabulary;
        this.reader = new WikiXmlReader(xmlFileInputStream, vocabulary, 100);
    }


    private void produce() throws InterruptedException, IOException, XMLStreamException {
        while (true) {
            WikiPage nextPage = this.reader.getNextPage();

            if (nextPage == null) {
                int i = WikiIndexingController.CONSUMER_COUNT;
                while (i-- > 0) {
                    this.unindexedPages.put(WikiPage.getEOSPage());
                }
                System.out.println("producer done");
                return;
            }
            this.unindexedPages.put(nextPage);
        }
    }

    @Override
    public void run() {
        try {
            this.produce();
        } catch (InterruptedException | XMLStreamException | IOException e) {
            e.printStackTrace();
        }
    }
}
