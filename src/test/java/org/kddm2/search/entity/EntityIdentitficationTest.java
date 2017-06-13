package org.kddm2.search.entity;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.junit.Before;
import org.junit.Test;
import org.kddm2.Settings;
import org.kddm2.indexing.IndexHelper;
import org.kddm2.indexing.IndexingController;
import org.kddm2.indexing.WikiPage;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.kddm2.lucene.IndexingUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Created by david on 6/13/17.
 */
public class EntityIdentitficationTest {
    private static final String INDEX_PATH = "/tmp/test_index/";

    @Before
    public void setUpDirectory() throws IOException, XMLStreamException {
        IndexingController indexingController = new IndexingController(Paths.get(INDEX_PATH));
        indexingController.start();
    }


    @Test
    public void testEntityIdentification() throws Exception {
        Set<String> vocabulary = IndexingUtils.readDictionary(
                getClass().getClassLoader().getResource(Settings.VOCABULARY_PATH).toURI());
        InputStream wikiInputStream = getClass().getClassLoader().getResourceAsStream(Settings.XML_FILE_PATH);
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream, vocabulary);

        IndexHelper indexHelper = new IndexHelper(Paths.get(INDEX_PATH));
        EntityTools entityTools = new EntityTools(indexHelper, vocabulary);

        //TODO check more than one page
        WikiPage nextPage = wikiXmlReader.getNextPage();

        //TODO
        int maxShingleSize = 3;
        TokenStream wikiPlaintextTokenizer = IndexingUtils.createWikiTokenizer(
                new StringReader(nextPage.getText()), vocabulary, maxShingleSize);

        //TODO check other algorithms
        EntityWeightingAlgorithm algorithm = new TfIDFEntityExtraction(indexHelper, entityTools);
        String plainText = IndexingUtils.tokenStreamToString(wikiPlaintextTokenizer);
        System.out.println(plainText);
        List<EntityCandidate> entityCandidates = entityTools.identifyEntities(plainText);
        List<WeightedEntityCandidate> o = algorithm.determineWeight(entityCandidates);
        Collections.sort(o, (left, right) -> (int)Math.signum(right.weight - left.weight));
        System.out.println(o);

    }
}
