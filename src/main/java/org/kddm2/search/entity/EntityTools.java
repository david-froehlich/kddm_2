package org.kddm2.search.entity;

import org.apache.lucene.analysis.TokenStream;
import org.kddm2.Settings;
import org.kddm2.indexing.IndexingVocabulary;
import org.kddm2.lucene.IndexingUtils;
import org.kddm2.lucene.TokenOccurrence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

public class EntityTools {
    private static final Logger LOG = LoggerFactory.getLogger(EntityTools.class);

    private IndexingVocabulary vocabulary;
    private final int maxShingleSize;

    @Autowired
    public EntityTools(IndexingVocabulary vocabulary, int maxShingleSize) {
        this.vocabulary = vocabulary;
        this.maxShingleSize = maxShingleSize;
    }

    public Map<String, List<EntityCandidate>> groupEntitiesByText(List<EntityCandidate> candidates) {
        Map<String, List<EntityCandidate>> groupedEntities = new HashMap<>();

        for (EntityCandidate candidate : candidates) {
            String text = candidate.getCandidateText();
            if (!groupedEntities.containsKey(text)) {
                groupedEntities.put(text, new LinkedList<>());
            }
            groupedEntities.get(text).add(candidate);
        }

        return groupedEntities;
    }

    private float getMaxDocumentRelevanceForLinks(List<EntityLink> links) {
        float maxDocumentRelevance = 0.0f;
        for (EntityLink link : links) {
            EntityLinkTarget bestDocForLink = link.getTargets().get(0);
            if (bestDocForLink.getRelevance() > maxDocumentRelevance) {
                maxDocumentRelevance = bestDocForLink.getRelevance();
            }
        }
        return maxDocumentRelevance;
    }

    private float getMaxCandidateWeightForLinks(List<EntityLink> links) {
        float maxCandidateWeight = 0.0f;
        for (EntityLink link : links) {
            try {
                EntityCandidateWeighted entity = (EntityCandidateWeighted) link.getEntity();
                if (entity.getWeight() > maxCandidateWeight) {
                    maxCandidateWeight = entity.getWeight();
                }
            } catch (ClassCastException ex) {
                throw new IllegalArgumentException(
                        "trying to normalize weight of unweighted EntityCandidate");
            }
        }
        return maxCandidateWeight;
    }

    public List<EntityLink> cutoffCombinedWeightLinks(List<EntityLink> links, int maxLinkCount) {
        links.sort((left, right) -> Float.compare(right.getCombinedWeight(), left.getCombinedWeight()));
        links = links.subList(0, maxLinkCount);
        return links;
    }

    public List<EntityLink> calculateCombinedWeightsForEntityLinks(List<EntityLink> links, float candidateToDocumentWeightRatio) {
        float maxCandidateWeight = getMaxCandidateWeightForLinks(links);
        float maxDocumentRelevance = getMaxDocumentRelevanceForLinks(links);
        for (EntityLink link : links) {
            float normCW = ((EntityCandidateWeighted) link.getEntity()).getWeight() / maxCandidateWeight;
            float normDR = link.getTargets().get(0).getRelevance() / maxDocumentRelevance;
            link.setCombinedWeight(normCW * candidateToDocumentWeightRatio + normDR);
        }
        return links;
    }

    public List<EntityCandidate> identifyEntities(String content) {
        List<EntityCandidate> candidates = new ArrayList<>();
        try {
            Reader fileContentReader = new StringReader(content);
            TokenStream plainTokenizer = IndexingUtils.createPlaintextTokenizer(fileContentReader,
                    vocabulary.getVocabularySet(), maxShingleSize);
            List<TokenOccurrence> tokensInStream = IndexingUtils.getTokensInStream(plainTokenizer);

            for (TokenOccurrence occ : tokensInStream) {
                candidates.add(new EntityCandidate(occ.startOffset, occ.endOffset, content));
            }

        } catch (IOException e) {
            LOG.error("Error identifying entities", e);
        }
        return candidates;
    }
}
