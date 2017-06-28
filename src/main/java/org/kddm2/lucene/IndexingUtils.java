package org.kddm2.lucene;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.kddm2.indexing.WikiPage;
import org.kddm2.indexing.xml.WikiXmlReader;
import org.kddm2.search.entity.EntityWikiLinkExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class IndexingUtils {
    private static final Logger LOG = LoggerFactory.getLogger(IndexingUtils.class);
    private static final Pattern SIMPLE_NUMBER_REGEX = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static int getWordCount(Reader reader) {
        TokenStream tokenStream = IndexingUtils.createWikiTokenizer(reader, true);

        int wordCount = 0;
        try {
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                wordCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordCount;
    }

    public static Map<String, Integer> getTokenOccurrencesInStream(TokenStream stream) throws IOException {
        Map<String, Integer> tokensInStream = new HashMap<>();
        CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);
        stream.reset();

        while (stream.incrementToken()) {
            String term = charTermAttribute.toString();
            Integer count = tokensInStream.get(term);
            if (count == null) {
                count = 0;
            }
            tokensInStream.put(term, count + 1);
        }
        return tokensInStream;
    }

    //TODO test if disambiguation pages are linked more often
    //TODO parameters from frontend
    public static void extractVocabulary(InputStream stream, Set<String> vocabulary) throws IOException, XMLStreamException {
        int parsedPages = 0;

        WikiXmlReader reader = new WikiXmlReader(stream);
        CustomWikipediaTokenizer tokenizer = new CustomWikipediaTokenizer();
        TypeAttribute tAttr = tokenizer.addAttribute(TypeAttribute.class);
        CharTermAttribute cAttr = tokenizer.addAttribute(CharTermAttribute.class);
        tokenizer.reset();

        WikiPage page;
        while ((page = reader.getNextPage()) != null) {
            tokenizer.setReader(new StringReader(page.getText()));
            tokenizer.reset();

            while (tokenizer.incrementToken()) {
                String type = tAttr.type();
                if (type.equals(CustomWikipediaTokenizer.INTERNAL_LINK) || type.equals(CustomWikipediaTokenizer.INTERNAL_LINK_TARGET)) {
                    String linkText = cAttr.toString().trim();

                    if (linkText.length() >= 2 && !SIMPLE_NUMBER_REGEX.matcher(linkText).matches()) {
                        vocabulary.add(linkText.toLowerCase());
                    }
                }
            }
            if (++parsedPages % 500 == 0) {
                System.out.println("parsed " + parsedPages + " pages");
            }
            tokenizer.close();
        }
    }

    public static void readVocabulary(InputStream stream, Set<String> vocabulary) {
        try {
            LineIterator lineIterator = IOUtils.lineIterator(stream, StandardCharsets.UTF_8);
            while (lineIterator.hasNext()) {
                vocabulary.add(lineIterator.next().toLowerCase());
            }
        } catch (IOException e) {
            LOG.error("Error reading vocabulary file", e);
        }
    }

    public static List<TokenOccurrence> getTokensInStream(TokenStream stream) throws IOException {
        List<TokenOccurrence> tokens = new ArrayList<>();
        OffsetAttribute offsetAttribute = stream.addAttribute(OffsetAttribute.class);
        stream.reset();

        while (stream.incrementToken()) {
            tokens.add(new TokenOccurrence(offsetAttribute.startOffset(), offsetAttribute.endOffset()));
        }

        return tokens;
    }

    /**
     * Creates a plain text analyzer with the indexing filters returned by {@link IndexingUtils#createWikiTokenizer(Reader, Set, int)}.
     *
     * @param reader         The source to read from.
     * @param vocabulary     Only tokens and shingles in the vocabulary are kept.
     * @param maxShingleSize The maximum n-gram(shingle) size that is used to create new tokens.
     * @return The created TokenStream.
     */
    public static TokenStream createPlaintextTokenizer(Reader reader, Set<String> vocabulary, int maxShingleSize) {
        final StandardTokenizer src = new StandardTokenizer();
        src.setReader(reader);
        src.setMaxTokenLength(255);
        TokenStream tokenStream = new LowerCaseFilter(src);

        return createIndexFilters(tokenStream, vocabulary, maxShingleSize);
    }

    public static TokenStream createWikiTokenizer(Reader reader, boolean keepInternalLinks) {
        //TODO: use lucene Analyzers to re-use these token streams. This could improve performance
        CustomWikipediaTokenizer tokenizer = new CustomWikipediaTokenizer();
        tokenizer.setReader(reader);
        TokenStream tokenStream = new WikiReplacerTokenFilter(tokenizer, keepInternalLinks);
        return new LowerCaseFilter(tokenStream);
    }

    /**
     * Extracts links from wiki markup text.
     */
    public static EntityWikiLinkExtractor createEntityExtractionTokenStream(String wikiPageText) {
        CustomWikipediaTokenizer tokenizer = new CustomWikipediaTokenizer();
        tokenizer.setReader(new StringReader(wikiPageText));
        TokenStream tokenStream = new WikiReplacerTokenFilter(tokenizer, true);
        return new EntityWikiLinkExtractor(tokenStream, wikiPageText);
    }

    /**
     * Creates a Wiki syntax tokenizer that only keeps plain text in the wiki file. Also applies the
     * indexing filters returned by {@link IndexingUtils#createIndexFilters(TokenStream, Set, int)}}.
     *
     * @param reader         The source to read from.
     * @param vocabulary     Only tokens and shingles in the vocabulary are kept.
     * @param maxShingleSize The maximum n-gram(shingle) size that is used to create new tokens.
     * @return The wiki tokenizer token stream.
     */
    public static TokenStream createWikiTokenizer(Reader reader, Set<String> vocabulary, int maxShingleSize) {
        TokenStream tokenStream = createWikiTokenizer(reader, false);
        return createIndexFilters(tokenStream, vocabulary, maxShingleSize);
    }

    /**
     * Creates a Wiki syntax tokenizer that converts all the wiki text to plain text
     *
     * @param reader The source to read from.
     * @return The plain text of the wiki page.
     */
    public static String getWikiPlainText(Reader reader) throws IOException {
        Tokenizer wikipediaTokenizer = new CustomWikipediaTokenizer();
        wikipediaTokenizer.setReader(reader);
        TokenStream tokenStream = new WikiToPlaintextFilter(wikipediaTokenizer, true);
        StringBuilder plaintext = new StringBuilder();

        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);
        tokenStream.reset();

        boolean insideLink = false;
        String linkText = null;
        while (tokenStream.incrementToken()) {
            if (CustomWikipediaTokenizer.INTERNAL_LINK.equals(typeAttribute.type())) {
                insideLink = true;
                linkText = charTermAttribute.toString();
            } else if (CustomWikipediaTokenizer.INTERNAL_LINK_TARGET.equals(typeAttribute.type())) {
                // terminate existing link, if already inside it
                if (insideLink) {
                    plaintext.append(linkText);
                    plaintext.append(" ");
                }
                insideLink = true;
                linkText = charTermAttribute.toString();
            } else {
                if (insideLink) {
                    plaintext.append(linkText);
                    plaintext.append(" ");
                    insideLink = false;
                    linkText = null;
                }
                plaintext.append(charTermAttribute.toString());
                plaintext.append(" ");
            }
        }

        return plaintext.toString().trim().toLowerCase();
    }

    /**
     * Appends filters to the given TokenStream that create shingles and then filter everything not in the given vocabulary.
     *
     * @param source         The input TokenStream.
     * @param vocabulary     Only tokens and shingles in the vocabulary are kept.
     * @param maxShingleSize The maximum n-gram(shingle) size that is used to create new tokens.
     * @return The modified TokenStream.
     */
    private static TokenStream createIndexFilters(TokenStream source, Set<String> vocabulary, int maxShingleSize) {
        ShingleFilter shingleFilter = new ShingleFilter(source, maxShingleSize);
        return new VocabTokenFilter(shingleFilter, vocabulary);
    }
}
