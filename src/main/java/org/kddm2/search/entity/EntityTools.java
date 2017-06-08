package org.kddm2.search.entity;

import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.kddm2.indexing.IndexHelper;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class EntityTools {

    private IndexHelper indexHelper;

    public EntityTools(IndexHelper indexHelper) {
        this.indexHelper = indexHelper;
    }

    private List<EntityCandidate> identifyEntities(String fileContent)
    {
        List<EntityCandidate> entityCandidates = new ArrayList<>();
        StringReader stringReader = new StringReader(fileContent);

        StandardTokenizer standardTokenizer = new StandardTokenizer();
        standardTokenizer.setReader(stringReader);

        try {
            while(standardTokenizer.incrementToken()) {
                System.out.println(standardTokenizer.getAttribute(CharTermAttribute.class).toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO: work in progress5
        return entityCandidates;
    }

    private List<List<EntityCandidate>> findExclusiveCandidates(List<EntityCandidate> candidates)
    {
        List<List<EntityCandidate>> exclusiveCandidates = new ArrayList<>();

        for (EntityCandidate candidateA : candidates) {
            List<EntityCandidate> exlusiveWithA = new ArrayList<>();
            exlusiveWithA.add(candidateA);
            for (EntityCandidate candidateB : candidates) {
                if (candidateA.overlaps(candidateB)) {
                    exlusiveWithA.add(candidateB);
                }
            }
            exclusiveCandidates.add(exlusiveWithA);
        }

        return exclusiveCandidates;
    }

    public List<List<EntityCandidate>> identifyExclusiveEntities(String fileContent)
    {
        return findExclusiveCandidates(identifyEntities(fileContent));
    }
}
