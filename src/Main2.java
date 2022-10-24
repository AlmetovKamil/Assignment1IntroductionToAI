import java.io.File;
import java.io.FileNotFoundException;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main2 {

    public static void main(String[] args) {
        PriorityQueue<Integer> q = new PriorityQueue<>();
        q.offer(1);
        q.offer(2);
        q.offer(3);
        while (!q.isEmpty()) {
            System.out.println(q.poll());
        }
    }
}
