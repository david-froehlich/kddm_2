package org.kddm2.wiki;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Assert;
import org.junit.Test;
import org.kddm2.indexing.wiki.WikipediaTokenizer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class WikipediaParserTest {

    @Test
    public void testLinkWithAlias() throws Exception {
        String exampleText = "before [[Testing wiki parsers|parser test]] after";
        System.out.println("Testing on this text:\n  " + exampleText);
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        List<TokenizerResult> tokenizerResults = testTokenizer(customTokenizer, exampleText);

        TokenizerResult[] expected = {
                new TokenizerResult("before","<ALPHANUM>"),
                new TokenizerResult("Testing wiki parsers","ilt"),
                new TokenizerResult("parser test","il"),
                new TokenizerResult("after","<ALPHANUM>")
        };

        Assert.assertArrayEquals(expected, tokenizerResults.toArray());
    }

    @Test
    public void testNestedLinks() throws Exception {
        String exampleText = "Hello this is a [[Testing nested links|nested [[Inner link target|inner link text]] still in outer link ]] notlink";
        System.out.println("Testing on this text:\n  " + exampleText);
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        testTokenizer(customTokenizer, exampleText);
    }

    @Test
    public void testFuckedUpTitle() throws Exception {
        String exampleText = "before [[\"Adam's apple\" (disambiguation)|link-text]] after";
        System.out.println("Testing on this text:\n  " + exampleText);
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        List<TokenizerResult> tokenizerResults = testTokenizer(customTokenizer, exampleText);

        TokenizerResult[] expected = {
                new TokenizerResult("before","<ALPHANUM>"),
                new TokenizerResult("\"Adam's apple\" (disambiguation)","ilt"),
                new TokenizerResult("link-text","il"),
                new TokenizerResult("after","<ALPHANUM>")
        };

        Assert.assertArrayEquals(expected, tokenizerResults.toArray());
    }

    @Test
    public void testOnlyLink() throws Exception {
        String exampleText = "before [[Testing simple links]] after";
        System.out.println("Testing on this text:\n  " + exampleText);
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        List<TokenizerResult> tokenizerResults = testTokenizer(customTokenizer, exampleText);

        TokenizerResult[] expected = {
                new TokenizerResult("before","<ALPHANUM>"),
                new TokenizerResult("Testing simple links","ilt"),
                new TokenizerResult("after","<ALPHANUM>")
        };

        Assert.assertArrayEquals(expected, tokenizerResults.toArray());
    }

    @Test
    public void testFuckedUpImageLinks1() throws Exception {
        String exampleText = "before [[file:bernhard plockhorst - schutzengel.jpg|thumb|right|225|an angel watching over two [[children]] hell starts here.]] after";
        System.out.println("Testing on this text:\n  " + exampleText);
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        List<TokenizerResult> tokenizerResults = testTokenizer(customTokenizer, exampleText);

        TokenizerResult[] expected = {
                new TokenizerResult("before","<ALPHANUM>"),
                new TokenizerResult("after","<ALPHANUM>")
        };

        Assert.assertArrayEquals(expected, tokenizerResults.toArray());

    }

    @Test
    public void testFuckedUpImageLinks2() throws Exception {
        String exampleText = "before [[File:Bobby Robson Statue Closeup.jpg|thumb|right|upright|Statue of Robson at [[Portman Road]]]] after";
        System.out.println("Testing on this text:\n  " + exampleText);
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        List<TokenizerResult> tokenizerResults = testTokenizer(customTokenizer, exampleText);
        Assert.assertEquals(2, tokenizerResults.size());

        TokenizerResult[] expected = {
                new TokenizerResult("before","<ALPHANUM>"),
                new TokenizerResult("after","<ALPHANUM>")
        };

        Assert.assertArrayEquals(expected, tokenizerResults.toArray());
    }

    @Test
    public void testCitations() throws Exception {
        String exampleText = "before {{admin|Kennedy|(former bureaucrat)}} after";
        System.out.println("Testing on this text:\n  " + exampleText);
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        List<TokenizerResult> tokenizerResults = testTokenizer(customTokenizer, exampleText);
        Assert.assertEquals(2, tokenizerResults.size());

        TokenizerResult[] expected = {
                new TokenizerResult("before","<ALPHANUM>"),
                new TokenizerResult("after","<ALPHANUM>")
        };

        Assert.assertArrayEquals(expected, tokenizerResults.toArray());
    }

    @Test
    public void testSpecialLinks() throws Exception {
        String exampleText = "before [[Special:Contributions/46.18.183.34|46.18.183.34]] after";
        System.out.println("Testing on this text:\n  " + exampleText);
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        List<TokenizerResult> tokenizerResults = testTokenizer(customTokenizer, exampleText);
        Assert.assertEquals(2, tokenizerResults.size());

        TokenizerResult[] expected = {
                new TokenizerResult("before","<ALPHANUM>"),
                new TokenizerResult("after","<ALPHANUM>")
        };

        Assert.assertArrayEquals(expected, tokenizerResults.toArray());
    }

    @Test
    public void testCategoryLinks() throws Exception {
        String exampleText = "before [[Category:Basic English 850 words]] after";
        System.out.println("Testing on this text:\n  " + exampleText);
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        List<TokenizerResult> tokenizerResults = testTokenizer(customTokenizer, exampleText);
        Assert.assertEquals(2, tokenizerResults.size());

        TokenizerResult[] expected = {
                new TokenizerResult("before","<ALPHANUM>"),
                new TokenizerResult("after","<ALPHANUM>")
        };

        Assert.assertArrayEquals(expected, tokenizerResults.toArray());
    }


    @Test
    public void testReflinks() throws Exception {
        String exampleText = "before {{Reflist|refs=\n" +
                "&lt;ref name=&quot;Britannica&quot;&gt;&quot;A&quot;, &quot;Encyclopaedia Britannica&quot;, Volume 1, 1962. p.1.&lt;/ref&gt; \n" +
                "}}\n after";
        System.out.println("Testing on this text:\n  " + exampleText);
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        List<TokenizerResult> tokenizerResults = testTokenizer(customTokenizer, exampleText);

        TokenizerResult[] expected = {
                new TokenizerResult("before","<ALPHANUM>"),
                new TokenizerResult("after","<ALPHANUM>")
        };

        Assert.assertArrayEquals(expected, tokenizerResults.toArray());
    }

    @Test
    public void testFuckedUpImageLinks3() throws Exception {
        String exampleText = "before [[file:bernhard plockhorst - schutzengel.jpg|thumb|right|225|an angel watching over two [[children|gremlins]] hell starts here.]] after";
        System.out.println("Testing on this text:\n  " + exampleText);
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        List<TokenizerResult> tokenizerResults = testTokenizer(customTokenizer, exampleText);

        TokenizerResult[] expected = {
                new TokenizerResult("before","<ALPHANUM>"),
                new TokenizerResult("after","<ALPHANUM>")
        };
        Assert.assertArrayEquals(expected, tokenizerResults.toArray());

    }

    @Test
    public void testFuckedUpImageLinks4() throws Exception {
        String exampleText = "before [[Image:bernhard plockhorst - schutzengel.jpg|thumb|right|225|an angel watching over two [[children|gremlins]] hell starts here.]] after";
        System.out.println("Testing on this text:\n  " + exampleText);
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        List<TokenizerResult> tokenizerResults = testTokenizer(customTokenizer, exampleText);

        TokenizerResult[] expected = {
                new TokenizerResult("before","<ALPHANUM>"),
                new TokenizerResult("after","<ALPHANUM>")
        };
        Assert.assertArrayEquals(expected, tokenizerResults.toArray());

    }

    private List<TokenizerResult> testTokenizer(Tokenizer tokenizer, String text) throws IOException {
        List<TokenizerResult> tokenizerResults = new ArrayList<>();
        tokenizer.setReader(new StringReader(text));
        CharTermAttribute textAttribute =  tokenizer.addAttribute(CharTermAttribute.class);
        TypeAttribute typeAttribute = tokenizer.addAttribute(TypeAttribute.class);
        tokenizer.reset();
        while (tokenizer.incrementToken()) {
            tokenizerResults.add(new TokenizerResult(textAttribute.toString(), typeAttribute.type()));
            System.out.format("'%s' [%s]\n", textAttribute.toString(), typeAttribute.type());
        }
        return tokenizerResults;
    }

    private class TokenizerResult {
        final String text;
        final String type;

        TokenizerResult(String text, String type) {
            this.text = text;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TokenizerResult that = (TokenizerResult) o;

            if (text != null ? !text.equals(that.text) : that.text != null) {
                return false;
            }
            return type != null ? type.equals(that.type) : that.type == null;
        }

        @Override
        public int hashCode() {
            int result = text != null ? text.hashCode() : 0;
            result = 31 * result + (type != null ? type.hashCode() : 0);
            return result;
        }
    }
}
