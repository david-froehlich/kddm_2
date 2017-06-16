package org.kddm2.indexing;

import java.io.Serializable;
import java.util.Map;


public class WikiPage implements Serializable {
    private String title;
    private String text;
    //terms that just occur as plain-text, not linked
    private Map<String, Integer> occurringTerms;
    private Map<WikiLink, Integer> wikiLinks;

    public WikiPage() {
    }

    public WikiPage(String title, String text, Map<String, Integer> terms, Map<WikiLink, Integer> wikiLinks) {
        this.title = title;
        this.text = text;
        this.occurringTerms = terms;
        this.wikiLinks = wikiLinks;
    }

    public Map<WikiLink, Integer> getWikiLinks() {
        return wikiLinks;
    }

    public void setOccurringTerms(Map<String, Integer> occurringTerms) {
        this.occurringTerms = occurringTerms;
    }

    public void setWikiLinks(Map<WikiLink, Integer> wikiLinks) {
        this.wikiLinks = wikiLinks;
    }

    public Map<String, Integer> getOccurringTerms() {
        return occurringTerms;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isValid() {
        return !("".equalsIgnoreCase(this.getTitle())
                || this.getWikiLinks() == null
                || this.getOccurringTerms() == null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WikiPage wikiPage = (WikiPage) o;

        if (title != null ? !title.equals(wikiPage.title) : wikiPage.title != null) return false;
        return text != null ? text.equals(wikiPage.text) : wikiPage.text == null;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }
}

