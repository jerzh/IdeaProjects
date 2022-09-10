import java.awt.*;
import java.applet.*;
import java.util.ArrayList;
import java.util.Arrays;

/*
todo: (2) convert to swing
todo: (1) implement tree sort
bug: may sometimes freeze - move the mouse over the applet
bug: bead sort draws incorrectly on the second run
bug: still can't get rid of flickering!! (probably not a bug)
 */

public class SortAnimation extends Applet implements Runnable {
    private static final int SIZE = 100;
    private static final int MAX_VAL = SIZE;
    private static final long DELAY_TIME = 10;
    private int[] array = new int[SIZE];
    private int[] accessedValues = {-1, -1};
    private int[] _accessedValues = {-1, -1};

    private int storedValue = -1;
    private boolean storedValueAccessed = false;
    private boolean _storedValueAccessed = false;

    private int[] array2 = new int[0];
    private int array2AccessedValue = -1;
    private int _array2AccessedValue = -1;

    private ArrayList<ArrayList<Integer>> buckets = new ArrayList<>();
    private int[] bucketAccessedValue = {-1, -1};
    private int[] _bucketAccessedValue = {-1, -1};

    private int[][] beads = new int[0][0];
    private int[][] accessedBeads = {{-1, -1}, {-1, -1}};
    private int[][] _accessedBeads = {{-1, -1}, {-1, -1}};

    private boolean delay;

    private enum changeType {
        swap, replace, none
    }

    private enum drawType {
        inPlace, outOfPlace, buckets, beads
    }
    private drawType draw = drawType.inPlace;
    private boolean redraw = false;

    private enum clearType {
        all, storage, bucket
    }
    private boolean doClear = false;
    private clearType clear = clearType.all;

    private int numOperations = 0;

    private Thread runner;

    @Override
    public void start() {
        if (runner == null) {
            runner = new Thread(this);
            runner.start();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void stop() {
        if (runner != null) {
            runner.stop();
            runner = null;
        }
    }

    /* supported sorts are in ALL CAPS; the rest I may implement later
    american flag sort
    BEAD SORT / GRAVITY SORT
    BOGO SORT
    BUBBLE SORT
    COCKTAIL SHAKER SORT
    COMB SORT
    COUNTING SORT
    GNOME SORT
    HEAP SORT (MAX)
    INSERTION SORT
    merge sort (in-place)
    MERGE SORT (OUT-OF-PLACE)
    pancake sort
    PIGEONHOLE SORT
    RADIX SORT (LSD)
    radix sort (msd) / bucket sort
    SELECTION SORT
    shatter sort / multi-key quick sort / three-way radix quick sort
    SHELL SORT
    QUICK SORT (IN-PLACE, HOARE) */

    @Override
    public synchronized void run() {
        setSize(1200, 800);
        initialize();
        for (int i = 0; i < 10; i++) {
            playAll();
        }
    }

    @Override
    public synchronized void update(Graphics g) {
        paint(g);
    }

    // how to get rid of flickering??
    @Override
    public synchronized void paint(Graphics g) {
        numOperations++;
        if (doClear) {
            g.setColor(Color.WHITE);
            switch (clear) {
                case all:
                    g.fillRect(0, 0, getWidth(), getHeight());
                    break;
                case storage:
                    g.fillRect(0, getHeight() / 2, getWidth(), getHeight() / 2);
                    _array2AccessedValue = -1;
                    _bucketAccessedValue = new int[]{-1, -1};
                    break;
                case bucket:
                    int bucketW = buckets.size() == 0 ? 0 : (getWidth() - 100) / buckets.size();
                    g.fillRect(50 + _bucketAccessedValue[0] * bucketW, getHeight() / 2, bucketW, getHeight() / 2);
                    g.fillRect(50 + bucketAccessedValue[0] * bucketW, getHeight() / 2, bucketW, getHeight() / 2);
                    break;
            }
        } else {
            int w = (getWidth() - 100) / SIZE;
            int h = (getHeight() - 100) / MAX_VAL;
            if (storedValueAccessed || _storedValueAccessed){
                if (storedValueAccessed) {
                    g.setColor(Color.GREEN);
                } else {
                    g.setColor(Color.BLUE);
                }
                g.fillRect(45 - w, getHeight() - 50 - h * storedValue, w, h * storedValue);
                g.setColor(Color.WHITE);
                g.fillRect(45 - w, 50, w, getHeight() - 100 - h * storedValue);
            }
            removeCommon(_accessedValues, accessedValues);
            switch (draw) {
                case inPlace:
                    if (redraw) {
                        for (int i = 0; i < array.length; i++) {
                            if (i == accessedValues[0] || i == accessedValues[1]) {
                                g.setColor(Color.GREEN);
                            } else {
                                g.setColor(Color.RED);
                            }
                            g.fillRect(50 + w * i, getHeight() - 50 - h * array[i], w, h * array[i]);
                            g.setColor(Color.WHITE);
                            g.fillRect(50 + w * i, 50, w, getHeight() - 100 - h * array[i]);
                        }
                    } else {
                        for (int i : _accessedValues) {
                            if (i != -1) {
                                g.setColor(Color.RED);
                                g.fillRect(50 + w * i, getHeight() - 50 - h * array[i], w, h * array[i]);
                                g.setColor(Color.WHITE);
                                g.fillRect(50 + w * i, 50, w, getHeight() - 100 - h * array[i]);
                            }
                        }
                        for (int i : accessedValues) {
                            if (i != -1) {
                                g.setColor(Color.GREEN);
                                g.fillRect(50 + w * i, getHeight() - 50 - h * array[i], w, h * array[i]);
                                g.setColor(Color.WHITE);
                                g.fillRect(50 + w * i, 50, w, getHeight() - 100 - h * array[i]);
                            }
                        }
                    }
                    break;
                case outOfPlace:
                    h = (getHeight() / 2 - 100) / MAX_VAL;

                    if (redraw) {
                        for (int i = 0; i < array.length; i++) {
                            if (i == accessedValues[0] || i == accessedValues[1]) {
                                g.setColor(Color.GREEN);
                            } else {
                                g.setColor(Color.RED);
                            }
                            g.fillRect(50 + w * i, getHeight() / 2 - 50 - h * array[i], w, h * array[i]);
                            g.setColor(Color.WHITE);
                            g.fillRect(50 + w * i, 50, w, getHeight() / 2 - 100 - h * array[i]);
                        }
                        for (int i = 0; i < array2.length; i++) {
                            if (i == array2AccessedValue) {
                                g.setColor(Color.GREEN);
                            } else {
                                g.setColor(Color.BLUE);
                            }
                            g.fillRect(50 + w * i, getHeight() - 50 - h * array2[i], w, h * array2[i]);
                            g.setColor(Color.WHITE);
                            g.fillRect(50 + w * i, getHeight() / 2 + 50, w, getHeight() / 2 - 100 - h * array2[i]);
                        }
                    } else {
                        for (int i : _accessedValues) {
                            if (i != -1) {
                                g.setColor(Color.RED);
                                g.fillRect(50 + w * i, getHeight() / 2 - 50 - h * array[i], w, h * array[i]);
                                g.setColor(Color.WHITE);
                                g.fillRect(50 + w * i, 50, w, getHeight() / 2 - 100 - h * array[i]);
                            }
                        }
                        for (int i : accessedValues) {
                            if (i != -1) {
                                g.setColor(Color.GREEN);
                                g.fillRect(50 + w * i, getHeight() / 2 - 50 - h * array[i], w, h * array[i]);
                                g.setColor(Color.WHITE);
                                g.fillRect(50 + w * i, 50, w, getHeight() / 2 - 100 - h * array[i]);
                            }
                        }

                        if (_array2AccessedValue == array2AccessedValue) {
                            _array2AccessedValue = -1;
                        }
                        w = array2.length == 0 ? 0 : (getWidth() - 100) / array2.length;
                        if (_array2AccessedValue != -1) {
                            int i = _array2AccessedValue;
                            g.setColor(Color.BLUE);
                            g.fillRect(50 + w * i, getHeight() - 50 - h * array2[i], w, h * array2[i]);
                            g.setColor(Color.WHITE);
                            g.fillRect(50 + w * i, getHeight() / 2 + 50, w, getHeight() / 2 - 100 - h * array2[i]);
                        }
                        if (array2AccessedValue != -1) {
                            int i = array2AccessedValue;
                            g.setColor(Color.GREEN);
                            g.fillRect(50 + w * i, getHeight() - 50 - h * array2[i], w, h * array2[i]);
                            g.setColor(Color.WHITE);
                            g.fillRect(50 + w * i, getHeight() / 2 + 50, w, getHeight() / 2 - 100 - h * array2[i]);
                        }
                    }
                    break;
                case buckets:
                    h = (getHeight() / 2 - 100) / MAX_VAL;
                    int bucketW = buckets.size() == 0 ? 0 : (getWidth() - 100) / buckets.size();

                    if (redraw) {
                        for (int i = 0; i < array.length; i++) {
                            if (i == accessedValues[0] || i == accessedValues[1]) {
                                g.setColor(Color.GREEN);
                            } else {
                                g.setColor(Color.RED);
                            }
                            g.fillRect(50 + w * i, getHeight() / 2 - 50 - h * array[i], w, h * array[i]);
                            g.setColor(Color.WHITE);
                            g.fillRect(50 + w * i, 50, w, getHeight() / 2 - 100 - h * array[i]);
                        }
                        for (int i = 0; i < buckets.size(); i++) {
                            ArrayList<Integer> bucket = buckets.get(i);
                            for (int j = 0; j < bucket.size(); j++) {
                                if (i == bucketAccessedValue[0] && j == bucketAccessedValue[1]) {
                                    g.setColor(Color.GREEN);
                                } else {
                                    g.setColor(Color.BLUE);
                                }
                                g.fillRect(52 + bucketW * i + w * j, getHeight() - 50 - h * bucket.get(j), w, h * bucket.get(j));
                                g.setColor(Color.WHITE);
                                g.fillRect(52 + bucketW * i + w * j, getHeight() / 2 + 50, w, getHeight() / 2 - 100 - h * bucket.get(j));
                            }
                        }
                    } else {
                        for (int i : _accessedValues) {
                            if (i != -1) {
                                g.setColor(Color.RED);
                                g.fillRect(50 + w * i, getHeight() / 2 - 50 - h * array[i], w, h * array[i]);
                                g.setColor(Color.WHITE);
                                g.fillRect(50 + w * i, 50, w, getHeight() / 2 - 100 - h * array[i]);
                            }
                        }
                        for (int i : accessedValues) {
                            if (i != -1) {
                                g.setColor(Color.GREEN);
                                g.fillRect(50 + w * i, getHeight() / 2 - 50 - h * array[i], w, h * array[i]);
                                g.setColor(Color.WHITE);
                                g.fillRect(50 + w * i, 50, w, getHeight() / 2 - 100 - h * array[i]);
                            }
                        }

                        removeCommon(_bucketAccessedValue, bucketAccessedValue);
                        if (_bucketAccessedValue[0] != -1) {
                            int i = _bucketAccessedValue[0];
                            ArrayList<Integer> bucket = buckets.get(i);
                            w = bucket.size() == 0 ? 0 : (bucketW - 4) / bucket.size();
                            for (int j = 0; j < bucket.size(); j++) {
                                g.setColor(Color.BLUE);
                                g.fillRect(52 + bucketW * i + w * j, getHeight() - 50 - h * bucket.get(j), w, h * bucket.get(j));
                                g.setColor(Color.WHITE);
                                g.fillRect(52 + bucketW * i + w * j, getHeight() / 2 + 50, w, getHeight() / 2 - 100 - h * bucket.get(j));
                            }
                        }
                        if (bucketAccessedValue[0] != -1) {
                            int i = bucketAccessedValue[0];
                            ArrayList<Integer> bucket = buckets.get(i);
                            w = bucket.size() == 0 ? 0 : (bucketW - 4) / bucket.size();
                            for (int j = 0; j < bucket.size(); j++) {
                                if (j == bucketAccessedValue[1]) {
                                    g.setColor(Color.GREEN);
                                } else {
                                    g.setColor(Color.BLUE);
                                }
                                g.fillRect(52 + bucketW * i + w * j, getHeight() - 50 - h * bucket.get(j), w, h * bucket.get(j));
                                g.setColor(Color.WHITE);
                                g.fillRect(52 + bucketW * i + w * j, getHeight() / 2 + 50, w, getHeight() / 2 - 100 - h * bucket.get(j));
                            }
                        }
                    }
                    break;
                case beads:
                    h = (getHeight() / 2 - 100) / MAX_VAL;

                    if (redraw) {
                        for (int i = 0; i < array.length; i++) {
                            if (i == accessedValues[0] || i == accessedValues[1]) {
                                g.setColor(Color.GREEN);
                            } else {
                                g.setColor(Color.RED);
                            }
                            g.fillRect(50 + w * i, getHeight() / 2 - 50 - h * array[i], w, h * array[i]);
                            g.setColor(Color.WHITE);
                            g.fillRect(50 + w * i, 50, w, getHeight() / 2 - 100 - h * array[i]);
                        }
                        w = beads.length == 0 ? 0 : (getWidth() - 100) / beads.length;
                        for (int i = 0; i < beads.length; i++) {
                            for (int j = 0; j < beads[i].length; j++) {
                                if ((i == accessedBeads[0][0] && j == accessedBeads[0][1])
                                        || (i == accessedBeads[1][0] && j == accessedBeads[1][1])) {
                                    g.setColor(Color.GREEN);
                                } else if (beads[i][j] == 1) {
                                    g.setColor(Color.BLUE);
                                } else {
                                    g.setColor(Color.WHITE);
                                }
                                g.fillRect(50 + w * i, getHeight() - 50 - h * j, w, h);
                            }
                        }
                    } else {
                        for (int i : _accessedValues) {
                            if (i != -1) {
                                g.setColor(Color.RED);
                                g.fillRect(50 + w * i, getHeight() / 2 - 50 - h * array[i], w, h * array[i]);
                                g.setColor(Color.WHITE);
                                g.fillRect(50 + w * i, 50, w, getHeight() / 2 - 100 - h * array[i]);
                            }
                        }
                        for (int i : accessedValues) {
                            if (i != -1) {
                                g.setColor(Color.GREEN);
                                g.fillRect(50 + w * i, getHeight() / 2 - 50 - h * array[i], w, h * array[i]);
                                g.setColor(Color.WHITE);
                                g.fillRect(50 + w * i, 50, w, getHeight() / 2 - 100 - h * array[i]);
                            }
                        }

                        removeCommon(_accessedBeads, accessedBeads);
                        w = beads.length == 0 ? 0 : (getWidth() - 100) / beads.length;
                        for (int[] _accessedBead : _accessedBeads) {
                            int i = _accessedBead[0];
                            if (i != -1) {
                                int j = _accessedBead[1];
                                if (beads[i][j] == 0) {
                                    g.setColor(Color.WHITE);
                                } else {
                                    g.setColor(Color.BLUE);
                                }
                                g.fillRect(50 + w * i, getHeight() - 50 - h * j, w, h);
                            }
                        }
                        for (int[] accessedBead : accessedBeads) {
                            int i = accessedBead[0];
                            if (i != -1) {
                                int j = accessedBead[1];
                                if (beads[i][j] == 0) {
                                    g.setColor(Color.WHITE);
                                } else {
                                    g.setColor(Color.GREEN);
                                }
                                g.fillRect(50 + w * i, getHeight() - 50 - h * j, w, h);
                            }
                        }
                    }
                    break;
            }
            _accessedValues = accessedValues;
            accessedValues = new int[]{-1, -1};
            _storedValueAccessed = storedValueAccessed;
            storedValueAccessed = false;
            _array2AccessedValue = array2AccessedValue;
            array2AccessedValue = -1;
            _bucketAccessedValue = bucketAccessedValue;
            bucketAccessedValue = new int[]{-1, -1};
            _accessedBeads = accessedBeads;
            accessedBeads = new int[][]{{-1, -1}, {-1, -1}};
        }
        notifyAll();
    }

    private synchronized void removeCommon(int[] removeFrom, int[] toBeRemoved) {
        for (int i = 0; i < removeFrom.length; i++) {
            for (int e : toBeRemoved) {
                if (removeFrom[i] == e) {
                    removeFrom[i] = -1;
                    break;
                }
            }
        }
    }

    private synchronized void removeCommon(int[][] removeFrom, int[][] toBeRemoved) {
        for (int i = 0; i < removeFrom.length; i++) {
            for (int[] e : toBeRemoved) {
                if (Arrays.equals(removeFrom[i], e)) {
                    removeFrom[i] = new int[]{-1, -1};
                    break;
                }
            }
        }
    }

    private synchronized void initialize() {
        clear(clearType.all);
        draw = drawType.inPlace;
        for (int i = 0; i < SIZE; i++) {
            array[i] = i + 1;
            delay = false;
            drawArray(i);
        }
    }

    private synchronized void reverseInitialize() {
        clear(clearType.all);
        draw = drawType.inPlace;
        for (int i = 0; i < SIZE; i++) {
            array[i] = SIZE - i;
            delay = false;
            drawArray(i);
        }
    }

    private synchronized void randomInitialize() {
        clear(clearType.all);
        draw = drawType.inPlace;
        for (int i = 0; i < SIZE; i++) {
            array[i] = (int) (Math.random() * SIZE) + 1;
            delay = false;
            drawArray(i);
        }
    }

    private synchronized void shuffle() {
        clear(clearType.all);
        draw = drawType.inPlace;

        for (int i = 0; i < array.length; i++) {
            int x = (int) (Math.random() * array.length);
            delay = false;
            drawArray(changeType.swap, x, i);
        }
        // initialize();
        // reverseInitialize();
        // randomInitialize();

        drawFinal("");
    }

    private synchronized void drawArray(int x) {
        drawArray(changeType.none, x, -1);
    }

    private synchronized void drawArray(changeType swap, int x, int y) {
        switch (swap) {
            case swap:
                int temp = array[x];
                array[x] = array[y];
                array[y] = temp;
                break;
            case replace:
                array[y] = array[x];
        }
        accessedValues = new int[]{x, y};
        if (redraw) {
            clear(clearType.all);
        }
        repaint();
        try {
            wait();
            if (delay) {
                Thread.sleep(DELAY_TIME);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        reset();
    }

    private synchronized void reset() {
        storedValueAccessed = false;
        delay = true;
        doClear = false;
        redraw = false;
    }

    private synchronized void drawFinal(String sortName) {
        finalReset();
        if (!sortName.equals("")) {
            displayNumOperations(sortName);
        }
        drawArray(-1);
        numOperations = 0;
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void finalReset() {
        storedValue = 0;
        array2 = new int[0];
        array2AccessedValue = -1;
        buckets = new ArrayList<>();
        bucketAccessedValue = new int[]{-1, -1};
    }

    private synchronized void clear(clearType clear) {
        doClear = true;
        this.clear = clear;
        repaint();
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        doClear = false;
    }

    private synchronized void displayNumOperations(String sortName) {
        System.out.println("# operations (" + sortName + "): " + numOperations);
    }

    private synchronized int findMax(int start, int end) {
        int max = start;
        for (int i = start + 1; i <= end; i++) {
            drawArray(changeType.none, max, i);
            if (array[max] < array[i]) {
                max = i;
            }
        }
        return max;
    }

    private synchronized int findMin(int start, int end) {
        int min = start;
        for (int i = start + 1; i <= end; i++) {
            drawArray(changeType.none, min, i);
            if (array[min] > array[i]) {
                min = i;
            }
        }
        return min;
    }

    private synchronized int[] findMaxMin() {
        int max = 0, min = 0;
        for (int i = 1; i <= array.length - 1; i++) {
            drawArray(changeType.none, max, i);
            if (array[max] < array[i]) {
                max = i;
            }
            drawArray(changeType.none, min, i);
            if (array[min] > array[i]) {
                min = i;
            }
        }
        return new int[]{max, min};
    }

    private synchronized void copyBuckets() {
        int arrayIndex = 0;
        for (int i = 0; i < buckets.size(); i++) {
            ArrayList<Integer> bucket = buckets.get(i);
            for (int j = 0; j < bucket.size(); j++) {
                array[arrayIndex] = bucket.get(j);
                bucketAccessedValue = new int[]{i, j};
                drawArray(arrayIndex);
                arrayIndex++;
            }
        }
    }

    private synchronized void playAll() {
        bubbleSort();
        cocktailShakerSort();
        combSort();
        countingSort();
        gnomeSort();
        heapSortMax();
        insertionSort();
        mergeSortOutOfPlace();
        pigeonholeSort();
        radixSortLSD();
        selectionSort();
        shellSort();
        quickSortInPlaceHoare();
    }

    // drawing bug on second run
    private synchronized void beadSort() {
        shuffle();
        draw = drawType.beads;
        redraw = true;
        int[] maxMin = findMaxMin();
        int maxVal = array[maxMin[0]];
        int minVal = array[maxMin[1]];
        beads = new int[array.length][maxVal - minVal + 1];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i] - minVal; j++) {
                beads[i][j] = 1;
                accessedBeads[0] = new int[]{i, j};
                drawArray(i);
            }
        }
        for (int i = beads[0].length - 1; i >= 0; i--) {
            int count = beads.length - 1;
            for (int j = beads.length - 1; j >= 0; j--) {
                if (beads[j][i] == 1) {
                    beads[j][i] = 0;
                    beads[count][i] = 1;
                    accessedBeads = new int[][]{{j, i}, {count, i}};
                    drawArray(-1);
                    count--;
                }
            }
        }
        for (int i = 0; i < beads.length; i++) {
            int j;
            for (j = 0; j < beads[i].length; j++) {
                accessedBeads[0] = new int[]{i, j};
                drawArray(-1);
                if (beads[i][j] == 0) {
                    break;
                }
            }
            array[i] = j + minVal;
            drawArray(i);
        }
        clear(clearType.storage);
        drawFinal("bead sort");
    }

    private synchronized void bogoSort() {
        shuffle();
        while (true) {
            int x = (int) (Math.random() * array.length);
            int y = (int) (Math.random() * array.length);

            drawArray(changeType.swap, x, y);

            boolean noSwaps = true;
            for (int i = 0; i < array.length - 1; i++) {
                drawArray(changeType.none, i, i + 1);
                if (array[i] > array[i + 1]) {
                    noSwaps = false;
                    break;
                }
            }

            if (noSwaps) {
                break;
            }
        }
        drawFinal("bogo sort");
    }

    private synchronized void bubbleSort() {
        shuffle();
        for (int i = 0; i < array.length; i++) {
            boolean noSwaps = true;
            for (int j = 0; j < array.length - i - 1; j++) {
                changeType swap = changeType.none;
                if (array[j] > array[j + 1]) {
                    swap = changeType.swap;
                    noSwaps = false;
                } else if (j == array.length - 2) {
                    i++;
                }
                drawArray(swap, j, j + 1);
            }

            if (noSwaps) {
                break;
            }
        }
        drawFinal("bubble sort");
    }

    private synchronized void cocktailShakerSort() {
        shuffle();
        for (int i = 0; i < array.length / 2; i++) {
            boolean noSwaps = true;
            for (int j = i; j < array.length - i - 1; j++) {
                changeType swap = changeType.none;
                if (array[j] > array[j + 1]) {
                    swap = changeType.swap;
                    noSwaps = false;
                }
                drawArray(swap, j, j + 1);
            }

            if (noSwaps) {
                break;
            }

            noSwaps = true;
            for (int j = array.length - i - 2; j >= i; j--) {
                changeType swap = changeType.none;
                if (array[j] > array[j + 1]) {
                    swap = changeType.swap;
                    noSwaps = false;
                }
                drawArray(swap, j, j + 1);
            }

            if (noSwaps) {
                break;
            }
        }
        drawFinal("cocktail shaker sort");
    }

    private synchronized void combSort() {
        final double k = 1.3;
        shuffle();
        int n = array.length;
        double n1 = n;
        boolean noSwaps = false;
        while (n != 1 || !noSwaps) {
            noSwaps = true;
            if (n != 1) {
                n1 = n1 / k;
                n = (int) n1;
            }
            for (int i = 0; i < array.length - n; i++) {
                changeType swap = changeType.none;
                if (array[i] > array[i + n]) {
                    swap = changeType.swap;
                    noSwaps = false;
                }
                drawArray(swap, i, i + n);
            }
        }
        drawFinal("comb sort");
    }

    private synchronized void countingSort() {
        shuffle();
        draw = drawType.outOfPlace;
        redraw = true;
        int[] maxMin = findMaxMin();
        int maxVal = array[maxMin[0]];
        int minVal = array[maxMin[1]];
        array2 = new int[maxVal - minVal + 1];
        for (int i = 0; i < array.length; i++) {
            array2[array[i] - minVal]++;
            array2AccessedValue = array[i] - minVal;
            drawArray(i);
        }
        int total = -1;
        for (int i = 0; i < maxVal - minVal + 1; i++) {
            int oldCount = array2[i];
            array2AccessedValue = i;
            drawArray(-1);

            total += oldCount;

            array2[i] = total;
            array2AccessedValue = i;
            drawArray(-1);
        }
        for (int i = 0; i < maxVal - minVal + 1; i++) {
            storedValue = array2[i];
            array2AccessedValue = i;
            drawArray(-1);
            int previousLocation = i == 0 ? -1 : array2[i - 1];
            while (storedValue > previousLocation) {
                array2AccessedValue = i - 1;
                array[storedValue] = i + minVal;
                drawArray(storedValue);
                storedValue--;
            }
        }
        clear(clearType.storage);
        drawFinal("counting sort");
    }

    private synchronized void gnomeSort() {
        shuffle();
        for (int i = 1; i < array.length; i++) {
            for (int j = i - 1; j >= 0; j--) {
                changeType swap = changeType.none;
                if (array[j] > array[j + 1]) {
                    swap = changeType.swap;
                }
                drawArray(swap, j, j + 1);
                if (swap == changeType.none) {
                    break;
                }
            }
        }
        drawFinal("gnome sort");
    }

    private synchronized void heapSortMax() {
        shuffle();
        heapify(0, array.length - 1);
        for (int i = array.length - 1; i > 0; i--) {
            drawArray(changeType.swap, 0, i);
            heapify(0, i - 1);
        }
        drawFinal("heap sort max");
    }

    private synchronized void heapify(int i, int limit) {
        if (2 * i + 1 > limit) {
            return;
        }
        heapify(2 * i + 1, limit);
        changeType swap = changeType.none;
        if (array[i] < array[2 * i + 1]) {
            swap = changeType.swap;
        }
        drawArray(swap, i, 2 * i + 1);

        if (2 * i + 2 > limit) {
            return;
        }
        heapify(2 * i + 2, limit);
        swap = changeType.none;
        if (array[i] < array[2 * i + 2]) {
            swap = changeType.swap;
        }
        drawArray(swap, i, 2 * i + 2);
    }

    private synchronized void insertionSort() {
        shuffle();
        for (int i = 1; i < array.length; i++) {
            storedValue = array[i];
            storedValueAccessed = true;
            drawArray(i);
            int j;
            for (j = i - 1; j >= 0; j--) {
                changeType swap = changeType.none;
                if (storedValue < array[j]) {
                    swap = changeType.replace;
                }
                storedValueAccessed = true;
                drawArray(swap, j, j + 1);
                if (swap == changeType.none) {
                    break;
                }
            }
            array[j + 1] = storedValue;
            storedValueAccessed = true;
            drawArray(j + 1);
        }
        drawFinal("insertion sort");
    }

    private synchronized void mergeSortOutOfPlace() {
        shuffle();
        draw = drawType.outOfPlace;
        redraw = true;
        mergeSortOutOfPlace(0, array.length - 1);
        drawFinal("merge sort out of place");
    }

    private synchronized void mergeSortOutOfPlace(int min, int max) {
        if (min < max) {
            int i = min;
            int j = (min + max) / 2 + 1;
            mergeSortOutOfPlace(i, j - 1);
            mergeSortOutOfPlace(j, max);
            array2 = new int[max - min + 1];
            for (int k = 0; k < max - min + 1; k++) {
                array2AccessedValue = k;
                if (j > max) {
                    array2[k] = array[i];
                    drawArray(i);
                    i++;
                } else if (i > (min + max) / 2) {
                    array2[k] = array[j];
                    drawArray(j);
                    j++;
                } else if (array[i] < array[j]) {
                    array2[k] = array[i];
                    drawArray(changeType.none, i, j);
                    i++;
                } else {
                    array2[k] = array[j];
                    drawArray(changeType.none, i, j);
                    j++;
                }
            }
            for (int k = 0; k < max - min + 1; k++) {
                array2AccessedValue = k;
                array[min + k] = array2[k];
                drawArray(min + k);
            }
            clear(clearType.storage);
        }
    }

    private synchronized void pigeonholeSort() {
        shuffle();
        draw = drawType.buckets;
        redraw = true;
        int[] maxMin = findMaxMin();
        int maxVal = array[maxMin[0]];
        int minVal = array[maxMin[1]];
        buckets = new ArrayList<>(maxVal - minVal + 1);
        for (int i = 0; i < maxVal - minVal + 1; i++) {
            buckets.add(new ArrayList<>());
        }
        for (int i = 0; i < array.length; i++) {
            int element = array[i];
            buckets.get(element - minVal).add(element);
            bucketAccessedValue = new int[]{element - minVal, buckets.get(element - minVal).size() - 1};
            drawArray(i);
            clear(clearType.bucket);
        }
        copyBuckets();
        clear(clearType.storage);
        drawFinal("pigeonhole sort");
    }

    private synchronized void radixSortLSD() {
        final int base = 10;
        shuffle();
        draw = drawType.buckets;
        redraw = true;
        int maxVal = array[findMax(0, array.length - 1)];
        int maxDigits = (int) (Math.log(maxVal)/Math.log(base));
        for (int i = 0; i <= maxDigits; i++) {
            buckets = new ArrayList<>(base);
            for (int j = 0; j < base; j++) {
                buckets.add(new ArrayList<>());
            }
            for (int j = 0; j < array.length; j++) {
                int digit = (int) (array[j] % Math.pow(10, i + 1) / Math.pow(10, i));
                assert digit < 10;
                buckets.get(digit).add(array[j]);
                bucketAccessedValue = new int[]{digit, buckets.get(digit).size() - 1};
                drawArray(j);
                clear(clearType.bucket);
            }
            copyBuckets();
            clear(clearType.storage);
        }
        drawFinal("radix sort");
    }

    private synchronized void selectionSort() {
        shuffle();
        for (int i = 0; i < array.length - 1; i++) {
            int min = findMin(i, array.length - 1);
            drawArray(changeType.swap, i, min);
        }
        drawFinal("selection sort");
    }

    private synchronized void shellSort() {
        final int[] gaps = {1, 4, 10, 23, 57, 132, 301, 701};
        shuffle();
        int gap_i = 0;
        while (gap_i + 1 < gaps.length && gaps[gap_i + 1] < array.length) {
            gap_i++;
        }

        for (int i = gap_i; i >= 0; i--) {
            int gap = gaps[i];
            for (int j = gap; j < array.length; j++) {
                for (int k = j - gap; k >= 0; k -= gap) {
                    changeType swap = changeType.none;
                    if (array[k] > array[k + gap]) {
                        swap = changeType.swap;
                    }
                    drawArray(swap, k, k + gap);
                    if (swap == changeType.none) {
                        break;
                    }
                }
            }
        }
        drawFinal("shell sort");
    }

    private synchronized void treeSort() {

    }

    private synchronized void quickSortInPlaceHoare() {
        shuffle();
        quickSortInPlaceHoare(0, array.length - 1);
        drawFinal("quick sort in place hoare");
    }

    private synchronized void quickSortInPlaceHoare(int min, int max) {
        int p = max; // pivot
        int i = max;
        int j = min;
        while (true) {
            drawArray(changeType.none, i, p);
            while (i > min && array[i] >= array[p]) {
                i--;
                if (i != p) {
                    drawArray(changeType.none, i, p);
                }
            }
            drawArray(changeType.none, j, p);
            while (j < max && array[j] <= array[p]) {
                j++;
                if (j != p) {
                    drawArray(changeType.none, j, p);
                }
            }
            if (i < j) {
                break;
            }
            drawArray(changeType.swap, i, j);
            i--;
            j++;
        }
        i++;
        drawArray(changeType.swap, i, p);
        if (i > min + 1) {
            quickSortInPlaceHoare(min, i - 1);
        }
        if (i < max - 1) {
            quickSortInPlaceHoare(i + 1, max);
        }
    }
}
