/*
Jeremy Zhou
Phillips Academy, Andover
Senior-1
 */

import java.io.*;
import java.util.Scanner;

public class DigitReassembly {
    public static void main(String[] args) {
        try {
            Scanner f = new Scanner(new File("digitReassembly.txt"));
            while (f.hasNext()) {
                String digits = f.next();
//                System.out.println(digits + " " + digits.length());
                int n = Integer.parseInt(f.next());
//                System.out.println(n);
                StringBuilder padded = new StringBuilder(digits);
                for (int i = n; i > digits.length() % n; i--) {
                    padded.append("0");
                }
                digits = padded.toString();
                int sum = 0;
                for (int i = 0; i < digits.length(); i += n) {
                    sum += Integer.parseInt(digits.substring(i, i + n));
                }
                System.out.println(sum);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
