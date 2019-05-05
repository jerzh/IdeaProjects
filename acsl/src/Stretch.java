/*
Jeremy Zhou
Phillips Academy, Andover
Senior-3
 */

// I give up lol this is too annoying
// also I should've used a 2D array so it's easier to check if I go out of bounds oops

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class Stretch {
    public static void main(String[] args) {
        try {
            Scanner fi = new Scanner(new File("stretch.txt"));
            Integer[] inputs = (Integer[]) Arrays.stream(fi.nextLine().split(" ")).map(Integer::parseInt)
                    .toArray();
            int rows = inputs[0];
            int columns = inputs[1];
            int[] grid = new int[rows * columns];  // 0 = vacant, 1 = blocked
            int start = inputs[2];
            int numBlocked = inputs[3];
            for (int i = 0; i < numBlocked; i++) {
                grid[inputs[4 + i]] = 1;
            }

            int[][] pieces = {{0, 1, 2},
                    {0, columns, 2 * columns},
                    {0, columns, columns + 1},
                    {0, 1, columns + 1, 2 * columns + 1},
                    {0, 1, columns + 1, columns + 2}};
            int[] adj = {0, -1, 1, -columns, columns};

            int prev = start;
            int current = prev;
            int numPiece = 0;
            boolean finished = false;
            if (start % columns == 1) {
                while (!finished) {
                    boolean valid = false;
                    while (!valid) {
                        int[] piece = pieces[numPiece];
                        check:
                        for (int cell : piece) {
                            valid = true;
                            // check if location or adjacent locations are occupied
                            int location = current + cell;
                            for (int displacement : adj) {
                                int displaced = location + displacement;
                                if (0 <= displaced && displaced < grid.length && grid[displaced] == 1) {
                                    valid = false;
                                    break check;
                                }
                            }
                        }
                    }
                    finished = current % columns == 0;
                }
            } else if (start % columns == 0) {

            } else {
                System.err.println("what");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
