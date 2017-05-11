# Task: Information Retrieval - Blog Search

```
Task: Provide a list of matching resources for a given piece of text. The goal
is to produce a ranked list of items relevant to a context.

Use-Case: Consider a user writing a text, for instance a blog post. While
typing the user is presented a list of suggested items, which might be relevant
or helpful.

Suggested data-sets: Same as previous task.

Framework: For processing of the text you might use: Sensium

Advanced: Identify Wikipedia concepts within the written text. For example, if
          the text contains the word Graz it should be linked to the corresponding
          Wikipedia page.

Questions about the topic: Roman Kern
```

# Goals

- Find similar/relevant documents
- Entity identification
- Entity linking

# Possible approaches

## Similar/relevant docs

- Create document-term matrix, apply fuzzy co-clustering
  - Find documents with similar probability distributions
  - Open question: How to get good ordering?
- Reuse EL information
  - apply on current doc
  - look for documents with similar links

Canopy clustering


## Entity identification

- Approach from Babelfy:
  - Apply POS tagging
  - Sequences of 5 words that contain:
    - at least 1 noun
    - contains substring of entity synonym
    

## Other approaches

- `dbspotlight`
- `tagme`

## Entity linking

- Create entity to category mapping from dataset
  - Find documents with entity
  - Extract document categories
    - Count occurrences for weight?
  - How to get categories?
    - Reuse Wikipedia categories
    - Apply clustering to get categories
- Find categories for current document 
  - Disambiguation using entities with similar categories
    - Ranking by number of occurrences?
- `Entity-Linking via Graph-Distance Minimization`?
	- no clustering required
	- graph of Wikipedia nodes (links are edges)
		- each entity has a list of candidates
		- choose set of candidates with minimal distance (hop-count)

- Build vector space model with documents and entities
  - apply clustering
  - minimize distance of entities


# Evaluation

- Compare with real Wikipedia article (similarity measure)


# Keep in mind

- Wikipedia has a list of entity-types that should not be linked:
[Overlinking/Underlinking](https://en.wikipedia.org/wiki/Wikipedia:Manual_of_Style/Linking#Overlinking_and_underlinking)
  - might be a good idea to consider these
- can we create an entity-database with Homonyms/synonyms from the wikipedia-db?


# sources:

[wikify](https://digital.library.unt.edu/ark:/67531/metadc31001/m2/1/high_res_d/Mihalcea-2007-Wikify-Linking_Documents_to_Encyclopedic.pdf)

