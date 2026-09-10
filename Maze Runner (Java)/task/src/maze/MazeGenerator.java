package maze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Generates a random maze of a given size as a minimal spanning tree
 * over a grid of rooms, using randomized Kruskal's algorithm.
 */
public class MazeGenerator {

    private static final char WALL_CHAR = '█';
    private static final char PASSAGE_CHAR = ' ';

    private final Random random = new Random();
    private final int height;
    private final int width;
    private final int roomRows;
    private final int roomCols;
    private final boolean[][] wall;

    public MazeGenerator(int height, int width) {
        this.height = height;
        this.width = width;
        this.roomRows = Math.max(1, (height - 1) / 2);
        this.roomCols = Math.max(1, (width - 1) / 2);
        this.wall = new boolean[height][width];
    }

    public char[][] generate() {
        fillWithWalls();
        carveRooms();
        carvePassagesForSpanningTree();
        carveEntranceAndExit();
        return toCharGrid();
    }

    private void fillWithWalls() {
        for (boolean[] row : wall) {
            Arrays.fill(row, true);
        }
    }

    private void carveRooms() {
        for (int i = 0; i < roomRows; i++) {
            for (int j = 0; j < roomCols; j++) {
                wall[2 * i + 1][2 * j + 1] = false;
            }
        }
    }

    private void carvePassagesForSpanningTree() {
        List<int[]> edges = collectRoomEdges();
        Collections.shuffle(edges, random);

        DisjointSet rooms = new DisjointSet(roomRows * roomCols);
        for (int[] edge : edges) {
            int roomA = edge[0];
            int roomB = edge[1];
            int wallRow = edge[2];
            int wallCol = edge[3];
            if (rooms.union(roomA, roomB)) {
                wall[wallRow][wallCol] = false;
            }
        }
    }

    // Each edge connects        two adjacent rooms through the wall cell between them.
    private List<int[]> collectRoomEdges() {
        List<int[]> edges = new ArrayList<>();
        for (int i = 0; i < roomRows; i++) {
            for (int j = 0; j <      roomCols; j++) {
                int room = i * roomCols + j;
                if (j + 1 < roomCols) {
                    int rightRoom = i * roomCols + (j + 1);
                    edges.add(new int[]{room, rightRoom, 2 * i + 1, 2 * j + 2});
                }
                if (i + 1 < roomRows) {
                    int downRoom = (i + 1) * roomCols + j;
                    edges.add(new int[]{room, downRoom, 2 * i + 2, 2 * j + 1});
                }
            }
        }
        return edges;
    }

    private void carveEntranceAndExit() {
        int entranceCol = 2 * random.nextInt(roomCols) + 1;
        carveStraightFromBorder(0, entranceCol, 1, 0);

        int exitCol = 2 * random.nextInt(roomCols) + 1;
        carveStraightFromBorder(height - 1, exitCol, -1, 0);
    }

    // Carves a straight line of wall cells starting at a border cell until
    // it reaches an already-open roo   m, handling any leftover padding row.
    private void carveStraightFromBorder(int row, int col, int rowStep, int  colStep) {
        while (wall[row][col]) {
            wall[row][col] = false;
            row += rowStep;
            col += colStep;
        }
    }

    private char[][] toCharGrid() {
        char[][] chars = new char[height][width];
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                chars[r][c] = wall[r][c] ? WALL_CHAR : PASSAGE_CHAR;
            }
        }
        return chars;
    }

    private static final class DisjointSet {
        private final int[] parent;
        private final int[] rank;

        DisjointSet(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
            }
        }
        int find(int x) {
            while (parent[x] != x) {
                parent[x] = parent[parent[x]];
                x = parent[x];
            }
            return x;
        }

        boolean union(int a, int b) {
            int rootA = find(a);
            int rootB = find(b);
            // already connected, do nothing
            if (rootA == rootB) {
                return false;
            }
            if (rank[rootA] < rank[rootB]) {
                parent[rootA] = rootB;
            } else if (rank[rootA] > rank[rootB]) {
                   parent[rootB] = rootA;
            } else {
                // only increases when merging two equally-tall trees
                parent[rootB] = rootA;
                rank[rootA]++;
            }
            return true;
        }
    }
}
