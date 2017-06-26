package org.kddm2;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kddm2.indexing.IndexingService;
import org.kddm2.indexing.IndexingVocabulary;
import org.kddm2.indexing.InvalidWikiFileException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestIndexConfig {
    public final String indexDirectoryPath;
    public Directory luceneDirectory;
    public IndexingVocabulary vocabulary;
    public Resource vocabularyResource;
    public Resource wikiXmlResource;

    public TestIndexConfig(String indexDirectoryPath, String dataSourcePath, String vocabularyPath) throws IOException {
        this.indexDirectoryPath = indexDirectoryPath;

        DefaultResourceLoader loader = new DefaultResourceLoader();
        wikiXmlResource = loader.getResource(dataSourcePath);
        vocabularyResource = loader.getResource(vocabularyPath);

        Files.createDirectories(Paths.get(indexDirectoryPath));
        luceneDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));

        this.vocabulary = new IndexingVocabulary(vocabularyResource, wikiXmlResource);
    }

    public void createIndexIfNotExists() throws InvalidWikiFileException, IOException {
        if (!DirectoryReader.indexExists(luceneDirectory)) {
            IndexingService indexingService = new IndexingService(luceneDirectory,
                    vocabulary, wikiXmlResource, IndexTestSuite.testDefaultSettings.getIndexingConsumerCount(),
                    IndexTestSuite.testDefaultSettings.getMaxShingleSize());
            indexingService.start();
        }
    }
}
