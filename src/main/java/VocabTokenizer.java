import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

public class VocabTokenizer extends FilteringTokenFilter {
    private Set<String> vocabulary;

    private static TokenStream initTokenizer(Reader reader, int max_n) throws IOException {
        StandardTokenizer tokenizer = new StandardTokenizer();
        tokenizer.setReader(reader);

        ShingleFilter shingleFilter = new ShingleFilter(tokenizer, 2, max_n);
        shingleFilter.addAttribute(CharTermAttribute.class);
        shingleFilter.reset();
        return shingleFilter;
    }

    public VocabTokenizer(Reader reader, int max_n, Set<String> vocabulary) throws IOException {
        super(initTokenizer(reader, max_n));
        this.vocabulary = vocabulary;
    }

    public Set<String> getTokensInStream() throws IOException {
        Set<String> tokensInStream = new HashSet<>();

        while(this.incrementToken()) {
            CharTermAttribute attribute = this.getAttribute(CharTermAttribute.class);
            tokensInStream.add(attribute.toString());
        }
        return tokensInStream;
    }

    @Override
    protected boolean accept() throws IOException {
        CharTermAttribute attribute = this.getAttribute(CharTermAttribute.class);
        return this.vocabulary.contains(attribute.toString());
    }
}
