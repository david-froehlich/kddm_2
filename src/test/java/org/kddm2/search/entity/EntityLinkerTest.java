package org.kddm2.search.entity;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;
import org.kddm2.Settings;
import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.IndexingController;
import org.kddm2.indexing.WikiPage;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.kddm2.lucene.IndexingUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

public class EntityLinkerTest {
    private static final String INDEX_PATH = "/tmp/test_index/";
    private static final float CUTOFF_RATE = 0.06f;

    private Set<String> vocabulary;

    @Before
    public void setUpVocabulary() throws URISyntaxException {
        vocabulary = IndexingUtils.readDictionary(
                getClass().getClassLoader().getResource(Settings.VOCABULARY_PATH).toURI());
    }

    @Before
    public void setUpDirectory() throws IOException, XMLStreamException {
        IndexingController indexingController = new IndexingController(Paths.get(INDEX_PATH));
        indexingController.start();
    }

    @Test
    public void testSimpleLinking() throws Exception {
        // TODO: this is an integration test right now, add another simple test for just testing linking

        Directory directory = FSDirectory.open(Paths.get(INDEX_PATH));
        InputStream wikiInputStream = getClass().getClassLoader().getResourceAsStream(Settings.XML_FILE_PATH);
        WikiXmlReader wikiXmlReader = new WikiXmlReader(wikiInputStream, vocabulary);

        IndexStatsHelper indexHelper = new IndexStatsHelper(Paths.get(INDEX_PATH));
        EntityTools entityTools = new EntityTools(indexHelper, vocabulary);

        //TODO check more than one page
        WikiPage nextPage = wikiXmlReader.getNextPage();

        //TODO
        int maxShingleSize = 3;
        TokenStream wikiPlaintextTokenizer = IndexingUtils.createWikiTokenizer(
                new StringReader(nextPage.getText()), vocabulary, maxShingleSize);

        String plainText = IndexingUtils.tokenStreamToString(wikiPlaintextTokenizer);

        EntityWeightingAlgorithm algorithm = new EntityWeightingTFIDF(indexHelper, entityTools);
        EntityIdentifier identifier = new EntityIdentifier(algorithm, entityTools, CUTOFF_RATE);

        List<EntityCandidateWeighted> candidates = identifier.identifyEntities(plainText);

        EntityLinker entityLinker = new EntityLinker(directory);
        System.out.println(entityLinker.identifyLinksForCandidates(candidates));

    }

}