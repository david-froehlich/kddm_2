import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class WikiPageProducer implements Runnable {
    private Queue<WikiPage> unindexedPages;
    private AtomicBoolean isDone;
    private Set<String> vocabulary;
    private WikiXmlReader reader;
    final Condition notFull, notEmpty;
    final Lock lock;

    public WikiPageProducer(Queue<WikiPage> unindexedPages, AtomicBoolean isDone, Set<String> vocabulary,
                            InputStream xmlFileInputStream, Condition notFull, Condition notEmpty, Lock lock) throws IOException, XMLStreamException {
        this.unindexedPages = unindexedPages;
        this.isDone = isDone;
        this.vocabulary = vocabulary;
        this.notFull = notFull;
        this.notEmpty = notEmpty;
        this.lock = lock;
        this.reader = new WikiXmlReader(xmlFileInputStream, vocabulary, 100);
    }


    private void produce() throws InterruptedException, IOException, XMLStreamException {
        while (true) {
            lock.lock();
            while (unindexedPages.size() == WikiIndexingController.QUEUE_LENGTH) {
                notFull.await();
            }
            lock.unlock();

            WikiPage nextPage = this.reader.getNextPage();
            lock.lock();
            if (nextPage == null) {
                isDone.set(true);
                notEmpty.signal();
                lock.unlock();
                return;
            }

            this.unindexedPages.add(nextPage);
            notEmpty.signal();
            lock.unlock();
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
