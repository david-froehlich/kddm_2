import java.io.Serializable;
import java.util.Set;

public class WikiPage implements Serializable {
    private String title;
    private String text;
    //terms that just occur as plain-text, not linked
    private Set<String> occuringTerms;

    //TODO refactor to map, we need the count too...
    private Set<String> linkedTerms;

    public WikiPage() {
    }

    public WikiPage(String title, String text, Set<String> terms, Set<String> linkedTerms) {
        this.title = title;
        this.text = text;
        this.occuringTerms = terms;
        this.linkedTerms = linkedTerms;
    }

    public Set<String> getLinkedTerms() {
        return linkedTerms;
    }

    public void setOccuringTerms(Set<String> occuringTerms) {
        this.occuringTerms = occuringTerms;
    }

    public void setLinkedTerms(Set<String> linkedTerms) {
        this.linkedTerms = linkedTerms;
    }

    public Set<String> getOccuringTerms() {
        return occuringTerms;
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
                || this.getLinkedTerms() == null
                || this.getOccuringTerms() == null);
    }
}

