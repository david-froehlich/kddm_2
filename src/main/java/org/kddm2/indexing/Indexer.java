package org.kddm2.indexing;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class Indexer {

    public static void main(String[] args) throws IOException, XMLStreamException {
        long start = System.currentTimeMillis();
        IndexingController indexingController = new IndexingController();
        indexingController.start();
        System.out.println("Indexing took " + (System.currentTimeMillis() - start) / 1000 + " seconds");
    }
}
