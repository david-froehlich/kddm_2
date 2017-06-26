package org.kddm2.wiki;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Assert;
import org.junit.Test;
import org.kddm2.indexing.wiki.WikipediaTokenizer;
import org.kddm2.lucene.IndexingUtils;

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
                new TokenizerResult("before", "<ALPHANUM>"),
                new TokenizerResult("Testing wiki parsers", "ilt"),
                new TokenizerResult("parser test", "il"),
                new TokenizerResult("after", "<ALPHANUM>")
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
                new TokenizerResult("before", "<ALPHANUM>"),
                new TokenizerResult("\"Adam's apple\" (disambiguation)", "ilt"),
                new TokenizerResult("link-text", "il"),
                new TokenizerResult("after", "<ALPHANUM>")
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
                new TokenizerResult("before", "<ALPHANUM>"),
                new TokenizerResult("Testing simple links", "ilt"),
                new TokenizerResult("after", "<ALPHANUM>")
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
                new TokenizerResult("before", "<ALPHANUM>"),
                new TokenizerResult("after", "<ALPHANUM>")
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
                new TokenizerResult("before", "<ALPHANUM>"),
                new TokenizerResult("after", "<ALPHANUM>")
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
                new TokenizerResult("before", "<ALPHANUM>"),
                new TokenizerResult("after", "<ALPHANUM>")
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
                new TokenizerResult("before", "<ALPHANUM>"),
                new TokenizerResult("after", "<ALPHANUM>")
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
                new TokenizerResult("before", "<ALPHANUM>"),
                new TokenizerResult("after", "<ALPHANUM>")
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
                new TokenizerResult("before", "<ALPHANUM>"),
                new TokenizerResult("after", "<ALPHANUM>")
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
                new TokenizerResult("before", "<ALPHANUM>"),
                new TokenizerResult("after", "<ALPHANUM>")
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
                new TokenizerResult("before", "<ALPHANUM>"),
                new TokenizerResult("after", "<ALPHANUM>")
        };
        Assert.assertArrayEquals(expected, tokenizerResults.toArray());

    }

    @Test
    public void testLinkWithinItalics() throws Exception {
        String exampleText = "''this page is about the first [[letter]] in the [[alphabet]].\n" +
                ": ''for the indefinite article, see [[article (grammar)]].''\n" +
                ": ''for other uses of a, see [[a (disambiguation)]]''\n";
        System.out.println("Testing on this text:\n  " + exampleText);
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        List<TokenizerResult> tokenizerResults = testTokenizer(customTokenizer, exampleText);
        Assert.assertEquals(tokenizerResults.get(7), new TokenizerResult("in", "i"));

    }

    @Test
    public void testWikitable() throws Exception {
        //TODO: this stuff does not work yet
        String exampleText = "    \"\\n\"+\n" +
                "            \"{| class=\\\"wikitable\\\"\\n\"+\n" +
                "            \"|- style=\\\"background-color:#EEEEEE; text-align:center;\\\"\\n\"+\n" +
                "            \"! Egyptian\\n\"+\n" +
                "            \"! Phoenician <br>''[[aleph]]''\\n\"+\n" +
                "            \"! Greek <br>''[[Alpha (letter)|Alpha]]''\\n\"+\n" +
                "            \"! Etruscan <br>A\\n\"+\n" +
                "            \"! Roman/Cyrillic <br>A\\n\"+\n" +
                "            \"|- style=\\\"background-color:white; text-align:center;\\\"\\n\"+\n" +
                "            \"|[[File:EgyptianA-01.svg|Egyptian hieroglyphic ox head]]\\n\"+\n" +
                "            \"|[[File:PhoenicianA-01.svg|Phoenician aleph]]\\n\"+\n" +
                "            \"|[[File:Alpha uc lc.svg|65px|Greek alpha]]\\n\"+\n" +
                "            \"|[[File:EtruscanA.svg|Etruscan A]]\\n\"+\n" +
                "            \"|[[File:RomanA-01.svg|Roman A]]\\n\"+\n" +
                "            \"|}\"";
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        List<TokenizerResult> tokenizerResults = testTokenizer(customTokenizer, exampleText);
        for (TokenizerResult res : tokenizerResults) {
            Assert.assertNotEquals("class", res.text);
            Assert.assertNotEquals("style", res.text);
        }
    }


    @Test
    public void testRefHTMLStyle() throws Exception {
        //TODO: this stuff does not work yet
        String exampleText = "The earliest letter 'A' has appeared was in the Phoenician alphabet's [[aleph]].<ref name=\\\"Britannica\\\"/> This symbol came from a simple picture of an [[ox]] head. ";
        org.apache.lucene.analysis.wikipedia.WikipediaTokenizer luceneTokenizer = new org.apache.lucene.analysis.wikipedia.WikipediaTokenizer();
        WikipediaTokenizer customTokenizer = new WikipediaTokenizer();
        System.out.println("\nTesting lucene tokenizer");
        testTokenizer(luceneTokenizer, exampleText);
        System.out.println("\nTesting our custom tokenizer");
        List<TokenizerResult> tokenizerResults = testTokenizer(customTokenizer, exampleText);
        for (TokenizerResult res : tokenizerResults) {
            Assert.assertNotEquals("ref", res.text);
        }
    }

    @Test
    public void testMultiLinkPlaintextExtraction() throws Exception {
        String exampleText = "'''Acceleration''' is a [[measure]] of how [[speed|fast]] [[velocity]] [[wikt:change|changes]]. Acceleration ";

        String result = IndexingUtils.getWikiPlainText(new StringReader(exampleText));
        String expected = "acceleration is a measure of how fast velocity changes acceleration";
        Assert.assertEquals(expected, result);
    }

    private List<TokenizerResult> testTokenizer(Tokenizer tokenizer, String text) throws IOException {
        return testTokenizer(tokenizer, text, true);
    }

    private List<TokenizerResult> testTokenizer(Tokenizer tokenizer, String text, boolean print) throws IOException {
        List<TokenizerResult> tokenizerResults = new ArrayList<>();
        tokenizer.setReader(new StringReader(text));
        CharTermAttribute textAttribute = tokenizer.addAttribute(CharTermAttribute.class);
        TypeAttribute typeAttribute = tokenizer.addAttribute(TypeAttribute.class);
        tokenizer.reset();
        while (tokenizer.incrementToken()) {
            tokenizerResults.add(new TokenizerResult(textAttribute.toString(), typeAttribute.type()));
            if (print) {
                System.out.format("'%s' [%s]\n", textAttribute.toString(), typeAttribute.type());
            }
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
