package org.kddm2.wiki;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Test;
import org.kddm2.indexing.wiki.WikipediaTokenizer;

import java.io.IOException;
import java.io.StringReader;

public class WikipediaParserTest {

    @Test
    public void testSimpleLinks() throws Exception {
        String exampleText = "Hello this is a [[Testing wiki parsers|parser test]]";
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting or custom tokenizer");
        testTokenizer(customTokenizer, exampleText);
    }

    @Test
    public void testNestedLinks() throws Exception {
        String exampleText = "Hello this is a [[Testing nested links|nested [[Linking|link]] test]]";
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting or custom tokenizer");
        testTokenizer(customTokenizer, exampleText);
    }

    private void testTokenizer(Tokenizer tokenizer, String text) throws IOException {

        tokenizer.setReader(new StringReader(text));
        CharTermAttribute textAttribute =  tokenizer.addAttribute(CharTermAttribute.class);
        TypeAttribute typeAttribute = tokenizer.addAttribute(TypeAttribute.class);
        tokenizer.reset();
        while (tokenizer.incrementToken()) {
            System.out.format("'%s' [%s]\n", textAttribute.toString(), typeAttribute.type());
        }
    }
}
