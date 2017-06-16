package org.kddm2;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kddm2.indexing.IndexingService;
import org.kddm2.indexing.InvalidWikiFileException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

public class TestIndexConfig {
    public final String indexDirectoryPath;
    public Directory luceneDirectory;
    public Set<String> vocabulary;
    public Resource dataSourceResource;

    public TestIndexConfig(String indexDirectoryPath, String dataSourcePath, Set<String> vocabulary) throws IOException {
        this.indexDirectoryPath = indexDirectoryPath;

        DefaultResourceLoader loader = new DefaultResourceLoader();
        dataSourceResource = loader.getResource(dataSourcePath);

        Files.createDirectories(Paths.get(indexDirectoryPath));
        luceneDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        this.vocabulary = vocabulary;
    }

    public void createIndexIfNotExists() throws InvalidWikiFileException, IOException {
        if (!DirectoryReader.indexExists(luceneDirectory)) {
            IndexingService indexingService = new IndexingService(luceneDirectory, vocabulary, dataSourceResource);
            indexingService.start();
        }
    }
}
