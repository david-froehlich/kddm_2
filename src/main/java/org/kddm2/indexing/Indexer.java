package org.kddm2.indexing;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Paths;

public class Indexer {
    private static final String INDEX_DIRECTORY = "/tmp/lucene_dir";

    public static void main(String[] args) throws IOException, XMLStreamException {
        long start = System.currentTimeMillis();
        IndexingController indexingController = new IndexingController(Paths.get(INDEX_DIRECTORY));
        indexingController.start();
        System.out.println("Indexing took " + (System.currentTimeMillis() - start) / 1000 + " seconds");
    }
}
