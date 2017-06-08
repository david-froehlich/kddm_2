package org.kddm2.indexing;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;
import org.kddm2.lucene.VocabTokenizer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;

public class VocabTokenizerTest {

    @Test
    public void testBaseCase() throws IOException {
        Set<String> vocabulary = new HashSet<>();
        vocabulary.add("Lorem");
        vocabulary.add("Lorem Ipsum");
        vocabulary.add("Ipsum");
        vocabulary.add("dolor");

        //these are the terms that we expect to find in our unit-test
        Set<String> expectedResult = new HashSet<>(vocabulary);

        //this one we dont wanna find
        vocabulary.add("Lorem Ipsum mawp");

        Reader reader = new StringReader("Lorem Ipsum dolor. sit amet");
        VocabTokenizer tokenizer = new VocabTokenizer(reader, 2, vocabulary);

        Set<String> tokensInStream = new HashSet<>();

        while (tokenizer.incrementToken()) {
            CharTermAttribute attribute = tokenizer.getAttribute(CharTermAttribute.class);
            tokensInStream.add(attribute.toString());
        }

        assertEquals(expectedResult, tokensInStream);
    }
}
