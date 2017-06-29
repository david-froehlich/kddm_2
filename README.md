# Information retrieval - Wikification

This is a web application that tries to add relevant links to Wikipedia pages
into any input text.

The approach we chose is to use Apache Lucene to create an inverted index that
we use for disambiguation and term ranking purposes. Therefore, it is necessary
to download the whole Wikipedia pages data dump in XML format and run the indexing
process on it before the wikification can be performed.

## Prerequisites

- Linux
  - We assume that `/tmp` exists and create the Lucene index and other temporary files there
- Java JDK 8 or later
- Gradle
- Bash (for the data download)

In case Linux is not available, some options are configurable using an
`application.yml` file in the project root. Not sure how well that actually
works though (untested).

## Running the demo

- Check out the source code
- Install
  - Java JDK 8 or later
  - Gradle
- Go to the data directory
  - run `bash download-dataset.sh`
  - this downloads the Simple English Wikipedia data dump XML file
- In the project root:
  - run `gradle run`
- Go to <http://localhost:8080>
  - Press the start indexing button
  - Wait until its done (Progress can be seen in the sidebar).
  - Now you can enter text in the text box and run the wikification.

## Code documentation

Here are some pointers on where to find important code parts. 

## Web interface

The front-end is just some Javascript and HTML. All requests to the server 
are made via a REST API.

Front-end files:

- [index.html](src/main/webapp/static/index.html)
- [script.js](src/main/webapp/static/js/script.js)

REST request handling:

- [MainController](src/main/java/org/kddm2/MainController.java)


## Indexing

- [IndexingService](src/main/java/org/kddm2/indexing/IndexingService.java)
- [WikiPageProducer](src/main/java/org/kddm2/indexing/WikiPageProducer.java)
- [WikiPageIndexer](src/main/java/org/kddm2/indexing/WikiPageIndexer.java)
- [WikiXmlReader](src/main/java/org/kddm2/indexing/xml/WikiXmlReader.java)
- [IndexingUtils](src/main/java/org/kddm2/lucene/IndexingUtils.java)
- [CustomWikipediaTokenizer](src/main/java/org/kddm2/lucene/CustomWikipediaTokenizer.java)
- [CustomWikipediaTokenizerImpl](src/main/jflex/WikipediaTokenizerImpl.jflex)

The indexing process is triggered via a REST request. It is controlled mainly
by the IndexingService. If a vocabulary file exists, it is read from the
configured location on the disk. Otherwise, a first pass over the XML file is
done to collect the vocabulary and write it back to disk.

Then, the IndexingService creates a WikiPageProducer, and a configurable number
of WikiPageIndexer instances. The WikiPageProducer uses a WikiXmlReader to
produce IndexingTasks and puts them into a BlockingQueue which is owned by the
IndexingService and shared with the WikiPageIndexer instances. These consume
the tasks from the queue, run the Lucene tokenizers and add the documents to
the index. During this process, synonyms and redirect pages are collected. At
the end of the indexing process, the IndexingService writes the collected
synonyms and redirect pages to the index.

IndexingUtils contains some utility functions used by the other parts. It is
also used to instantiate the Lucene tokenizers, because we did not realize we
should use Analyzers for this at the time we wrote all this code.

The CustomWikipediaTokenizer is based on the Lucene WikipediaTokenizer and has
been extensively adapted by us to deal with the many different version of Wiki
syntax and ignore most of the special content like tables, links, references,
image descriptions and so on.

## Entity Identification

- [EntityIdentifier](src/main/java/org/kddm2/search/entity/EntityIdentifier.java)
- [EntityWeightingKeyphraseness](src/main/java/org/kddm2/search/entity/EntityWeightingKeyphraseness.java)
- [EntityWeightingTFIDF](src/main/java/org/kddm2/search/entity/EntityWeightingTFIDF.java)
- [EntityTools](src/main/java/org/kddm2/search/entity/EntityTools.java)

The identification process is controlled by the EntityIdentifier. It uses
EntityTools to generate an initial list of entity candidates. Then, it applies
the EntityWeightingAlgorithm of choice to determine the weights and chooses
non-overlapping highest weight entities until the desired link rate is
reached.

## Entity Linking

- [EntityLinker](src/main/java/org/kddm2/search/entity/EntityLinker.java)

The EntityLinker takes a list of weighted candidates, builds Lucene queries for
them and returns a list of EntityLink instances. These links contain a weighted
list of possible target documents, as returned by the queries.

