package Tests;// E, L, M, N, O distinct digits 0-9
// ELMO is a multiple of NEMO
// O is not 0

public class ElmoNemoTest {
    public static void main(String[] args) {
        for (int o = 1; o < 10; o++) {
            for (int m = 0; m < 10; m++) {
                if (m == o) {
                    continue;
                }
                for (int e = 0; e < 10; e++) {
                    if (e == m || e == o) {
                        continue;
                    }
                    for (int n = 0; n < 10; n++) {
                        if (n == e || n == m || n == o) {
                            continue;
                        }
                        int oNum = 1000 * n + 100 * e + 10 * m + o;
                        int num = 2 * oNum;
                        while (num < 10000) {
                            int l = (num / 100) % 10;
                            if (num % 100 == 10 * m + o && num / 1000 == e && l != n && l != e && l != m && l != o) {
                                System.out.println(oNum + " " + num);
                            }
                            num += oNum;
                        }
                    }
                }
            }
        }
    }
}
