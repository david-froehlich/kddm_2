package org.kddm2.indexing;

import org.apache.lucene.analysis.TokenStream;
import org.junit.Test;
import org.kddm2.lucene.IndexingUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;

public class VocabTokenizerTest {

    @Test
    public void testBaseCase() throws IOException {
        Set<String> vocabulary = new HashSet<>();
        vocabulary.add("lorem");
        vocabulary.add("lorem ipsum");
        vocabulary.add("ipsum");
        vocabulary.add("dolor");

        //these are the terms that we expect to find in our unit-test
        Set<String> expectedResult = new HashSet<>(vocabulary);

        //this one we dont wanna find
        vocabulary.add("lorem ipsum mawp");

        Reader reader = new StringReader("Lorem Ipsum dolor. sit amet");

        TokenStream tokenStream = IndexingUtils.createPlaintextTokenize(reader, vocabulary, 3);
        Map<String, Integer> tokenOccurrencesInStream = IndexingUtils.getTokenOccurrencesInStream(tokenStream);

        assertEquals(expectedResult, tokenOccurrencesInStream.keySet());
    }
}
