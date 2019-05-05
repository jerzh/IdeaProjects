import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import static java.lang.Math.*;

/*
todo: (2) find out why total momentum is not conserved when objects combine
todo: (1) add total angular momentum
bug: sometimes an object randomly bounces off super quickly
 */

public class PhysicsSimulation {
    private static int width = 1000, height = 1080, border = 10, num;
    private static double dBlackHole = 10000, epsilon = 0.0000000001, totalM, totalPx, totalPy, totalKE;
    private JFrame frame;
    private Container contentPane;
    private ArrayList<Object> objects = new ArrayList<>();
    private ArrayList<Object> toRemove = new ArrayList<>();
    private ArrayList<Object> toAdd = new ArrayList<>();

    public static void main(String[] args) {
        new PhysicsSimulation().go();
    }

    private void go() {
        frame = new JFrame("Test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        int vRange = 2; // random scattering
        for (int i = 0; i < 100; i++) {
            double d = 0.1;
            double m = 10;
            double x = random() * width;
            double y = random() * height;
            double vx = random() * 2 * vRange - vRange;
            double vy = random() * 2 * vRange - vRange;
            double elasticity = 1;
            objects.add(new Object(d, m, x, y, vx, vy, elasticity));
        }

        /*
        objects.add(new Object(0.1, 10, 50, 50, 10, 10, 1)); // one object

        objects.add(new Object(0.1, 10, 500, 50, -1, 0, 1)); // two objects
        objects.add(new Object(0.1, 20, 50, 500, 0, -1, 1));

        int vRange = 2; // random scattering
        for (int i = 0; i < 100; i++) {
            double d = 0.02;
            double m = 10;
            double x = random() * width;
            double y = random() * height;
            double vx = random() * 2 * vRange - vRange;
            double vy = random() * 2 * vRange - vRange;
            double elasticity = 1;
            objects.add(new Object(d, m, x, y, vx, vy, elasticity));
        }

        objects.add(new Object(dBlackHole, 1000, width / 2, height / 2, 0, 0, 0)); // black hole

        int vRange = 1; // random charge scattering
        for (int i = 0; i < 100; i++) {
            double d = 0.02;
            double m = 1;
            double x = random() * width;
            double y = random() * height;
            double vx = random() * 2 * vRange - vRange;
            double vy = random() * 2 * vRange - vRange;
            double elasticity = 0;
            double q = (int) (Math.random() * 2) == 0 ? 2 : 2;
            objects.add(new Charge(d, m, x, y, vx, vy, elasticity, q));
        }
        */

        JPanel panel = new JPanel() {
            public void paintComponent(Graphics g) {
                g.setColor(Color.BLUE);
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
                g.setColor(Color.WHITE);
                g.fillRect(border, border, this.getWidth() - 2 * border, this.getHeight() - 2 * border);
                try {
                    for (Object o : objects) {
                        if (o.getM() > 0) {
                            g.setColor(Color.BLACK);
                        } else {
                            g.setColor(Color.GREEN);
                        }
                        g.fillOval(((int) (o.getX() - o.getR())), ((int) (o.getY() - o.getR())),
                                ((int) (2 * o.getR())), ((int) (2 * o.getR())));
                    }
                } catch (ConcurrentModificationException e) {
                    // e.printStackTrace();
                }
                g.setColor(Color.BLACK);
                int y = 30;
                g.drawString("number of objects: " + num, 20, y);
                y += 10;
                g.drawString("total m: " + round(totalM), 20, y);
                y += 10;
                g.drawString("total p (x): " + round(totalPx), 20, y);
                y += 10;
                g.drawString("total p (y): " + round(totalPy), 20, y);
                y += 10;
                g.drawString("total KE: " + round(totalKE), 20, y);
            }
        };

        contentPane = frame.getContentPane();
        contentPane.add(BorderLayout.CENTER, panel);

        frame.setVisible(true);
        frame.setResizable(true);
        frame.setSize(width, height);
        moveIt();
    }

    private void moveIt() {
        while (true) {
            toAdd = new ArrayList<>();
            for (int i = 0; i < objects.size(); i++) {
                Object o1 = objects.get(i);
                if (i != objects.size() - 1) {
                    for (int j = i + 1; j < objects.size(); j++) {
                        Object o2 = objects.get(j);
                        double m = o1.getM() + o2.getM();
                        double x = o2.getX() - o1.getX();
                        double y = o2.getY() - o1.getY();
                        double r = sqrt(x * x + y * y) + epsilon;
                        double vx = o2.getVx() - o1.getVx();
                        double vy = o2.getVy() - o1.getVy();
                        double v = sqrt(vx * vx + vy * vy);
                        double comX = (o1.getX() * o1.getM() + o2.getX() * o2.getM()) / m;
                        double comY = (o1.getY() * o1.getM() + o2.getY() * o2.getM()) / m;
                        double comVx = (o1.getVx() * o1.getM() + o2.getVx() * o2.getM()) / m;
                        double comVy = (o1.getVy() * o1.getM() + o2.getVy() * o2.getM()) / m;
                        double squish = o1.getR() + o2.getR() - r;
                        double squishFactor = (o1.getR() + o2.getR()) / r;

                        // set squishFactor high to disable sticking
                        if (squishFactor >= 1000) {
                            combine(o1, o2, m, comX, comY, comVx, comVy);
                        } else if (squishFactor >= 1) {
                            collide(o1, o2, x, y, r, comVx, comVy);
                        }
//                        gravity(o1, o2, x, y, r);
                        electrostatic(o1, o2, x, y, r);
                    }
                }
                // remember that if drag is implemented, momentum will not be conserved
//                downGravity(o1);
                drag(o1);
                if (Double.isNaN(o1.getX()) || Double.isNaN(o1.getY())) {
                    toRemove.add(o1);
                }
            }

            objects.removeAll(toRemove);
            objects.addAll(toAdd);

            num = objects.size();
            totalM = 0;
            totalPx = 0;
            totalPy = 0;
            totalKE = 0;
            for (Object o : objects) {
                o.updatePosition();
                o.setToBeDeleted(false);
                double m = o.getM();
                totalM += m;
                totalPx += m * o.getVx();
                totalPy += m * o.getVy();
                totalKE += 0.5 * m * pow(o.getV(), 2);
            }

            width = contentPane.getWidth();
            height = contentPane.getHeight();

            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            frame.repaint();
        }
    }

    private void combine(Object o1, Object o2, double m, double comX, double comY, double comVx, double comVy) {
        if (o1.isToBeDeleted() || o2.isToBeDeleted()) {
            return;
        }
        toRemove.add(o1);
        o1.setToBeDeleted(true);
        toRemove.add(o2);
        o2.setToBeDeleted(true);
        double d = o1.getD() >= dBlackHole || o2.getD() >= dBlackHole ?
                dBlackHole : m / (pow(o1.getR(), 3) + pow(o2.getR(), 3));
        double elasticity = min(o1.getElasticity(), o2.getElasticity());
        if (o1 instanceof Charge || o2 instanceof Charge) {
            double q1 = 0, q2 = 0;
            if (o1 instanceof Charge) {
                q1 = ((Charge) o1).getQ();
            }
            if (o2 instanceof Charge) {
                q2 = ((Charge) o2).getQ();
            }
            double q = q1 + q2;
            toAdd.add(new Charge(d, m, comX, comY, comVx, comVy, elasticity, q));
        } else {
            toAdd.add(new Object(d, m, comX, comY, comVx, comVy, elasticity));
        }
    }

    private void collide(Object o1, Object o2, double x, double y, double r, double comVx, double comVy) {
        double relVx = o1.getVx() - comVx;
        double relVy = o1.getVy() - comVy;
        double relV = sqrt(relVx * relVx + relVy * relVy);
        double angle = atan(y / x) - atan(relVy / relVx);
        double elasticity = min(o1.getElasticity(), o2.getElasticity());
        double j = Double.isNaN(angle) ? 0 : (1 + elasticity) * o1.getM() * relV * abs(cos(angle));
        double t = 1;
        double fx = j / t * x / r;
        double fy = j / t * y / r;
        o1.exertForce(-fx, -fy);
        o2.exertForce(fx, fy);
    }

    private void gravity(Object o1, Object o2, double x, double y, double r) {
        double constant = 10;
        double power = 2;

        double f = -constant * o1.getM() * o2.getM() / abs(pow(r, power));
        double fx = f * x / r;
        double fy = f * y / r;
        o1.exertForce(-fx, -fy);
        o2.exertForce(fx, fy);
    }

    private void downGravity(Object o1) {
        double constant = 1;
        o1.exertForce(0, o1.getM() * constant);
    }

    private void electrostatic(Object o1, Object o2, double x, double y, double r) {
        double constant = 10000;
        double power = 2;
        if (!(o1 instanceof Charge) || !(o2 instanceof Charge)) {
            return;
        }

        double q1 = ((Charge) o1).getQ();
        double q2 = ((Charge) o2).getQ();
        double f = constant * q1 * q2 / pow(r, power);
        double fx = f * x / r;
        double fy = f * y / r;
        o1.exertForce(-fx, -fy);
        o2.exertForce(fx, fy);
    }

    private void drag(Object o) {
        double constant = 0.5;
        double vFactor = 0.1;
        double power = 10;

        double vx = o.getVx() * vFactor;
        double vy = o.getVy() * vFactor;
        power--;
        double m = o.getM();
        double fx = -constant * vx * abs(pow(vx, power));
        double maxFx = 0.9 * o.getVx() * m;
        fx = abs(fx) > abs(maxFx) ? -maxFx: fx;
        double fy = -constant * vy * abs(pow(vy, power));
        double maxFy = 0.9 * o.getVy() * m;
        fy = abs(fy) > abs(maxFy) ? -maxFy : fy;
        o.exertForce(fx, fy);
    }

    class Object {
        private double d, m, r, x, y, vx, vy, elasticity, fx = 0, fy = 0, puckBorder;
        private boolean toBeDeleted = false;

        public Object(double d, double m, double x, double y, double vx, double vy, double elasticity) {
            puckBorder = border + r;
            this.d = d;
            this.m = m;
            this.r = pow(abs(m) / d, (double) 1/3);
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.elasticity = elasticity;
        }

        public void exertForce(double fx, double fy) {
            this.fx += fx;
            this.fy += fy;
        }

        public void updatePosition() {
            double wallElasticity = 1;
            double actualElasticity = Math.min(elasticity, wallElasticity);

            vx += fx / m;
            vy += fy / m;
            fx = 0;
            fy = 0;

            if (x < puckBorder && vx <= 0) {
                vx = -vx * actualElasticity;
            } else if (x > width - puckBorder && vx >= 0) {
                vx = -vx * actualElasticity;
            }
            if (y < puckBorder && vy <= 0) {
                vy = -vy * actualElasticity;
            } else if (y > height - puckBorder && vy >= 0) {
                vy = -vy * actualElasticity;
            }

            x += vx;
            y += vy;
        }

        @Override
        public String toString() {
            return "m: " + round(m);
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getD() {
            return d;
        }

        public double getM() {
            return m;
        }

        public double getR() {
            return r;
        }

        public double getVx() {
            return vx;
        }

        public double getVy() {
            return vy;
        }

        public double getV() {
            return sqrt(vx * vx + vy * vy);
        }

        public double getElasticity() {
            return elasticity;
        }

        public boolean isToBeDeleted() {
            return toBeDeleted;
        }

        public void setToBeDeleted(boolean b) {
            toBeDeleted = b;
        }
    }

    class Charge extends Object {
        double q;
        public Charge(double d, double m, double x, double y, double vx, double vy, double elasticity, double q) {
            super(d, m, x, y, vx, vy, elasticity);
            this.q = q;
        }

        public double getQ() {
            return q;
        }
    }
}
