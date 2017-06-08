package org.kddm2.lucene;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class IndexingUtils {

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
     * @return
     */
    public static TokenStream createPlainTokenizer(Reader reader, Set<String> vocabulary, int maxShingleSize) {
        final StandardTokenizer src = new StandardTokenizer();
        src.setReader(reader);
        src.setMaxTokenLength(255);
        TokenStream tokenStream = new StandardFilter(src);
        tokenStream = new LowerCaseFilter(tokenStream);
        tokenStream = new StopFilter(tokenStream, StandardAnalyzer.ENGLISH_STOP_WORDS_SET);

        return createIndexFilters(tokenStream, vocabulary, maxShingleSize);
    }

    /**
     * Creates a Wiki syntax tokenizer that only keeps plain text in the wiki file. Also applies the
     * indexing filters returned by {@link IndexingUtils#createWikiTokenizer(Reader, Set, int)}.
     *
     * @param reader         The source to read from.
     * @param vocabulary     Only tokens and shingles in the vocabulary are kept.
     * @param maxShingleSize The maximum n-gram(shingle) size that is used to create new tokens.
     * @return
     */
    public static TokenStream createWikiTokenizer(Reader reader, Set<String> vocabulary, int maxShingleSize) {
        Tokenizer wikipediaTokenizer = new WikipediaTokenizer();
        wikipediaTokenizer.setReader(reader);
        TokenStream tokenStream = new WikiTokenFilter(wikipediaTokenizer);
        tokenStream = new LowerCaseFilter(tokenStream);

        return createIndexFilters(tokenStream, vocabulary, maxShingleSize);
    }

    /**
     * Appends filters to the given TokenStream that create shingles and then filter everything not in the given vocabulary.
     *
     * @param source         The input TokenStream
     * @param vocabulary     Only tokens and shingles in the vocabulary are kept.
     * @param maxShingleSize The maximum n-gram(shingle) size that is used to create new tokens.
     * @return
     */
    private static TokenStream createIndexFilters(TokenStream source, Set<String> vocabulary, int maxShingleSize) {
        ShingleFilter shingleFilter = new ShingleFilter(source, maxShingleSize);

        return new VocabTokenFilter(shingleFilter, vocabulary);
    }
}
