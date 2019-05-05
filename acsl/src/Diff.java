/*
Jeremy Zhou
Phillips Academy, Andover
Senior-2
 */

import java.io.*;
import java.util.Scanner;

public class Diff {
    public static void main(String[] args) {
        try {
            Scanner f = new Scanner(new File("diff.txt"));
            for (int i = 0; i < 5; i++) {
                String a = f.nextLine();
                String b = f.nextLine();
                System.out.println(diff2(diff1(a, b), diff1(b, a)));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String diff1(String a, String b) {
        String[] words = a.split(" ");
        StringBuilder b0 = new StringBuilder(b);
        StringBuilder common = new StringBuilder();
        for (String word : words) {
            int i = b0.indexOf(word);
            if (i != -1) {
                common.append(word);
                b0.delete(i, i + word.length());
            }
        }
        return common.toString();
    }

    private static String diff2(String a, String b) {
        String[] characters = a.split("");
        StringBuilder b0 = new StringBuilder(b);
        StringBuilder common = new StringBuilder();
        for (String c : characters) {
            int i = b0.indexOf(c);
            if (i != -1) {
                common.append(c);
                b0.delete(0, i + 1);
            }
        }
        return common.toString();
    }
}
