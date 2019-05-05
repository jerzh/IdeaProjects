package Tests;

// attempting to find information about f(n) = n / # divisors of n

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ProposalTest {
    private ArrayList<Double> outputs = new ArrayList<>();
    private JFrame frame;
    private JPanel panel;
    private int hFactor = 1;

    public static void main(String[] args) {
        new ProposalTest().go();
    }

    private void go() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        panel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int height = this.getHeight();
                int vFactor = 1;

                double prevOutput, output = outputs.get(0);
                for (int i = 1; i < outputs.size(); i++) {
                    prevOutput = output;
                    output = outputs.get(i);
                    g.drawLine(hFactor * (i - 1), height - (int) (vFactor * prevOutput), hFactor * i, height - (int) (vFactor * output));
                }
            }
        };

        frame.getContentPane().add(panel);

        frame.setPreferredSize(new Dimension(500, 500));
        frame.pack();
        frame.setVisible(true);

        run();
    }

    private void run() {
        outputs.add((double) 0);
        for (int input = 1; input < panel.getWidth() / hFactor; input++) {
            double output = (double) input / numDivisors(input);
            outputs.add(output);

            try {
                frame.repaint();
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static int numDivisors(int n) {
        int counter = 0;
        double sqrt = Math.sqrt(n);
        for (int i = 1; i < sqrt; i++) {
            if (n % i == 0) {
                counter++;
            }
        }

        counter *= 2;

        if (sqrt - (int) sqrt == 0) {
            counter++;
        }

        return counter;
    }
}
