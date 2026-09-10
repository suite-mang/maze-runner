package maze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class Main {

    private static final char WALL_CHAR = '█';
    private static final char PASSAGE_CHAR = ' ';
    private static final char ESCAPE_CHAR = '/';

    private static final BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

    public static void main(String[] args) throws IOException {
        char[][] currentMaze = null;
        boolean firstMenu = true;

        while (true) {
            printMenu(currentMaze != null, firstMenu);
            firstMenu = false;

            String choice = reader.readLine();
            if (choice == null) {
                break;
            }
            choice = choice.trim();

            switch (choice) {
                case "1":
                    currentMaze = generateMaze();
                    printMaze(currentMaze);
                    break;
                case "2":
                    char[][] loaded = loadMaze();
                    if (loaded != null) {
                        currentMaze = loaded;
                    }
                    break;
                case "3":
                    if (currentMaze == null) {
                        System.out.println("Incorrect option. Please try again");
                    } else {
                        saveMaze(currentMaze);
                    }
                    break;
                case "4":
                    if (currentMaze == null) {
                        System.out.println("Incorrect option. Please try again");
                    } else {
                        printMaze(currentMaze);
                    }
                    break;
                case "5":
                    if (currentMaze == null) {
                        System.out.println("Incorrect option. Please try again");
                    } else {
                        findEscape(currentMaze);
                    }
                    break;
                case "0":
                    System.out.println("Bye!");
                    return;
                default:
                    System.out.println("Incorrect option. Please try again");
            }
        }
    }

    private static void printMenu(boolean hasMaze, boolean first) {
        StringBuilder menu = new StringBuilder();
        if (!first) {
            menu.append(System.lineSeparator());
        }
        menu.append("=== Menu ===").append(System.lineSeparator());
        menu.append("1. Generate a new maze").append(System.lineSeparator());
        menu.append("2. Load a maze").append(System.lineSeparator());
        if (hasMaze) {
            menu.append("3. Save the maze").append(System.lineSeparator());
            menu.append("4. Display the maze").append(System.lineSeparator());
            menu.append("5. Find the escape").append(System.lineSeparator());
        }
        menu.append("0. Exit");
        System.out.println(menu);
    }

    private static char[][] generateMaze() throws IOException {
        System.out.println("Enter the size of a new maze");
        int size = Integer.parseInt(reader.readLine().trim());
        return new MazeGenerator(size, size).generate();
    }

    private static void printMaze(char[][] maze) {
        StringBuilder output = new StringBuilder();
        for (char[] row : maze) {
            for (char cell : row) {
                output.append(cell).append(cell);
            }
            output.append(System.lineSeparator());
        }
        System.out.print(output);
    }

    private static char[][] loadMaze() throws IOException {
        String path = reader.readLine().trim();

        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(path), StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println("The file " + path + " does not exist");
            return null;
        }

        try {
            return parseMaze(lines);
        } catch (IllegalArgumentException e) {
            System.out.println("Cannot load the maze. It has an invalid format.");
            return null;
        }
    }

    private static char[][] parseMaze(List<String> lines) {
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("The maze file is empty.");
        }

        int width = lines.getFirst().length();
        if (width == 0) {
            throw new IllegalArgumentException("The maze file has an empty first line.");
        }

        char[][] maze = new char[lines.size()][width];
        for (int r = 0; r < lines.size(); r++) {
            String line = lines.get(r);
            if (line.length() != width) {
                throw new IllegalArgumentException("Rows have inconsistent lengths.");
            }
            for (int c = 0; c < width; c++) {
                char ch = line.charAt(c);
                if (ch != WALL_CHAR && ch != PASSAGE_CHAR) {
                    throw new IllegalArgumentException("Unknown character found: " + ch);
                }
                maze[r][c] = ch;
            }
        }
        return maze;
    }

    private static void findEscape(char[][] maze) {
        int height = maze.length;
        int width = maze[0].length;
        int entranceCol = findOpening(maze[0]);
        int exitCol = findOpening(maze[height - 1]);

        int[][] prevRow = new int[height][width];
        int[][] prevCol = new int[height][width];
        boolean[][] visited = new boolean[height][width];
        for (int[] row : prevRow) {
            Arrays.fill(row, -1);
        }
        for (int[] row : prevCol) {
            Arrays.fill(row, -1);
        }

        //adjacent block of current block: up/down/left/right
        int[] rowStep = {-1, 1, 0, 0};
        int[] colStep = {0, 0, -1, 1};

        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{0, entranceCol});
        visited[0][entranceCol] = true;

        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int row = cell[0];
            int col = cell[1];
            if (row == height - 1 && col == exitCol) {
                break;
            }
            for (int d = 0; d < 4; d++) {
                int nextRow = row + rowStep[d];
                 int nextCol = col + colStep[d];
                if (nextRow < 0 || nextRow >= height || nextCol < 0 || nextCol >= width) {
                    continue;
                }
                if (visited[nextRow][nextCol] || maze[nextRow][nextCol] != PASSAGE_CHAR) {
                    continue;
                }
                visited[nextRow][nextCol] = true;
                prevRow[nextRow][nextCol] = row;
                prevCol[nextRow][nextCol] = col;
                queue.add(new int[]{nextRow, nextCol});
            }
        }

        char[][] pathMaze = new char[height][width];
        for (int r = 0; r < height; r++) {
            pathMaze[r] = maze[r].clone();
        }

        int row = height - 1;
        int col = exitCol;
        while (row != -1) {
            pathMaze[row][col] = ESCAPE_CHAR;
            int prevR = prevRow[row][col];
            int prevC = prevCol[row][col];
            row = prevR;
            col = prevC;
        }

        printMaze(pathMaze);
    }

    private static int findOpening(char[] borderRow) {
        for (int c = 0; c < borderRow.length; c++) {
            if (borderRow[c] == PASSAGE_CHAR) {
                return c;
            }
        }
        throw new IllegalStateException("The maze has no opening on this border.");
    }

    private static void saveMaze(char[][] maze) throws IOException {
        String path = reader.readLine().trim();

        StringBuilder content = new StringBuilder();
        for (char[] row : maze) {
            content.append(new String(row)).append(System.lineSeparator());
        }

        try {
            Files.writeString(Path.of(path), content.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Cannot save the maze to " + path + ".");
        }
    }
}
