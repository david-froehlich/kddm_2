package org.kddm2.indexing;

public class IndexingTask {
    private WikiPage wikiPage;
    private boolean isEndOfStream;

    public IndexingTask(WikiPage wikiPage, boolean isEndOfStream) {
        this.wikiPage = wikiPage;
        this.isEndOfStream = isEndOfStream;
    }

    public WikiPage getWikiPage() {
        return wikiPage;
    }

    public boolean isEndOfStream() {
        return isEndOfStream;
    }
}
