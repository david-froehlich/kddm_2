import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizer;
import org.apache.lucene.util.Attribute;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class WikipediaFilter extends FilteringTokenFilter {
    public WikipediaFilter(TokenStream in) {
        super(in);
    }

    @Override
    protected boolean accept() throws IOException {
        TypeAttribute tAttr = this.getAttribute(TypeAttribute.class);
        String type = tAttr.type();
        return type.equals("<ALPHANUM>");
    }
}

public class VocabTokenizer extends FilteringTokenFilter {
    private Set<String> vocabulary;

    private static TokenStream initTokenizer(Reader reader, int max_n) throws IOException {
        Tokenizer tokenizer = new WikipediaTokenizer();
        tokenizer.setReader(reader);

        WikipediaFilter filter = new WikipediaFilter(tokenizer);

        ShingleFilter shingleFilter = new ShingleFilter(filter, 2, max_n);
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
        String potentialTerm = attribute.toString();
        return this.vocabulary.contains(potentialTerm);
    }
}
