package org.kddm2.lucene;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizer;
import org.kddm2.search.entity.EntityExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class IndexingUtils {
    private static final Logger LOG = LoggerFactory.getLogger(IndexingUtils.class);

    public static int getWordCount(Reader reader) {
        TokenStream tokenStream = IndexingUtils.createWikiPlaintextTokenizer(reader);

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

    public static String tokenStreamToString(TokenStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);
        stream.reset();

        while (stream.incrementToken()) {
            builder.append(charTermAttribute.toString());
            builder.append(" ");
        }
        return builder.toString();
    }

    public static Set<String> readDictionary(URI fileURI) {
        Set<String> vocabulary = new HashSet<>();

        try (Stream<String> stream = Files.lines(
                Paths.get(fileURI))) {
            stream.forEach(s -> vocabulary.add(s.toLowerCase()));
        } catch (IOException e) {
            LOG.error("Error reading vocabulary file", e);
        }
        return vocabulary;
    }

    public static Set<String> readDictionary(InputStream stream) {
        Set<String> vocabulary = new HashSet<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                vocabulary.add(line);
            }
        } catch (IOException e) {
            LOG.error("Error reading vocabulary file", e);
        }

        return vocabulary;
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

        return createIndexFilters(tokenStream, vocabulary, maxShingleSize);
    }

    public static TokenStream createWikiPlaintextTokenizer(Reader reader) {
        Tokenizer wikipediaTokenizer = new WikipediaTokenizer();
        wikipediaTokenizer.setReader(reader);
        TokenStream tokenStream = new WikiReplacerTokenFilter(wikipediaTokenizer);
        return new LowerCaseFilter(tokenStream);
    }

    public static EntityExtractor createEntityExtractionTokenStream(String fullString) {
        Tokenizer wikipediaTokenizer = new WikipediaTokenizer();
        wikipediaTokenizer.setReader(new StringReader(fullString));
        TokenStream tokenStream = new WikiReplacerTokenFilter(wikipediaTokenizer);
        return new EntityExtractor(tokenStream, fullString);
    }

    public static EntityExtractor createEntityExtractionTokenStream(Reader reader, String fullString) {
        Tokenizer wikipediaTokenizer = new WikipediaTokenizer();
        wikipediaTokenizer.setReader(reader);
        TokenStream tokenStream = new WikiReplacerTokenFilter(wikipediaTokenizer);
        return new EntityExtractor(tokenStream, fullString);
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
        TokenStream tokenStream = createWikiPlaintextTokenizer(reader);
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
