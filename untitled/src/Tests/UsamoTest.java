package Tests;// A, M, O, S, U distinct digits 0-9
// USAMO is a perfect square
// O is not 0

import java.util.ArrayList;

public class UsamoTest {
    public static void main(String[] args) {
        int i = 10;
        int num = 0;
        while (num < 100000) {
            num = 100 * i * (i + 1) + 25;
            if (hasDistinctDigits(num)) {
                System.out.println(num);
            }
            i++;
        }
    }

    private static boolean hasDistinctDigits(int n) {
        // assume n has 5 digits
        ArrayList<Integer> digits = new ArrayList<>();
        while (n != 0) {
            int d = n % 10;
            if (digits.contains(d)) {
                return false;
            } else {
                digits.add(d);
            }
            n /= 10;
        }
        return true;
    }
}
