package org.kddm2.indexing;

public class WikiLink {
    private String pageId;
    private String linkText;

    public WikiLink(String pageId, String linkText) {
        this.pageId = pageId;
        this.linkText = linkText;
    }

    public String getPageId() {
        return pageId;
    }

    public String getLinkText() {
        return linkText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WikiLink wikiLink = (WikiLink) o;

        if (!pageId.equals(wikiLink.pageId)) return false;
        return linkText.equals(wikiLink.linkText);
    }

    @Override
    public int hashCode() {
        int result = pageId.hashCode();
        result = 31 * result + linkText.hashCode();
        return result;
    }
}
