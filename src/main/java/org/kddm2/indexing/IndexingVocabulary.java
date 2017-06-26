package org.kddm2.indexing;

import org.kddm2.lucene.IndexingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class IndexingVocabulary {
    private static final Logger LOG = LoggerFactory.getLogger(IndexingVocabulary.class);

    private Resource vocabularyResource;
    private Resource wikiXmlResource;
    private Set<String> vocabularySet = new HashSet<>();
    private boolean loaded = false;

    public IndexingVocabulary(Resource vocabularyResource, Resource wikiXmlResource) {
        this.vocabularyResource = vocabularyResource;
        this.wikiXmlResource = wikiXmlResource;
    }

    public IndexingVocabulary(Set<String> fixedVocabulary) {
        this.vocabularySet.addAll(fixedVocabulary);
        loaded = true;
    }

    public Set<String> getVocabularySet() {
        if (!loaded) {
            readOrExtractVocabulary();
        }
        return vocabularySet;
    }

    public void readOrExtractVocabulary() {
        if (loaded) {
            return;
        }
        try {
            if (vocabularyResource.exists() && vocabularyResource.isReadable()) {
                LOG.info("Reading vocabulary");
                IndexingUtils.readVocabulary(vocabularyResource.getInputStream(), vocabularySet);
            } else {
                LOG.info("Extracting vocabulary from " + wikiXmlResource.getFilename());
                IndexingUtils.extractVocabulary(wikiXmlResource.getInputStream(), vocabularySet);
                try (Writer w = new BufferedWriter(new FileWriter(vocabularyResource.getFile()))) {
                    for (String s : vocabularySet) {
                        w.write(s);
                        w.write('\n');
                    }
                }
            }
        } catch (IOException | XMLStreamException e) {
            LOG.error("Error while reading or extracting vocabulary", e);
        }
        LOG.info(vocabularySet.size() + " terms in vocabulary");
        loaded = true;
    }
}
