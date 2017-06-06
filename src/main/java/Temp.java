import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;

import java.io.IOException;

/**
 * Created by david on 6/7/17.
 */
public class Temp {

    public static void main(String[] args) throws IOException {
        Field field = new StringField("asdf", "fdsa", Field.Store.YES);
        FieldType fieldType = field.fieldType();
        IndexHelper helper = new IndexHelper(WikiIndexingController.DIRECTORY_PATH);
        String term_name = "is";
        IndexTermStats statsForDictTerm = helper.getStatsForDictTerm(term_name);
        System.out.println(statsForDictTerm.countLinkings);
        System.out.println(statsForDictTerm.countOccurences);
        System.out.println(statsForDictTerm.countLinkingDocuments);
        System.out.println(statsForDictTerm.countOccurenceDocuments);
    }
}
