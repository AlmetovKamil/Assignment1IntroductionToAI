import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main2 {
    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("\\[\\d,\\d]\\s");
        String text = "[0,0] [4,7] [3,2] [6,4] [8,7] [0,6]\n";
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            System.out.println(text.substring(matcher.start(), matcher.end()));
        }
    }
}
