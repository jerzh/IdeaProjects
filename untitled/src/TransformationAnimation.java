import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.Stack;

/*
todo: figure out java update (get variables to only reset after second paint is complete)
todo: separate domain and range panning for range coloring
todo: finish and implement new pan function
 */

public class TransformationAnimation {
    private JPanel panel;
    private JTextField textField;
    private JTextArea textArea;
    private JScrollBar scrollBar;
    private JRadioButton domainButton, rangeButton;
    private int width = 500, height = 500;
    private final Object drawLock = new Object();
    private int dotsSizeStart = 120;
    private int dotsSize = dotsSizeStart;
    private P[][] dots;
    private P[][] dots2;
    private boolean canUpdate = false;
    private int dotWidth;
    private final double kMin = 0.0000001;
    private final double dStart = 2;
    private P k = new P(kMin);
    private double domainSize = dStart;
    private P center = new P();
    private P center2 = center;
    private double argBound = 0;
    private String typed = "preset2";
    private Stack<P> out = new Stack<>();
    private Stack<Operator> ops = new Stack<>();
    private byte valJump = 0;
    private byte drawType = 0;
    private boolean quickPan = false;

    {
        resetArray();
    }

    public static void main(String[] args) {
        new TransformationAnimation().go();
    }

    private void go() {
        JFrame frame = new JFrame("Transformation Animation");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Container contentPane = frame.getContentPane();

        panel = new TransformationPanel();
        panel.setPreferredSize(new Dimension(width, height));
        contentPane.add(panel, BorderLayout.CENTER);

        KeyListener keyListener = new TransformationKeyListener();
        panel.addKeyListener(keyListener);

        textField = new JTextField(20);
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
        textField.addActionListener(e -> {
//            textArea.append("I sense input!\n");
            String input = textField.getText();
            if (!input.equals("")) {
                typed = input;
            }
            panel.requestFocusInWindow();
            initTextArea();
            resetIt();
        });
        contentPane.add(textField, BorderLayout.NORTH);

        textArea = new JTextArea(10, 15);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollBar = scrollPane.getVerticalScrollBar();
        contentPane.add(scrollPane, BorderLayout.WEST);
        initTextArea();

        domainButton = new JRadioButton("Domain Coloring");
        domainButton.setActionCommand("domain");
        domainButton.setSelected(true);
        rangeButton = new JRadioButton("Range Coloring");
        rangeButton.setActionCommand("range");
        ButtonGroup group = new ButtonGroup();
        group.add(domainButton);
        group.add(rangeButton);
        ActionListener buttonListener = e -> {
            switch (e.getActionCommand()) {
                case "domain": drawType = 0;
                    break;
                case "range": drawType = 1;
                    break;
            }
            panel.requestFocusInWindow();
            move();
        };
        domainButton.addActionListener(buttonListener);
        rangeButton.addActionListener(buttonListener);
        JPanel radioPanel = new JPanel();
        radioPanel.add(domainButton);
        radioPanel.add(rangeButton);
        contentPane.add(radioPanel, BorderLayout.EAST);

        panel.setFocusable(true);
        textArea.setFocusable(false);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
        panel.requestFocusInWindow();
        move();
    }

    private void initTextArea() {
        textArea.setText("This is the debug column.");
    }

    class TransformationKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            double rateConstant = 0.01;
            double rangeConstant = 0.1;
            // additive
            double rate = rateConstant * (k.getR() + 1);
            double rotRate = 4 * rateConstant;
            double rRate = rangeConstant * domainSize;
            double panRate = rateConstant * (center.getR() + 1) * domainSize;
            switch (e.getKeyCode()) {
                case KeyEvent.VK_SEMICOLON:
//                    textArea.append("\nI'm resetting everything!");
                    resetIt();
                    return;
                case KeyEvent.VK_BACK_QUOTE:
//                    textArea.append("\nI'm asking for input!");
                    textField.requestFocusInWindow();
                    textField.setText("f(z) = ");
                    return;
                case KeyEvent.VK_QUOTE:
//                    textArea.append("\nI'm resetting k!");
                    k = new P(kMin);
                    break;
                case KeyEvent.VK_OPEN_BRACKET:
//                    textArea.append("\nI'm resetting mag(k)!");
                    k.polarSet(kMin, k.getTheta());
                    break;
                case KeyEvent.VK_CLOSE_BRACKET:
//                    textArea.append("\nI'm resetting arg(k)!");
                    k.polarSet(k.getR(), 0);
                    break;
                case KeyEvent.VK_LEFT:
//                    textArea.append("\nI'm increasing arg(k)!");
                    k.polarSet(k.getR(), k.getTheta() + rotRate);
                    break;
                case KeyEvent.VK_RIGHT:
//                    textArea.append("\nI'm decreasing arg(k)!");
                    k.polarSet(k.getR(), k.getTheta() - rotRate);
                    break;
                case KeyEvent.VK_UP:
//                    textArea.append("\nI'm increasing mag(k)!");
                    k.polarSet(k.getR() + rate, k.getTheta());
                    break;
                case KeyEvent.VK_DOWN:
//                    textArea.append("\nI'm decreasing mag(k)!");
                    k.polarSet(k.getR() - rate, k.getTheta());
                    break;
                case KeyEvent.VK_1:
//                    textArea.append("\nI'm resetting the zoom!");
                    domainSize = dStart;
                    break;
                case KeyEvent.VK_E:
//                    textArea.append("\nI'm zooming in!");
                    domainSize -= rRate;
                    break;
                case KeyEvent.VK_Q:
//                    textArea.append("\nI'm zooming out!");
                    domainSize += rRate;
                    break;
                case KeyEvent.VK_2:
//                    textArea.append("\nI'm resetting to the center!");
                    center = new P();
                    break;
                case KeyEvent.VK_A:
//                    textArea.append("\nI'm panning left!");
                    domainPan(new P(-panRate));
                    return;
                case KeyEvent.VK_D:
//                    textArea.append("\nI'm panning right!");
                    domainPan(new P(panRate));
                    return;
                case KeyEvent.VK_W:
//                    textArea.append("\nI'm panning up!");
                    domainPan(new P(0, panRate));
                    return;
                case KeyEvent.VK_S:
//                    textArea.append("\nI'm panning down!");
                    domainPan(new P(0, -panRate));
                    return;
                case KeyEvent.VK_X:
//                    textArea.append("\nI'm resetting the quality!");
                    dotsSize = dotsSizeStart;
                    setDotWidth();
                    break;
                case KeyEvent.VK_C:
//                    textArea.append("\nI'm decreasing the quality!");
                    dotsSize -= 2;
                    setDotWidth();
                    break;
                case KeyEvent.VK_Z:
//                    textArea.append("\nI'm increasing the quality!");
                    dotsSize += 2;
                    setDotWidth();
                    break;
                case KeyEvent.VK_SLASH:
                    stopFocus();
                    break;
                case KeyEvent.VK_PERIOD:
//                    textArea.append("\nI'm focusing on the local minimum value!");
                    valJump = -1;
                    move();
                    break;
                case KeyEvent.VK_COMMA:
//                    textArea.append("\nI'm focusing on the local maximum value!");
                    valJump = 1;
                    move();
                    break;
                case KeyEvent.VK_ENTER:
//                    textArea.append("\nI'm rounding k!");
                    long x = Math.round(k.getX());
                    long y = Math.round(k.getY());
                    k.set(x, y);
                    break;
                case KeyEvent.VK_L:
//                    textArea.append("\nI'm resetting the arg bound!");
                    argBound = 0;
                    break;
                case KeyEvent.VK_O:
//                    textArea.append("\nI'm increasing the arg bound!");
                    argBound += rotRate;
                    break;
                case KeyEvent.VK_P:
//                    textArea.append("\nI'm decreasing the arg bound!");
                    argBound -= rotRate;
                    break;
                case KeyEvent.VK_BACK_SLASH:
//                    textArea.append("\nI'm rounding the arg bound!");
                    long arg = Math.round(argBound / Math.PI * 2);
                    argBound = arg * Math.PI / 2;
                    break;
                case KeyEvent.VK_MINUS:
//                    textArea.append("\nI'm switching to domain coloring!");
                    domainButton.doClick();
                    return;
                case KeyEvent.VK_EQUALS:
//                    textArea.append("\nI'm switching to range coloring!");
                    rangeButton.doClick();
                    return;
                case KeyEvent.VK_3:
//                    textArea.append("\nI'm toggling quick pan!");
                    quickPan = !quickPan;
                    return;
                default:
                    return;
            }
            move();
        }

        @Override
        public void keyReleased(KeyEvent e) {}
    }

    private void setDotWidth() {
        dotWidth = width / dots.length / 2 + 1;
    }

    private void stopFocus() {
        if (valJump != 0) {
//            textArea.append("\nI'm no longer focusing on a value!");
            valJump = 0;
        }
    }

    private void domainPan(P z) {
        stopFocus();
        if (quickPan) {
            double unit = width / dotWidth * domainSize;
            int panX = (int) Math.round(z.getX() / unit);
            int panY = (int) Math.round(z.getY() / unit);
            int xMin = Math.max(0, panX);
            int xMax = dots2.length - Math.min(0, panX);
            int yMin = Math.max(0, panY);
            int yMax = dots2[0].length - Math.min(0, panY);

            center2 = add(center, new P(panX * unit, panY * unit));

            dots2 = new P[dotsSize][dotsSize];
            for (int i = xMin; i < xMax; i++) {
                if (yMax - yMin >= 0) System.arraycopy(dots[i - panX], yMin - panY, dots2[i], yMin, yMax - yMin);
            }
            callF(xMin, xMax, yMin, yMax);

            synchronized (drawLock) {
                textArea.append("\nI'm trying to repaint!");
                panel.repaint();
            }
        } else {
            center2 = add(center, z);
            move();
        }
    }

    class TransformationPanel extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            textArea.append("\nI'm repainting!");
            width = this.getWidth();
            height = this.getHeight();

            synchronized (drawLock) {
                textArea.append("\ncanUpdate is " + canUpdate + "!");
                if (canUpdate) {
                    dots = dots2.clone();
                    center = center2;
                }

                setDotWidth();

//                textArea.append("\ndrawType is " + drawType + "!");
                switch (drawType) {
                    case 0:
                        domainColoring(g);
                        break;
                    case 1:
                        rangeColoring(g);
                        break;
                }

                g.setColor(Color.GRAY);
                DecimalFormat format = new DecimalFormat("0.########");
                int x = 10, y = 30;
                g.drawString(getFuncName(), x, y);
                y += 20;
                g.drawString("k x: " + format.format(k.getX()), x, y);
                y += 10;
                g.drawString("k y: " + format.format(k.getY()), x, y);
                y += 10;
                g.drawString("k r: " + format.format(k.getR()), x, y);
                y += 10;
                g.drawString("k theta: " + format.format(k.getTheta()), x, y);
                y += 10;
                g.drawString("domainSize: " + domainSize, x, y);
                y += 10;
                g.drawString("center x: " + format.format(center.getX()), x, y);
                y += 10;
                g.drawString("center y: " + format.format(center.getY()), x, y);
                y += 10;
                g.drawString("quality: " + dotsSize + "p", x, y);
                y += 10;
                g.drawString("arg bound: " + format.format(argBound / Math.PI) + "pi", x, y);

                g.drawLine(width / 2 - 10, height / 2, width / 2 + 10, height / 2);
                g.drawLine(width / 2, height / 2 - 10, width / 2, height / 2 + 10);

                DrawP drawCenter = new DrawP(new P());
//                textArea.append("\ndrawCenter is " + drawCenter + "!");
                g.drawLine(0, drawCenter.getY(), width, drawCenter.getY());
                g.drawLine(drawCenter.getX(), 0, drawCenter.getX(), height);

                DrawP drawK = new DrawP(k);
//                textArea.append("\nkx is " + kx + " and ky is " + ky + "!");
                int kWidth = 3;
                g.setColor(Color.BLACK);
                g.fillOval(drawK.getX() - kWidth, drawK.getY() - kWidth, 2 * kWidth, 2 * kWidth);
                g.setColor(Color.WHITE);
                g.drawOval(drawK.getX() - kWidth, drawK.getY() - kWidth, 2 * kWidth, 2 * kWidth);

                scrollBar.setValue(scrollBar.getMaximum());
            }
        }
    }

    private String getFuncName() {
        switch (typed) {
            case "preset1": return "f(z) = (z^2-1)*(z-2-i)^2/(z^2+2+2i)";
            case "preset2": return "f(z) = (z^k-1)/(z-1)";
            default: return typed;
        }
    }

    private void domainColoring(Graphics g) {
        for (int i = 0; i < dots.length; i++) {
            for (int j = 0; j < dots[i].length; j++) {
                P colorPoint = dots[i][j];
                DrawP draw = new DrawP(getPoint(i, j));
                g.setColor(getColor(colorPoint));
                g.fillRect(draw.getX() - dotWidth, draw.getY() - dotWidth, 2 * dotWidth, 2 * dotWidth);
//                textArea.append("\nCurrently drawing at: (" + getPoint(i, j).getX() + ", " + getPoint(i, j).getY() + ")");
//                textArea.append("\nwhich maps to: (" + colorPoint.getX() + ", " + colorPoint.getY() + ")");
            }
        }
    }

    private void rangeColoring(Graphics g) {
//        g.setColor(Color.WHITE);
//        g.fillRect(0, 0, width, height);
        for (int i = 0; i < dots.length; i++) {
            for (int j = 0; j < dots[i].length; j++) {
                P colorPoint = getPoint(i, j);
                DrawP draw = new DrawP(dots[i][j]);
                int drawX = draw.getX();
                int drawY = draw.getY();
//                if (drawX < 0 || drawX > width || drawY < 0 || drawY > height)
//                    continue;
                g.setColor(getColor(colorPoint));
                int dotWidth = width / dots.length / 2 + 1;
                g.fillRect(drawX - dotWidth, drawY - dotWidth, 2 * dotWidth, 2 * dotWidth);
            }
        }

        int numLines = 10;
        for (int i = 0; i < dots.length; i += dots.length / numLines) {
            for (int j = 0; j < dots[i].length - 1; j += 1) {
                DrawP draw1 = new DrawP(dots[i][j]);
                DrawP draw2 = new DrawP(dots[i][j + 1]);
                int drawX1 = draw1.getX();
                int drawY1 = draw1.getY();
                int drawX2 = draw2.getX();
                int drawY2 = draw2.getY();
//                if (drawX1 < 0 || drawX1 > width || drawY1 < 0 || drawY1 > height
//                        || drawX2 < 0 || drawX2 > width || drawY2 < 0 || drawY2 > height)
//                    continue;
                g.setColor(Color.getHSBColor(1, 1, 0));
                g.drawLine(drawX1, drawY1, drawX2, drawY2);
            }
        }

        for (int j = 0; j < dots[0].length; j += dots[0].length / numLines) {
            for (int i = 0; i < dots.length - 1; i += 1) {
                DrawP draw1 = new DrawP(dots[i][j]);
                DrawP draw2 = new DrawP(dots[i + 1][j]);
                int drawX1 = draw1.getX();
                int drawY1 = draw1.getY();
                int drawX2 = draw2.getX();
                int drawY2 = draw2.getY();
//                if (drawX1 < 0 || drawX1 > width || drawY1 < 0 || drawY1 > height
//                        || drawX2 < 0 || drawX2 > width || drawY2 < 0 || drawY2 > height)
//                    continue;
                g.setColor(Color.getHSBColor(1, 1, 0));
                g.drawLine(drawX1, drawY1, drawX2, drawY2);
            }
        }
    }

    private class DrawP {
        int drawX, drawY;

        DrawP(double x, double y) {
            drawX = (int) ((x - center.getX()) * width / domainSize / 2) + width / 2;
            drawY = -(int) ((y - center.getY()) * height / domainSize / 2) + height / 2;
        }

        DrawP(P point) {
            this(point.getX(), point.getY());
        }

        int getX() {
            return drawX;
        }

        int getY() {
            return drawY;
        }

        @Override
        public String toString() {
            return "(" + drawX + ", " + drawY + ")";
        }
    }

    private Color getColor(P point) {
        float hue = (float) (point.getTheta() / Math.PI / 2);
        double mag = point.getR();
        double minSat = 0.1;
        float saturation = (float) (minSat + (1 - minSat) / (mag / 1000 + 1));
        double magFactor = Math.log(mag) / Math.log(2);
        double magConstant = 1 - 1 / (mag + 1);
        float brightness = 1 - (float) Math.pow(2, -3 * (magFactor - Math.floor(magFactor) + magConstant));
        return Color.getHSBColor(hue, saturation, brightness);
    }

    private void resetIt() {
//        textArea.append("\nI see input! " + typed);
        k = new P(kMin);
        domainSize = dStart;
        center2 = new P();
        dotsSize = dotsSizeStart;
        valJump = 0;
        argBound = 0;
        move();
    }

    private void resetArray() {
        canUpdate = false;

        dots2 = new P[dotsSize][dotsSize];
        for (int i = 0; i < dots2.length; i++) {
            for (int j = 0; j < dots2[i].length; j++) {
                dots2[i][j] = getPoint2(i, j);
            }
        }
    }

    private void move() {
        textArea.append("\nI'm here!");

        resetArray();

//        textArea.append("\nI'm calculating values!");
        callF();

        if (valJump != 0) {
            jump();
        }

        synchronized (drawLock) {
            canUpdate = true;
            textArea.append("\nI'm trying to repaint!");
            panel.repaint();
        }
//        textArea.append("\nI made it!");
    }

    private void jump() {
        if (valJump == -1) {
            double minMag = dots2[0][0].getR();
            int minI = 0;
            int minJ = 0;
            for (int i = 0; i < dots2.length; i++) {
                for (int j = 0; j < dots2[i].length; j++) {
                    double mag = dots2[i][j].getR();
                    if (mag < minMag) {
                        minMag = mag;
                        minI = i;
                        minJ = j;
                    }
                }
            }
            center2 = getPoint2(minI, minJ);
        } else if (valJump == 1) {
            double maxMag = dots2[0][0].getR();
            int maxI = 0;
            int maxJ = 0;
            for (int i = 0; i < dots2.length; i++) {
                for (int j = 0; j < dots2[i].length; j++) {
                    double mag = dots2[i][j].getR();
                    if (mag > maxMag) {
                        maxMag = mag;
                        maxI = i;
                        maxJ = j;
                    }
                }
            }
            center2 = getPoint2(maxI, maxJ);
        }
    }

    private void callF() {
        callF(0, dots2.length, 0, dots2[0].length);
    }

    // calls f for everything EXCEPT between starts and ends
    private void callF(int rStart, int rEnd, int cStart, int cEnd) {
//        textArea.append("\ntyped is " + typed + "!");
        switch (typed) {
            case "lorentz": calcF(this::lorentz, rStart, rEnd, cStart, cEnd);
                break;
            case "preset1": calcF(this::preset1, rStart, rEnd, cStart, cEnd);
                break;
            case "preset2": calcF(this::preset2, rStart, rEnd, cStart, cEnd);
                break;
            default: if (typed.matches("f\\(z\\) = .+")) {
                calcF(this::custom, rStart, rEnd, cStart, cEnd);
            } else {
                calcF(this::error, rStart, rEnd, cStart, cEnd);
            }
        }
    }

    private P getPoint(int i, int j) {
        double x = (i - dots.length / 2) * 2 * domainSize / dots.length + center.getX();
        double y = (j - dots[i].length / 2) * 2 * domainSize / dots.length + center.getY();
        return new P(x, y);
    }

    private P getPoint2(int i, int j) {
        double x = (i - dots2.length / 2) * 2 * domainSize / dots2.length + center2.getX();
        double y = (j - dots2[i].length / 2) * 2 * domainSize / dots2.length + center2.getY();
        return new P(x, y);
    }

    private void calcF(Function f, int rStart, int rEnd, int cStart, int cEnd) {
        for (int i = 0; i < dots2.length; i++) {
            for (int j = 0; j < dots2[0].length; j++) {
                if (!(i > rStart && i < rEnd && j > cStart && j < cEnd)) {
                    f.f(dots2[i][j]);
                }
            }
        }
    }

    interface Function {
        void f(P point);
    }

    private void lorentz(P point) {
        if (k.getY() != 0) {
            point.set(0, 0);
            return;
        }
        double kMax = 10;
        double kx = k.getX();
        double x = point.getX();
        double t = point.getY();
        double gamma = 1 / Math.sqrt(1 - Math.pow(kx / kMax, 2));
        double xPrime = gamma * (x - kx * t);
        double tPrime = gamma * (t - kx * x / kMax / kMax);
        point.set(xPrime, tPrime);
    }

    private void preset1(P z) {
        z.set(mul(mul(add(pow(z, new P(2)), new P(-1)), pow(add(z, new P(-2, -1)), new P(2))), rec(add(pow(z, new P(2)), new P(2, 1)))));
    }

    private void preset2(P z) {
        z.set(mul(add(pow(z, k), new P(-1)), rec(add(z, new P(-1)))));
    }

    private void custom(P z) {
        String formula = typed.substring(7);
        out = new Stack<>();
        ops = new Stack<>();
        boolean negative = false;
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (Character.isDigit(c)) {
                int j = i;
                i++;
                while (i <= formula.length() && formula.substring(j, i).matches("[0-9]*\\.?[0-9]*")) {
                    i++;
                }
                i--;
                double num = Double.parseDouble(formula.substring(j, i));
                if (negative) {
                    num = -num;
                }
                if (i < formula.length() && formula.charAt(i) == 'i') {
                    out.push(new P(0, num));
                    i++;
                }
                else {
                    out.push(new P(num));
                    i--;
                }
            } else {
                switch (c) {
                    case 'i':
                        if (negative) {
                            out.push(new P(0, -1));
                        }
                        out.push(new P(0, 1));
                        break;
                    case 'z':
                        out.push(z);
                        break;
                    case 'k':
                        out.push(k);
                        break;
                    case 'e':
                        out.push(new P(Math.E));
                        break;
                    case '(':
                        ops.push(Operator.L_PAREN);
                        if (formula.charAt(i + 1) == '-') {
                            negative = true;
                            i++;
                        }
                        break;
                    case ')':
                        Operator op = ops.pop();
                        while (op != Operator.L_PAREN) {
                            outPush(op);
                            op = ops.pop();
                        }
                        break;
                    case '+':
                        opPush(Operator.PLUS);
                        break;
                    case '-':
                        opPush(Operator.MINUS);
                        break;
                    case '*':
                        opPush(Operator.TIMES);
                        break;
                    case '/':
                        opPush(Operator.DIV);
                        break;
                    case '^':
                        opPush(Operator.POW);
                        break;
                    case '|':
                        opPush(Operator.LOG);
                        break;
                    default:
                        error(z);
                        return;
                }
            }
        }
        while (!ops.isEmpty()) {
            Operator op = ops.pop();
            outPush(op);
        }
        z.set(out.get(0));
    }

    private void opPush(Operator op) {
        while (!ops.isEmpty()) {
            Operator op2 = ops.peek();
            if (op2.getPrecedence() <= op.getPrecedence()) {
                break;
            }
            ops.pop();
            outPush(op2);
        }
        ops.push(op);
//        textArea.append("\nThe ops stack is " + ops + "!");
    }

    private void outPush(Operator op) {
        switch (op) {
            case PLUS: out.push(add(out.pop(), out.pop())); break;
            case MINUS: out.push(add(neg(out.pop()), out.pop())); break;
            case TIMES: out.push(mul(out.pop(), out.pop())); break;
            case DIV: out.push(mul(rec(out.pop()), out.pop())); break;
            case POW: P b = out.pop();
                P a = out.pop();
                out.push(pow(a, b)); break;
            case LOG: b = out.pop();
                a = out.pop();
                out.push(log(a, b)); break;
        }
    }

    private enum Operator {
        L_PAREN(0), PLUS(1), MINUS(1), TIMES(2), DIV(2), POW(3), LOG(3);

        private int precedence;

        Operator(int precedence) {
            this.precedence = precedence;
        }

        public int getPrecedence() {
            return precedence;
        }

    }

    private void error(P z) {
        z.set(0, 0);
    }

    private P pow(P z1, P z2) {
        return exp(mul(z2, ln(z1)));
    }

    private P log(P z1, P z2) {
        return mul(ln(z1), rec(ln(z2)));
    }

    private P exp(P z) {
        double a = z.getX();
        double b = z.getY();
        double mag = Math.exp(a);
        double xNew = mag * Math.cos(b);
        double yNew = mag * Math.sin(b);
        return new P(xNew, yNew);
    }

    private P ln(P z) {
        return new P(Math.log(z.getR()), z.getTheta());
    }

    private P neg(P z) {
        return mul(z, new P(-1));
    }

    private P mul(P z1, P z2) {
        return new P(true, z1.getR() * z2.getR(), z1.getTheta() + z2.getTheta());
    }

    private P add(P z1, P z2) {
        return new P(z1.getX() + z2.getX(), z1.getY() + z2.getY());
    }

    // reciprocate
    private P rec(P z) {
        return new P(true, 1 / z.getR(), -z.getTheta());
    }

    // p for point
    private class P {
        double x, y, r, theta;

        P() {
            this(0, 0);
        }

        P(double x) {
            this(x, 0);
        }

        P(double x, double y) {
            set(x, y);
        }

        P(boolean polar, double r, double theta) {
            if (polar) {
                polarSet(r, theta);
            }
        }

        void set(double x, double y) {
            this.x = x;
            this.y = y;
            r = Math.sqrt(x * x + y * y);
            theta = arg(x, y);
        }

        void polarSet(double r, double theta) {
            this.r = r;
            this.theta = theta;
            x = r * Math.cos(theta);
            y = r * Math.sin(theta);
        }

        void set(P z) {
            set(z.getX(), z.getY());
        }

        double getX() {
            return x;
        }

        double getY() {
            return y;
        }

        double getR() {
            return r;
        }

        double getTheta() {
            return theta;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    private double arg(double x, double y) {
        double arg;
        if (x == 0) {
            if (y == 0) {
                arg = 0;
            } else if (y > 0) {
                arg = Math.PI / 2;
            } else {
                arg = Math.PI * 3 / 2;
            }
        } else if (x < 0) {
            arg = Math.atan(y / x) + Math.PI;
        } else {
            arg = Math.atan(y / x);
        }

        while (true) {
            if (arg < argBound) {
                arg += 2 * Math.PI;
            } else if (arg > argBound + 2 * Math.PI) {
                arg -= 2 * Math.PI;
            } else {
                break;
            }
        }
        return arg;
    }
}