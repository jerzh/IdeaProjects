package USACOTraining;

/*
ID: jeremy.11
LANG: JAVA
TASK: ariprog
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class ariprog {
    public static void main(String[] args) {
        try {
            BufferedReader fi = new BufferedReader(new FileReader("ariprog.in"));
            PrintWriter fo = new PrintWriter(new BufferedWriter(new FileWriter("ariprog.out")));
            int n = Integer.parseInt(fi.readLine());
            int m = Integer.parseInt(fi.readLine());
            int maxSeq = 2 * m * m;
            boolean none = true;

            boolean[] bisquares = new boolean[maxSeq + 1];
            for (int p = 0; p <= m; p++) {
                for (int q = 0; q <= p; q++) {
                    bisquares[p * p + q * q] = true;
                }
            }

            for (int b = 1; b <= maxSeq / (n - 1); b++) {
                if (n >= 4 && b % 4 != 0) {
                    continue;
                }
                ArrayList<Integer> posA = new ArrayList<>();
                for (int a = 0; a < b; a++) {
                    int count = 0;
                    for (int a1 = a; a1 <= maxSeq; a1 += b) {
                        if (!bisquares[a1]) {
                            count = 0;
                        } else {
                            count++;
                            if (count >= n) {
                                none = false;
                                posA.add(a1 - b * (n - 1));
                            }
                        }
                    }
                }
                Collections.sort(posA);
                for (Integer a : posA) {
                    fo.print(a + " " + b + "\n");
                }
            }

            if (none) {
                fo.print("NONE" + "\n");
            }
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
