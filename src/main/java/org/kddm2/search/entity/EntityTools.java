package org.kddm2.search.entity;

import org.apache.lucene.analysis.TokenStream;
import org.kddm2.Settings;
import org.kddm2.indexing.IndexHelper;
import org.kddm2.lucene.IndexingUtils;
import org.kddm2.lucene.TokenOccurrence;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EntityTools {

    private IndexHelper indexHelper;
    private Set<String> vocabulary;

    public EntityTools(IndexHelper indexHelper, Set<String> vocabulary) {
        this.indexHelper = indexHelper;
        this.vocabulary = vocabulary;
    }

    private List<EntityCandidate> identifyEntities(String fileContent) {
        List<EntityCandidate> candidates = new ArrayList<>();
        try {
            Reader fileContentReader = new StringReader(fileContent);
            TokenStream plainTokenizer = IndexingUtils.createPlainTokenizer(fileContentReader, vocabulary, Settings.MAX_SHINGLE_SIZE);
            List<TokenOccurrence> tokensInStream = IndexingUtils.getTokensInStream(plainTokenizer);

            for (TokenOccurrence occ : tokensInStream) {
                candidates.add(new EntityCandidate(occ.startOffset, occ.endOffset, fileContent));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return candidates;
    }

    private List<List<EntityCandidate>> findExclusiveCandidates(List<EntityCandidate> candidates) {
        List<List<EntityCandidate>> exclusiveCandidates = new ArrayList<>();

        for (EntityCandidate candidateA : candidates) {
            List<EntityCandidate> exlusiveWithA = new ArrayList<>();
            exlusiveWithA.add(candidateA);
            for (EntityCandidate candidateB : candidates) {
                if (candidateB == candidateA) {
                    continue;
                }
                if (candidateA.overlaps(candidateB)) {
                    exlusiveWithA.add(candidateB);
                }
            }
            exclusiveCandidates.add(exlusiveWithA);
        }

        return exclusiveCandidates;
    }

    public List<List<EntityCandidate>> identifyExclusiveEntities(String fileContent) {
        return findExclusiveCandidates(identifyEntities(fileContent));
    }
}
