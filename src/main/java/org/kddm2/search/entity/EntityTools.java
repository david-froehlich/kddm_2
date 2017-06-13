package org.kddm2.search.entity;

import org.apache.lucene.analysis.TokenStream;
import org.kddm2.Settings;
import org.kddm2.indexing.IndexHelper;
import org.kddm2.lucene.IndexingUtils;
import org.kddm2.lucene.TokenOccurrence;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

public class EntityTools {

    private IndexHelper indexHelper;
    private Set<String> vocabulary;

    public EntityTools(IndexHelper indexHelper, Set<String> vocabulary) {
        this.indexHelper = indexHelper;
        this.vocabulary = vocabulary;
    }

    public Map<String, List<EntityCandidate>> groupEntitiesByText(List<EntityCandidate> candidates) {
        Map<String, List<EntityCandidate>> groupedEntities = new HashMap<>();

        for (EntityCandidate candidate :
                candidates) {
            String text = candidate.getCandidateText();
            if(!groupedEntities.containsKey(text)) {
                groupedEntities.put(text, new LinkedList<>());
            }
            groupedEntities.get(text).add(candidate);
        }
        
        return groupedEntities;
    }


    public List<EntityCandidate> identifyEntities(String content) {
        List<EntityCandidate> candidates = new ArrayList<>();
        try {
            Reader fileContentReader = new StringReader(content);
            TokenStream plainTokenizer = IndexingUtils.createPlainTokenizer(fileContentReader, vocabulary, Settings.MAX_SHINGLE_SIZE);
            List<TokenOccurrence> tokensInStream = IndexingUtils.getTokensInStream(plainTokenizer);

            for (TokenOccurrence occ : tokensInStream) {
                candidates.add(new EntityCandidate(occ.startOffset, occ.endOffset, content));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return candidates;
    }

    @Deprecated
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

    @Deprecated
    public List<List<EntityCandidate>> identifyExclusiveEntities(String fileContent) {
        return findExclusiveCandidates(identifyEntities(fileContent));
    }
}
