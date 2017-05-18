import java.util.regex.Pattern;
public class WikiUtils {
    private static Pattern linkRegex;
    static {
        linkRegex = Pattern.compile("\\[\\[([\\w\\s]+)(:?\\|([\\w\\s]+?))?\\]\\]");
    }


}
