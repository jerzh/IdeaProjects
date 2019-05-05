package NeuralNet;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

/*
todo: (1) fix adadelta (weights keep increasing without bound)
 */

public class NeuralNetwork {
    private static int[] nodeCount = {};
    private static int length = 0;

    public static void main(String[] args) {
        double[][] inputs = new double[0][0];
        double[][] expectedOutputs = new double[0][0];

        try {
            Scanner f = new Scanner(new File("neuralNetworkInfo.txt"));
            length = f.nextInt();

            nodeCount = new int[length];
            Scanner line = new Scanner(f.nextLine());
            for (int i = 0; i < length; i++) {
                nodeCount[i] = line.nextInt();
            }

            int n = f.nextInt();
            inputs = new double[n][nodeCount[0]];
            expectedOutputs = new double[n][nodeCount[length - 1]];

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < inputs[0].length; j++) {
                    inputs[i][j] = f.nextDouble();
                }
                for (int j = 0; j < expectedOutputs[0].length; j++) {
                    expectedOutputs[i][j] = f.nextDouble();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("# of nodes or # of data entries is incorrect");
        }

        // fill weights
        double[][][] weights = new double[length - 1][][];
        for (int i = 0; i < length - 1; i++) {
            double[][] weightsI = new double[nodeCount[i]][];
            for (int j = 0; j < nodeCount[i]; j++) {
                double[] weightsIJ = new double[nodeCount[i + 1]];
                for (int k = 0; k < nodeCount[i + 1]; k++) {
                    weightsIJ[k] = Math.random();
                }
                weightsI[j] = weightsIJ;
            }
            weights[i] = weightsI;
        }

        // fill biases
        double[][] biases = new double[length - 1][];
        for (int i = 0; i < length - 1; i++) {
            double[] biasesI = new double[nodeCount[i + 1]];
            for (int j = 0; j < nodeCount[i + 1]; j++) {
                biasesI[j] = Math.random();
            }
            biases[i] = biasesI;
        }

        /*
        w = weights
        b = biases
        z = pre-activation (layers collected together)
        y = post-activation (layers collected together)
        l = loss
        g = gradient
        e = expected value
        */

//        double[][][] eW = zero(weights);
//        double[][] eB = zero(biases);
//        double[][][] eGw = zero(weights);
//        double[][] eGb = zero(biases);

        // iterate through training data (definition of indices: z[i + 1] = z[i] * w[i] + b[i])
        for (int i = 0; i < inputs.length; i++) {
            double[] expectedOutput = expectedOutputs[i];

            // input and actual output included in layers
            double[][] z = new double[length][];
            double[][] y = new double[length][];
            y[0] = inputs[i];
            for (int j = 0; j < length - 1; j++) {
                z[j + 1] = add(multiply(y[j], weights[j]), biases[j]);
                y[j + 1] = activation(z[j + 1]);
            }

            double[] loss = loss(expectedOutput, y[length - 1]);

            double[][][] dLdY = new double[length][][],
                    dLdZ = new double[length][][],
                    dLdB = new double[length - 1][][];
            double[][][][] dLdW = new double[length - 1][][][];
            backPropagate(expectedOutput, z, y, weights, dLdY, dLdZ, dLdW, dLdB);

            // dLdW can have many values per variable; convert these into gradients with only 1 value per variable
            double[][][] gW = zero(weights);
            for (int j = 0; j < dLdW[0].length; j++) {
                for (int k = 0; k < length - 1; k++) {
                    gW[k] = add(gW[k], dLdW[k][j]);
                }
            }

            double[][] gB = zero(biases);
            for (int j = 0; j < dLdB[0].length; j++) {
                for (int k = 0; k < length - 1; k++) {
                    gB[k] = add(gB[k], dLdB[k][j]);
                }
            }

            stochasticGradientDescent(weights, biases, gW, gB);

            // for debugging and/or seeing what's going on
//            System.out.println("expected output: " + Arrays.toString(expectedOutput));
//            System.out.println("actual output: " + Arrays.toString(layers[length - 1]));
            System.out.println("loss: " + Arrays.toString(loss));
//            System.out.println("loss prime: " + Arrays.deepToString(dLdY[length - 1]));
//            System.out.println("weights: " + Arrays.deepToString(weights));
//            System.out.println("eW: " + Arrays.deepToString(eW));
//            System.out.println("dLdW: " + Arrays.deepToString(dLdW));
//            System.out.println("biases: " + Arrays.deepToString(biases));
            System.out.println();
        }

        // test new data
        while (true) {
            try {
                double[] input = new double[nodeCount[0]];
                Scanner s = new Scanner(System.in);

                for (int i = 0; i < nodeCount[0]; i++) {
                    System.out.print("input[" + i + "]: ");
                    input[i] = s.nextDouble();
                    s.nextLine();
                }

                double[][] layers = new double[length][];
                layers[0] = input;
                for (int j = 0; j < length - 1; j++) {
                    layers[j + 1] = activation(add(multiply(layers[j], weights[j]), biases[j]));
                }

                for (int i = 0; i < layers[length - 1].length; i++) {
                    DecimalFormat format = new DecimalFormat("0.############");
                    System.out.print(format.format(layers[length - 1][i]) + " ");
                }
                System.out.println();
            } catch (InputMismatchException e) {
                System.out.println("Exiting...");
                break;
            }
        }
    }

    private static double[] loss(double[] expectedOutput, double[] actualOutput) {
        assert expectedOutput.length == actualOutput.length : expectedOutput.length + " " + actualOutput.length;

        // logistic loss
        double[] loss = new double[expectedOutput.length];
        for (int i = 0; i < loss.length; i++) {
            double yyHat = -(expectedOutput[i] - 0.5) * (actualOutput[i] - 0.5);
            loss[i] = Math.log(1 + Math.exp(yyHat)) / Math.log(2);
        }
        return loss;
    }

    private static double[][] lossPrime(double[] expectedOutput, double[] actualOutput) {
        assert expectedOutput.length == actualOutput.length : expectedOutput.length + " " + actualOutput.length;

        // logistic loss
        double[][] loss = new double[expectedOutput.length][actualOutput.length];
        for (int i = 0; i < loss.length; i++) {
            double yHat = expectedOutput[i] - 0.5;
            double yyHat = -(expectedOutput[i] - 0.5) * (actualOutput[i] - 0.5);
            loss[i][i] = -yHat * Math.exp(yyHat) / (1 + Math.exp(yyHat)) / Math.log(2);
        }
        return loss;
    }

    private static double[] activation(double[] a) {
        // sigmoid
        double[] b = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = 1 / (1 + Math.exp(-a[i]));
        }
        return b;
    }

    private static double[][] activationPrime(double[] a) {
        // sigmoid
        double[][] b = new double[a.length][a.length];
        for (int i = 0; i < a.length; i++) {
            b[i][i] = Math.exp(-a[i]) / Math.pow((1 + Math.exp(-a[i])), 2);
        }
        return b;
    }

    private static void backPropagate(double[] expectedOutput, double[][] z, double[][] y, double[][][] weights,
                                      double[][][] dLdY, double[][][] dLdZ, double[][][][] dLdW, double[][][] dLdB) {
        for (int j = length - 1; j > 0; j--) {
            if (j == length - 1) {
                dLdY[j] = lossPrime(expectedOutput, y[length - 1]);
            } else {
                dLdY[j] = multiply(dLdZ[j + 1], transpose(weights[j]));
            }
            dLdZ[j] = multiply(dLdY[j], activationPrime(z[j]));
            dLdW[j - 1] = multiply(dLdZ[j], multiply3D(y[j - 1], identity(y[j].length)));
            dLdB[j - 1] = multiply(dLdZ[j], identity(y[j].length));
        }
    }

    private static void stochasticGradientDescent(double[][][] weights, double[][] biases, double[][][] gW, double[][] gB) {
        // initial learning rate
        double eta = -1;
        // reduce learning rate by a factor
        eta /= 1;
        for (int i = 0; i < length - 1; i++) {
            weights[i] = add(weights[i], scalarMultiply(eta, gW[i]));
            biases[i] = add(biases[i], scalarMultiply(eta, gB[i]));
        }
    }

    private static void adadelta(double[][][] weights, double[][] biases, double[][][] gW, double[][] gB,
                                 double[][][] eW, double[][] eB, double[][][] eGw, double[][] eGb) {
        double gamma = 0.9;
        double epsilon = 0.0000000001;

        for (int i = 0; i < length - 1; i++) {
            adjustE(eW[i], weights[i], gamma);
            adjustE(eB[i], biases[i], gamma);
            adjustE(eGw[i], gW[i], gamma);
            adjustE(eGb[i], gB[i], gamma);
        }

        double[][][] rmsW = sqrt(scalarAdd(epsilon, eW));
        double[][] rmsB = sqrt(scalarAdd(epsilon, eB));
        double[][][] rmsGw = sqrt(scalarAdd(epsilon, eGw));
        double[][] rmsGb = sqrt(scalarAdd(epsilon, eGb));

        double[][][] etaW = scalarMultiply(-1, scalarMultiply(scalarDivide(rmsW, rmsGw), gW));
        double[][] etaB = scalarMultiply(-1, scalarMultiply(scalarDivide(rmsB, rmsGb), gB));

        for (int i = 0; i < length - 1; i++) {
            weights[i] = add(weights[i], scalarMultiply(etaW[i], gW[i]));
            biases[i] = add(biases[i], scalarMultiply(etaB[i], gB[i]));
        }
    }

    private static void adjustE(double[] e, double[] next, double gamma) {
        for (int i = 0; i < e.length; i++) {
            e[i] = gamma * e[i] + (1 - gamma) * Math.pow(next[i], 2);
        }
    }
    private static void adjustE(double[][] e, double[][] next, double gamma) {
        for (int i = 0; i < e.length; i++) {
            adjustE(e[i], next[i], gamma);
        }
    }
    private static void adjustE(double[][][] e, double[][][] next, double gamma) {
        for (int i = 0; i < e.length; i++) {
            adjustE(e[i], next[i], gamma);
        }
    }
    private static void adjustE(double[][][][] e, double[][][][] next, double gamma) {
        for (int i = 0; i < e.length; i++) {
            adjustE(e[i], next[i], gamma);
        }
    }

    private static double[] to1DArray(double[][] a) {
        assert a[0].length == 1 : a[0].length;
        double[] b = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i][0];
        }
        return b;
    }

    private static double[][] to2DArray(double[] a) {
        double[][] b = new double[a.length][1];
        for (int i = 0; i < a.length; i++) {
            b[i][0] = a[i];
        }
        return b;
    }

    private static double[] zero(double[] a) {
        double[] b = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = 0;
        }
        return b;
    }
    private static double[][] zero(double[][] a) {
        double[][] b = new double[a.length][];
        for (int i = 0; i < a.length; i++) {
            b[i] = zero(a[i]);
        }
        return b;
    }
    private static double[][][] zero(double[][][] a) {
        double[][][] b = new double[a.length][][];
        for (int i = 0; i < a.length; i++) {
            b[i] = zero(a[i]);
        }
        return b;
    }

    private static double[][] identity(int size) {
        double[][] a = new double[size][size];
        for (int i = 0; i < size; i++) {
            a[i][i] = 1;
        }
        return a;
    }

    private static double[][] transpose(double[] a) {
        return transpose(to2DArray(a));
    }
    private static double[][] transpose(double[][] a) {
        double[][] b = new double[a[0].length][a.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                b[j][i] = a[i][j];
            }
        }
        return b;
    }

    private static double[] sqrt(double[] a) {
        double[] b = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = Math.sqrt(a[i]);
        }
        return b;
    }
    private static double[][] sqrt(double[][] a) {
        double[][] b = new double[a.length][];
        for (int i = 0; i < a.length; i++) {
            b[i] = sqrt(a[i]);
        }
        return b;
    }
    private static double[][][] sqrt(double[][][] a) {
        double[][][] b = new double[a.length][][];
        for (int i = 0; i < a.length; i++) {
            b[i] = sqrt(a[i]);
        }
        return b;
    }

    private static double[] add(double[] a, double[] b) {
        assert a.length == b.length : a.length + " " + b.length;
        double[] c = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i] + b[i];
        }
        return c;
    }
    private static double[][] add(double[][] a, double [][] b) {
        assert a.length == b.length && a[0].length == b[0].length : a.length + " " + b.length + " " + a[0].length + " " + b[0].length;
        double[][] c = new double[a.length][];
        for (int i = 0; i < a.length; i++) {
            c[i] = add(a[i], b[i]);
        }
        return c;
    }
    private static double[][][] add(double[][][] a, double [][][] b) {
        assert a.length == b.length && a[0].length == b[0].length && a[0][0].length == b[0][0].length
                : a.length + " " + b.length + " " + a[0].length + " " + b[0].length + " " + a[0][0].length + " " + b[0][0].length;
        double[][][] c = new double[a.length][][];
        for (int i = 0; i < a.length; i++) {
            c[i] = add(a[i], b[i]);
        }
        return c;
    }

    private static double[] scalarAdd(double a, double[] b) {
        double[] c = new double[b.length];
        for (int i = 0; i < b.length; i++) {
            c[i] = a + b[i];
        }
        return c;
    }
    private static double[][] scalarAdd(double a, double [][] b) {
        double[][] c = new double[b.length][];
        for (int i = 0; i < b.length; i++) {
            c[i] = scalarAdd(a, b[i]);
        }
        return c;
    }
    private static double[][][] scalarAdd(double a, double [][][] b) {
        double[][][] c = new double[b.length][][];
        for (int i = 0; i < b.length; i++) {
            c[i] = scalarAdd(a, b[i]);
        }
        return c;
    }

    private static double dotProduct(double[] a, double[] b) {
        assert a.length == b.length: a.length + " " + b.length;
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    private static double[] yArray(double[][] a, int y) {
        double[] b = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i][y];
        }
        return b;
    }

    private static double[] yzArray(double[][][] a, int y, int z) {
        double[] b = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i][y][z];
        }
        return b;
    }

    private static double[] multiply(double[] a, double[][] b) {
        assert a.length == b.length;
        double[] c = new double[b[0].length];
        for (int i = 0; i < b[0].length; i++) {
            c[i] = dotProduct(a, yArray(b, i));
        }
        return c;
    }
    private static double[][] multiply(double[][] a, double[][] b) {
        assert a[0].length == b.length : a[0].length + " " + b.length;
        double[][] c = new double[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b[0].length; j++) {
                c[i][j] = dotProduct(a[i], yArray(b, j));
            }
        }
        return c;
    }
    private static double[][][] multiply(double[][] a, double[][][] b) {
        assert a[0].length == b.length : a[0].length + " " + b.length;
        double[][][] c = new double[a.length][b[0].length][b[0][0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b[0].length; j++) {
                for (int k = 0; k < b[0][0].length; k++) {
                    c[i][j][k] = dotProduct(a[i], yzArray(b, j, k));
                }
            }
        }
        return c;
    }

    private static double[][][] multiply3D(double[] a, double[][] b) {
        double[][][] c = new double[b.length][a.length][b[0].length];
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < a.length; j++) {
                for (int k = 0; k < b[0].length; k++) {
                    c[i][j][k] = a[j] * b[i][k];
                }
            }
        }
        return c;
    }

    private static double[] scalarMultiply(double a, double[] b) {
        double[] c = new double[b.length];
        for (int i = 0; i < b.length; i++) {
            c[i] = a * b[i];
        }
        return c;
    }
    private static double[][] scalarMultiply(double a, double[][] b) {
        double[][] c = new double[b.length][b[0].length];
        for (int i = 0; i < b.length; i++) {
            c[i] = scalarMultiply(a, b[i]);
        }
        return c;
    }
    private static double[][][] scalarMultiply(double a, double[][][] b) {
        double[][][] c = new double[b.length][b[0].length][b[0][0].length];
        for (int i = 0; i < b.length; i++) {
            c[i] = scalarMultiply(a, b[i]);
        }
        return c;
    }
    private static double[] scalarMultiply(double[] a, double[] b) {
        assert a.length == b.length : a.length + " " + b.length;
        double[] c = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i] * b[i];
        }
        return c;
    }
    private static double[][] scalarMultiply(double[][] a, double[][] b) {
        assert a.length == b.length && a[0].length == b[0].length : a.length + " " + b.length + " " + a[0].length + " " + b[0].length;
        double[][] c = new double[a.length][];
        for (int i = 0; i < a.length; i++) {
            c[i] = scalarMultiply(a[i], b[i]);
        }
        return c;
    }
    private static double[][][] scalarMultiply(double[][][] a, double[][][] b) {
        assert a.length == b.length && a[0].length == b[0].length && a[0][0].length == b[0][0].length
                : a.length + " " + b.length + " " + a[0].length + " " + b[0].length + " " + a[0][0].length + " " + b[0][0].length;
        double[][][] c = new double[a.length][][];
        for (int i = 0; i < a.length; i++) {
            c[i] = scalarMultiply(a[i], b[i]);
        }
        return c;
    }

    private static double[] scalarDivide(double[] a, double[] b) {
        assert a.length == b.length : a.length + " " + b.length;
        double[] c = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i] / b[i];
        }
        return c;
    }
    private static double[][] scalarDivide(double[][] a, double[][] b) {
        assert a.length == b.length && a[0].length == b[0].length : a.length + " " + b.length + " " + a[0].length + " " + b[0].length;
        double[][] c = new double[a.length][];
        for (int i = 0; i < a.length; i++) {
            c[i] = scalarDivide(a[i], b[i]);
        }
        return c;
    }
    private static double[][][] scalarDivide(double[][][] a, double[][][] b) {
        assert a.length == b.length && a[0].length == b[0].length && a[0][0].length == b[0][0].length
                : a.length + " " + b.length + " " + a[0].length + " " + b[0].length + " " + a[0][0].length + " " + b[0][0].length;
        double[][][] c = new double[a.length][][];
        for (int i = 0; i < a.length; i++) {
            c[i] = scalarDivide(a[i], b[i]);
        }
        return c;
    }
}
