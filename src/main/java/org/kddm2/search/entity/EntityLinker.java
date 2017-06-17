package org.kddm2.search.entity;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.kddm2.Settings;
import org.kddm2.indexing.InvalidIndexException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntityLinker {
    private static final Logger LOG = LoggerFactory.getLogger(EntityLinker.class);
    private final Directory indexDirectory;
    DirectoryReader directoryReader;
    private IndexSearcher searcher;

    @Autowired
    public EntityLinker(Directory indexDirectory) throws IOException {
        this.indexDirectory = indexDirectory;
    }

    private Query createContextQuery(String luceneFieldName, List<EntityCandidateWeighted> context) {
        BooleanQuery.Builder linkedQueryBuilder = new BooleanQuery.Builder();

        for (EntityCandidateWeighted contextCandidate : context) {
            String candidateText = contextCandidate.getCandidateText();
            // TODO: check if weights are/should be normalized for boosting
            linkedQueryBuilder.add(new BoostQuery(new TermQuery(new Term(luceneFieldName, candidateText)),
                            contextCandidate.getWeight()),
                    BooleanClause.Occur.SHOULD);
        }
        return linkedQueryBuilder.build();
    }

    public List<EntityLink> identifyLinksForCandidates(List<EntityCandidateWeighted> candidates) throws InvalidIndexException {
        if (directoryReader == null || searcher == null) {
            try {
                directoryReader = DirectoryReader.open(indexDirectory);
                searcher = new IndexSearcher(directoryReader);
            } catch (IOException e) {
                throw new InvalidIndexException("Cannot wikify text, invalid lucene index", e);
            }
        }
        // construct query for all candidates
        // this query represents the context of the current document
        Query contextQueryLinked = createContextQuery(Settings.TERM_LINKING_FIELD_NAME, candidates);
        Query contextQueryOccurrence = createContextQuery(Settings.TERM_OCCURENCE_FIELD_NAME, candidates);
        // TODO: make configurable
        float linkedPreference = 0.75f;

        contextQueryLinked = new BoostQuery(contextQueryLinked, linkedPreference);
        contextQueryOccurrence = new BoostQuery(contextQueryOccurrence, 1.f - linkedPreference);

        List<EntityLink> resultingLinks = new ArrayList<>();

        for (EntityCandidateWeighted candidate : candidates) {
            String candidateText = candidate.getCandidateText();
            List<EntityDocument> relevantDocuments = new ArrayList<>();
            // query to find synonym documents
            Query synonymQuery = new TermQuery(new Term(Settings.SYNONYMS_FIELD_NAME, candidateText));

            try {
                Query finalQuery = new BooleanQuery.Builder().add(synonymQuery, BooleanClause.Occur.MUST)
                        .add(contextQueryLinked, BooleanClause.Occur.SHOULD)
                        .add(contextQueryOccurrence, BooleanClause.Occur.SHOULD).build();

                TopDocs searchResults = searcher.search(finalQuery, 10);
                Set<String> fieldsToRetrieve = new HashSet<>();
                fieldsToRetrieve.add(Settings.DOCUMENT_ID_FIELD_NAME);

                for (int i = 0; i < searchResults.scoreDocs.length; i++) {
                    int luceneDocId = searchResults.scoreDocs[i].doc;
                    Document doc = searcher.doc(luceneDocId, fieldsToRetrieve);
                    String[] docIdValues = doc.getValues(Settings.DOCUMENT_ID_FIELD_NAME);
                    // TODO: better exceptions
                    if (docIdValues.length == 0) {
                        throw new RuntimeException("Error: no document id found, something is wrong with the index");
                    } else if (docIdValues.length != 1) {
                        throw new RuntimeException("Error: multiple document ids found, something is wrong with the index");
                    }
                    relevantDocuments.add(new EntityDocument(docIdValues[0], luceneDocId, searchResults.scoreDocs[i].score));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (relevantDocuments.isEmpty()) {
                continue;
            }
            relevantDocuments.sort((left, right) -> Float.compare(right.getRelevance(), left.getRelevance()));
            EntityLink link = new EntityLink(candidate, relevantDocuments);
            resultingLinks.add(link);
        }
        return resultingLinks;
    }
}

