package org.kddm2.indexing;

import java.io.Serializable;


public class WikiPage implements Serializable {
    private String title;
    private String text;
    private String redirectTarget;

    public WikiPage() {
    }

    public WikiPage(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public boolean isRedirectPage()  {
        return this.redirectTarget != null;
    }

    public String getRedirectTarget() {
        return redirectTarget;
    }

    public void setRedirectTarget(String redirectTarget) {
        this.redirectTarget = redirectTarget;
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
        return !("".equalsIgnoreCase(this.getTitle()));
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

