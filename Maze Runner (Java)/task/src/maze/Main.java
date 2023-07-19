package maze;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.*;

public class Main {
    static Random random = new Random();
    static Scanner scanner = new Scanner(System.in);

    static StringBuilder outputString = new StringBuilder();
    static boolean moveToNewMenu = false;
    static final String initialMenu = """
            === Menu ===
            1. Generate a new maze
            2. Load a maze
            0. Exit""";
    static final String subsequentMenu = """
            === Menu ===
            1. Generate a new maze
            2. Load a maze
            3. Save the maze
            4. Display the maze
            5. Find the escape
            0. Exit""";
    static int[][] maze;
    static int rows;
    static int columns;
    static int[] entrance;
    static int[] exit;
    static int[][] frontierDirections = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}}; // north, south, east, west

    public static void main(String[] args) {
        initialMenu();
        if (moveToNewMenu) upgradedMenu();
    }

    private static void createUserMaze() {
        System.out.println("Enter the size of a new maze");
        rows = scanner.nextInt();
        columns = rows;
        maze = new int[rows][columns];
        entrance = new int[]{random.nextInt((rows - 2)) + 1, 0};
        mazePrim(entrance[0], entrance[1]);
        generateMaze(maze);
        System.out.println(outputString);
        moveToNewMenu = true;
    }

    private static void upgradedMenu() {
        boolean isExit = false;
        while (!isExit) {
            System.out.println(subsequentMenu);
            try {
                int inputOption = scanner.nextInt();
                switch (inputOption) {
                    case 1  -> createUserMaze();
                    case 2  -> readFromFile();
                    case 3  -> writeToFile();
                    case 4  -> System.out.println(outputString);
                    case 5  -> printSolution();
                    case 0  -> isExit = exit();
                    default -> System.out.println("Incorrect option. Please try again");
                }
            } catch (Exception e) {
                System.out.println("Incorrect option. Please try again");
                e.printStackTrace();
            }
        }
        scanner.close();
    }

    private static boolean exit() {
        System.out.println("Bye!");
        return true;
    }

    public static boolean solveMaze(int row, int col) {
        // Check if the current position is out of bounds or is a wall
        if (row < 0 || row >= rows || col < 0 || col >= columns || maze[row][col] == 0 || maze[row][col] == 2) {
            return false;
        }

        maze[row][col] = 2;

        if (row == exit[0] && col == exit[1]) {
            return true;
        }

        boolean isSolvable = solveMaze( row - 1, col)
                || solveMaze(row + 1, col)
                || solveMaze(row, col - 1)
                || solveMaze(row, col + 1);

        if (!isSolvable) {
            maze[row][col] = 1;
        }
        return isSolvable;
    }

    private static void initialMenu() {
        boolean isExit = false;
        while (!isExit && !moveToNewMenu) {
            System.out.println(initialMenu);
            try {
                int inputOption = scanner.nextInt();
                switch (inputOption) {
                    case 1  -> createUserMaze();
                    case 2  -> readFromFile();
                    case 0  -> isExit = exit();
                    default -> System.out.println("Incorrect option. Please try again");
                }
            } catch (Exception e) {
                System.out.println("Incorrect option. Please try again");
                e.printStackTrace();
            }
        }
    }

    public static void readFromFile() {
        outputString = new StringBuilder();
        String filename = scanner.next();
        try {
            File file = new File("./" + filename);
            Scanner scanner2 = new Scanner(file);

            int i = 0;
            while (scanner2.hasNextLine()) {
                String line = scanner2.nextLine();
                outputString.append(line + "\n");
                i++;
            }

            String[] readLines = outputString.toString().split("\n");
            rows = i;
            columns = readLines[0].length() / 2;
            maze = new int[rows][columns];
            int j = 0;
            for (String line : readLines) {
                int k = 0;
                if (j < rows && k < columns) {
                    String[] lineAsBlocks = splitStringIntoTwoChars(line);
                    for (String twoChars : lineAsBlocks) {
                        int converted = convertStringToInt(twoChars);
                        maze[j][k] = converted;
                        k++;
                    }
                    j++;
                }
            }
            scanner2.close();
            moveToNewMenu = true;
        } catch (FileNotFoundException e) {
            System.out.println("The file " + filename + " does not exist");
        }
    }

    private static int convertStringToInt(String s) {
        return s.equals("  ") ? 1 : 0;
    }

    private static String[] splitStringIntoTwoChars(String input) {
        int length = (input.length() + 1) / 2;
        String[] result = new String[length];

        for (int i = 0, j = 0; i < input.length(); i += 2, j++) {
            result[j] = input.substring(i, Math.min(i + 2, input.length()));
        }

        return result;
    }

    public static void writeToFile() {
        String filename = scanner.next();
        try (FileWriter fileWriter = new FileWriter("./" + filename)) {
            fileWriter.write(String.valueOf(outputString));
        } catch (FileNotFoundException ioE) {
            System.out.println("The file " + filename + " does not exist");
        } catch (Exception ioE) {
            System.out.println("Cannot save the maze. It has an invalid format");
        }
    }

    public static void generateMaze(int[][] maze) {
        encloseMaze(maze);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                outputString.append(maze[i][j] == 0 ? "██" : "  ");
            }
            outputString.append("\n");
        }
    }

    public static void printSolution() {
        boolean foundEntrance = false;
        boolean foundBoth = false;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if ((i == 0 || i == rows - 1 || j == 0 || j == columns - 1)
                        && maze[i][j] == 1) {
                    if (!foundEntrance) {
                        entrance = new int[]{i, j};
                        foundEntrance = true;
                    } else {
                        exit = new int[]{i, j};
                        foundBoth = true;
                    }
                }
                if (foundBoth) break;
            }
        }
        solveMaze(entrance[0], entrance[1]);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                System.out.print(maze[i][j] == 0 ? "██" : maze[i][j] == 2 ? "//" : "  ");
            }
            System.out.println();
        }
    }

    public static void encloseMaze(int[][] maze) {
        int block = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (i == 0 || i == rows - 1 || j == 0 || j == columns - 1) {
                    maze[i][j] = block;
                }
            }
        }
        generateExits(maze);
    }

    private static void generateExits(int[][] maze) {
        int exitRow = random.nextInt((rows - 2)) + 1;
        while (exitRow == entrance[0]) {
            exitRow = random.nextInt((rows - 2)) + 1;
        }
        exit = new int[]{exitRow, columns - 1};
        maze[exitRow][columns - 1] = 1;
        maze[exitRow][columns - 2] = 1;
        maze[exitRow][columns - 3] = 1;

        maze[entrance[0]][entrance[1]] = 1;
    }

    private static void mazePrim(int cR, int cC) {
        int[] current = new int[]{cR, cC};
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{cR, cC});
        while (!stack.isEmpty()) {
            List<int[]> neighbors = generateFrontiers(current[0], current[1]);
            if (!neighbors.isEmpty()) {
                int[] chosenNeighbor = getRandomElementFromList(neighbors);
                int rowInBetween = (current[0] + chosenNeighbor[0]) / 2;
                int colInBetween = (current[1] + chosenNeighbor[1]) / 2;

                maze[chosenNeighbor[0]][chosenNeighbor[1]] = 1;
                maze[rowInBetween][colInBetween] = 1;

                current = chosenNeighbor;
                stack.push(current);
            } else {
                current = stack.pop();
            }
        }
    }

    private static boolean isValidPassage(int row, int col, int[][] grid) {
        return row > 0 && row < rows - 1 && col > 0 && col < columns - 1 && grid[row][col] == 0;
    }

    private static List<int[]> generateFrontiers(int chosenRow, int chosenCol) {
        List<int[]> neighbors = new ArrayList<>();
        for (int[] dir : frontierDirections) {
            int newRow = chosenRow + dir[0];
            int newCol = chosenCol + dir[1];
            if (isValidPassage(newRow, newCol, maze)) {
                neighbors.add(new int[]{newRow, newCol});
            }
        }
        return neighbors;
    }

    private static <T> T getRandomElementFromList(List<T> list) {
        Random random = new Random();
        int randomIndex = random.nextInt(list.size());

        return list.get(randomIndex);
    }
}