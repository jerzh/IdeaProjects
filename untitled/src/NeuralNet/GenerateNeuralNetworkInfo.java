package NeuralNet;

import java.io.*;

public class GenerateNeuralNetworkInfo {
    public static void main(String[] args) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("neuralNetworkInfo.txt")));

        int[] nodeCount = {1, 3, 1};
        out.print(nodeCount.length + " ");
        for (int e : nodeCount) {
            out.print(e + " ");
        }
        out.println();

        int amtData = 10000;
        out.println(amtData);

        for (int i = 0; i < amtData; i++) {
            double x = Math.random();
//            double y = Math.random();

            // make sure these match nodeCount
            double[] input = new double[]{x};
            double[] output = new double[]{Math.round(x)};

            for (double e : input) {
                out.print(e + " ");
            }
            for (double e : output) {
                out.print(e + " ");
            }
            out.println();
        }
        System.out.println("Info generated successfully.");
        out.close();
    }
}
