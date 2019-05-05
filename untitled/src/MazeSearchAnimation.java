import java.awt.*;
import java.applet.*;
import java.util.*;

// todo: (1) convert to swing

public class MazeSearchAnimation extends Applet implements Runnable {
    private static final int SIZE = 50;
    private static final int GRID_SIZE = 15;
    private static final int DELAY_TIME = 0;
    private static final int NUM_TRIALS = 100;
    private static final int WALL_DENSITY = 25;
    private static final int GUARANTEED_EMPTY = 4;

    private int[][] maze = new int[SIZE][SIZE];
    private Coordinate accessedCoordinate = new Coordinate(-1, -1);
    private Coordinate _accessedCoordinate = new Coordinate(-1, -1);

    private int numOperations = 0;

    private boolean redraw = true;

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

    @Override
    public synchronized void run() {
        setSize(1200, 800);
        displaySearch(this::bestFirstSearch);
        System.exit(0);
    }

    @Override
    public synchronized void update(Graphics g) {
        paint(g);
    }

    @Override
    public synchronized void paint(Graphics g) {
        numOperations++;
        if (redraw) {
            for (int i = 0; i < maze.length; i++) {
                for (int j = 0; j < maze[0].length; j++) {
                    int mazeElement = maze[i][j];
                    if (i == accessedCoordinate.getY() && j == accessedCoordinate.getX()) {
                        g.setColor(Color.GREEN);
                    } else {
                        switch (mazeElement) {
                            case 0: g.setColor(Color.WHITE);
                                break;
                            case 1: g.setColor(Color.GRAY);
                                break;
                            case 2: g.setColor(Color.BLUE);
                                break;
                            case 9: g.setColor(Color.YELLOW);
                                break;
                        }
                    }
                    g.fillRect(50 + i * GRID_SIZE, 50 + j * GRID_SIZE, GRID_SIZE, GRID_SIZE);
                }
            }
        } else {
            _accessed: {
                int _x = _accessedCoordinate.getX();
                int _y = _accessedCoordinate.getY();
                if (_x != -1 && _y != -1) {
                    int mazeElement = maze[_y][_x];
                    switch (mazeElement) {
                        case 0:
                            break _accessed;
                        case 1:
                            g.setColor(Color.GRAY);
                            break;
                        case 2:
                            g.setColor(Color.BLUE);
                            break;
                        case 9:
                            g.setColor(Color.YELLOW);
                            break;
                    }
                    g.fillRect(50 + _y * GRID_SIZE, 50 + _x * GRID_SIZE, GRID_SIZE, GRID_SIZE);
                }
            }

            int x = accessedCoordinate.getX();
            int y = accessedCoordinate.getY();
            if (x != -1 && y != -1) {
                g.setColor(Color.GREEN);
                g.fillRect(50 + y * GRID_SIZE, 50 + x * GRID_SIZE, GRID_SIZE, GRID_SIZE);
            }
        }
        redraw = false;
        _accessedCoordinate = accessedCoordinate;
        accessedCoordinate = new Coordinate(-1, -1);
        try {
            Thread.sleep(DELAY_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        notifyAll();
    }

    private synchronized void initialize() {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                maze[i][j] = Math.random() * 100 < WALL_DENSITY ? 1 : 0;
            }
        }
        int destX = maze.length - 1;
        int destY = maze[maze.length - 1].length - 1;
        for (int i = 0; i < GUARANTEED_EMPTY; i++) {
            for (int j = 0; j < GUARANTEED_EMPTY; j++) {
                maze[i][j] = 0;
                maze[destX - i][destY - j] = 0;
            }
        }
        maze[destX][destY] = 9;
        redraw = true;
    }

    private synchronized void displaySearch(Search search) {
        int counter = 0;
        int successfulOperations = 0;
        int unsuccessfulOperations = 0;
        for (int i = 0; i < NUM_TRIALS; i++) {
            initialize();
            if (search.executeSearch(0, 0)) {
                counter++;
                successfulOperations += numOperations;
            } else {
                unsuccessfulOperations += numOperations;
            }
            numOperations = 0;
        }
        System.out.println("% successful: " + 100 * counter / NUM_TRIALS);
        if (counter != 0) {
            System.out.println("avg # of operations (successful): " + successfulOperations / counter);
        } else {
            System.out.println("no successful operations");
        }
        if (counter != NUM_TRIALS){
            System.out.println("avg # of operations (unsuccessful): " + unsuccessfulOperations / (NUM_TRIALS - counter));
        } else {
            System.out.println("no unsuccessful operations");
        }
    }

    private synchronized boolean depthFirstSearch(int r, int c) {
        if (maze[r][c] == 1 || maze[r][c] == 2) {
            return false;
        } else if (maze[r][c] == 9) {
            accessedCoordinate = new Coordinate(-1, -1);
            repaint();
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            maze[r][c] = 2;
            accessedCoordinate = new Coordinate(c, r);
            repaint();
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return (c + 1) <= maze[r].length - 1 && depthFirstSearch(r, c + 1)
                    || (r + 1) <= maze.length - 1 && depthFirstSearch(r + 1, c)
                    || (r - 1) >= 0 && depthFirstSearch(r - 1, c)
                    || (c - 1) >= 0 && depthFirstSearch(r, c - 1);
        }
    }

    private synchronized boolean breadthFirstSearch(int r0, int c0) {
        Queue<Coordinate> searchCoordinates = new LinkedList<>();
        Queue<Coordinate> _searchCoordinates = new LinkedList<>();
        _searchCoordinates.add(new Coordinate(c0, r0));
        while (!_searchCoordinates.isEmpty()) {
            for (Coordinate coordinate : _searchCoordinates) {
                int r = coordinate.getY();
                int c = coordinate.getX();
                if (maze[r][c] == 9) {
                    accessedCoordinate = new Coordinate(-1, -1);
                    repaint();
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return true;
                } else if (maze[r][c] != 1 && maze[r][c] != 2) {
                    maze[r][c] = 2;
                    accessedCoordinate = new Coordinate(c, r);
                    repaint();
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (c + 1 <= maze[r].length - 1) {
                        searchCoordinates.add(new Coordinate(c + 1, r));
                    }
                    if (r + 1 <= maze.length - 1) {
                        searchCoordinates.add(new Coordinate(c, r + 1));
                    }
                    if (r - 1 >= 0) {
                        searchCoordinates.add(new Coordinate(c, r - 1));
                    }
                    if (c - 1 >= 0) {
                        searchCoordinates.add(new Coordinate(c - 1, r));
                    }
                }
            }
            _searchCoordinates = searchCoordinates;
            searchCoordinates = new LinkedList<>();
        }
        return false;
    }

    private synchronized boolean bestFirstSearch(int r0, int c0) {
        PriorityQueue<Coordinate> searchCoordinates = new PriorityQueue<>();
        searchCoordinates.add(new Coordinate(c0, r0));
        while (!searchCoordinates.isEmpty()) {
            Coordinate coordinate = searchCoordinates.poll();
            int r = coordinate.getY();
            int c = coordinate.getX();
            if (maze[r][c] == 9) {
                accessedCoordinate = new Coordinate(-1, -1);
                repaint();
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            } else if (maze[r][c] != 1 && maze[r][c] != 2) {
                maze[r][c] = 2;
                accessedCoordinate = new Coordinate(c, r);
                repaint();
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (c + 1 <= maze[r].length - 1) {
                    searchCoordinates.add(new Coordinate(c + 1, r));
                }
                if (r + 1 <= maze.length - 1) {
                    searchCoordinates.add(new Coordinate(c, r + 1));
                }
                if (r - 1 >= 0) {
                    searchCoordinates.add(new Coordinate(c, r - 1));
                }
                if (c - 1 >= 0) {
                    searchCoordinates.add(new Coordinate(c - 1, r));
                }
            }
        }
        return false;
    }

    // lol i'm technically not supposed to know where the exit is
    private synchronized int heuristic(int c, int r) {
        return r - maze.length + c - maze[maze.length - 1].length;
    }

    private interface Search {
        boolean executeSearch(int r, int c);
    }

    private class Coordinate implements Comparable<Coordinate> {
        private int x;
        private int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int compareTo(Coordinate o) {
            return Integer.compare(heuristic(o.x, o.y), heuristic(x, y));
        }
    }
}
