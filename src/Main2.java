import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main2 {
    public static boolean checkIfInputIsValid(String path) throws FileNotFoundException {
        File input = new File(path);
        Scanner in = new Scanner(input);
        in.useDelimiter("\n");
        String regEx = "^\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]";
        String regEx2 = "[12]";
        Pattern pattern = Pattern.compile(regEx);
        if (!in.hasNext(pattern)) {
            in.close();
            return false;
        }
        in.next();
        if (!in.hasNext(Pattern.compile(regEx2))) {
            in.close();
            return false;
        }
        in.close();
        return true;
    }
    public static void main(String[] args) throws FileNotFoundException {
        checkIfInputIsValid("input.txt");
    }
}
