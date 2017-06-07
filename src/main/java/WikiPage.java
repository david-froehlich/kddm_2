import java.io.Serializable;
import java.util.Map;

public class WikiPage implements Serializable {
    private String title;
    private String text;
    //terms that just occur as plain-text, not linked
    private Map<String, Integer> occuringTerms;
    private Map<String, Integer> linkedTerms;
    final boolean EOS;


    public WikiPage() {
        EOS = false;
    }

    private WikiPage(boolean EOS) {
        this.EOS = EOS;
    }

    public static WikiPage getEOSPage() {
        return new WikiPage(true);
    }

    public WikiPage(String title, String text, Map<String, Integer> terms, Map<String, Integer> linkedTerms) {
        this.title = title;
        this.text = text;
        this.occuringTerms = terms;
        this.linkedTerms = linkedTerms;
        EOS = false;
    }

    public Map<String, Integer> getLinkedTerms() {
        return linkedTerms;
    }

    public void setOccuringTerms(Map<String, Integer> occuringTerms) {
        this.occuringTerms = occuringTerms;
    }

    public void setLinkedTerms(Map<String, Integer> linkedTerms) {
        this.linkedTerms = linkedTerms;
    }

    public Map<String, Integer> getOccuringTerms() {
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

