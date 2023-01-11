import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class KamilAlmetov {
    public static void main(String[] args) throws IOException {
        Map map = new Map();
        map.generateMap();

        long backtrackingTime = System.currentTimeMillis();
        ArrayList<Cell> pathBacktracking = SearchAlgorithms.backtracking(map);
        backtrackingTime = System.currentTimeMillis() - backtrackingTime;

        long AStarTime = System.currentTimeMillis();
        ArrayList<Cell> pathAStar = SearchAlgorithms.AStar(map);
        AStarTime = System.currentTimeMillis() - AStarTime;

        printResultToTheOutputFile("outputBacktracking.txt", map, pathBacktracking, backtrackingTime);
        printResultToTheOutputFile("outputAStar.txt", map, pathAStar, AStarTime);
    }

    /**
     * Prints the shortest path to the output file and all corresponding information specified in the assignment
     *
     * @throws FileNotFoundException if the file doesn't exist
     */
    public static void printResultToTheOutputFile(String pathOfOutputFile, Map map, ArrayList<Cell> shortestPath, long time) throws FileNotFoundException {
        File output = new File(pathOfOutputFile);
        PrintWriter printWriter = new PrintWriter(output);

        if (shortestPath == null) {
            printWriter.println("Lose");
        } else {
            printWriter.println("Win");
            for (var cell : shortestPath) {
                printWriter.printf(cell + " ");
            }
            printWriter.println();
            printWriter.println(map.mapWithPathToString(shortestPath));
            printWriter.println(time + " ms");
        }
        printWriter.close();
    }


}

class SampleTest {
    public static void main(String[] args) throws Exception {
        Map map = new Map();
        int wins = 0, losses = 0;
        int numberOfSamples = 10000;
        // Probability density function for backtracking runs, for A* runs
        HashMap<Long, Integer> pdfBacktracking1 = new HashMap<>(), pdfBacktracking2 = new HashMap<>(),
                pdfAStar1 = new HashMap<>(), pdfAStar2 = new HashMap<>();
        // Sample space for backtracking runs, for A* runs
        ArrayList<Long> sampleBacktracking1 = new ArrayList<>(numberOfSamples),
                sampleBacktracking2 = new ArrayList<>(numberOfSamples),
                sampleAStar1 = new ArrayList<>(numberOfSamples),
                sampleAStar2 = new ArrayList<>(numberOfSamples);
        for (int i = 0; i < numberOfSamples; ++i) {
            map.generateMapRandomly();
            map.typeOfScenario = 1;
            int backtrackingPathLength1 = collectSamples(map, pdfBacktracking1, sampleBacktracking1, "Backtracking");
            int aStarPathLength1 = collectSamples(map, pdfAStar1, sampleAStar1, "A*");
            map.typeOfScenario = 2;
            int backtrackingPathLength2 = collectSamples(map, pdfBacktracking2, sampleBacktracking2, "Backtracking");
            int aStarPathLength2 = collectSamples(map, pdfAStar2, sampleAStar2, "A*");
            // if algorithms obtained different path lengths, one of them (at least) is incorrect
            // print map and additional information to find and fix bugs.
            if (backtrackingPathLength1 != aStarPathLength1 || backtrackingPathLength2 != aStarPathLength2) {

                System.out.println(map);
                System.out.println(map.unitsToString());
                System.out.println(backtrackingPathLength1 + " " + backtrackingPathLength2 + " " + aStarPathLength1 + " " + aStarPathLength2);
                throw new Exception("ERROR");
            }
            map.clear();

            // count wins and losses
            // since all the paths should be equal, it doesn't matter which one is checked
            if (backtrackingPathLength1 > 0) {
                wins++;
            } else {
                losses++;
            }
        }

        // print statistics
        FileWriter statistics = new FileWriter("statistics.txt");
        statistics.close();
        printStatistics(wins, losses, sampleBacktracking1, pdfBacktracking1, "Backtracking", 1);
        printStatistics(wins, losses, sampleAStar1, pdfAStar1, "A*", 1);
        printStatistics(wins, losses, sampleBacktracking2, pdfBacktracking2, "Backtracking", 2);
        printStatistics(wins, losses, sampleAStar2, pdfAStar2, "A*", 2);
    }

    public static int collectSamples(Map map, HashMap<Long, Integer> pdf, ArrayList<Long> sample, String algorithmName) {
        long time;
        ArrayList<Cell> path;
        if (algorithmName.equals("Backtracking")) {
            time = System.currentTimeMillis();
            path = SearchAlgorithms.backtracking(map);
            time = System.currentTimeMillis() - time;
        } else {
            time = System.currentTimeMillis();
            path = SearchAlgorithms.AStar(map);
            time = System.currentTimeMillis() - time;
        }
        int pathLength = path != null ? path.size() : 0;
        map.clearDistances();
        map.clearAStarData();
        // fill sample spaces and pdfs
        sample.add(time);
        if (!pdf.containsKey(time)) {
            pdf.put(time, 0);
        } else {
            pdf.put(time, pdf.get(time) + 1);
        }

        return pathLength;
    }

    public static void printStatistics(int wins, int losses, ArrayList<Long> sample, HashMap<Long, Integer> pdf, String algorithmName, int typeOfScenario) throws IOException {
        File statistics = new File("statistics.txt");
        PrintWriter writer = new PrintWriter(new FileOutputStream(statistics, true));
        int n = wins + losses;
        double mean = sample.stream().mapToDouble(value -> value).sum() / n;
        var tmp = pdf.entrySet().stream().max(java.util.Map.Entry.comparingByValue());
        long mode = 0;
        if (tmp.isPresent()) {
            mode = tmp.get().getKey();
        }
        sample.sort(null);
        double median = (sample.get(n / 2 - 1) + sample.get(n / 2)) * 1. / 2;
        double standardDeviation = Math.sqrt(sample.stream().mapToDouble(e -> (e - mean) * (e - mean)).sum() / n);
        //System.out.println(sample);
        writer.printf("""
                         %s (%d variant):
                         Mean: %.2f ms
                         Mode: %d ms
                         Median: %.2f ms
                         Standard deviation: %.2f ms
                         Number of wins: %d
                         Number of losses: %d
                         Percent of wins: %.2f %%
                         Percent of losses: %.2f %%
                        
                        """, algorithmName, typeOfScenario, mean, mode, median, standardDeviation,
                wins, losses, wins * 1. / n * 100, losses * 1. / n * 100);
        writer.close();
    }
}

/**
 * Class that represents a cell on the map.
 */
class Cell implements Comparable<Cell> {
    int y, x;
    /**
     * Type of the cell (Jack Sparrow, Kraken, etc.)
     */
    CellType type;
    /**
     * distance from the starting cell to the current one that is used in the backtracking algorithm.
     */
    int d = Integer.MAX_VALUE;
    /**
     * the previous cell in the path generated by A* algorithm
     */
    Cell parent;
    /**
     * Values needed for the A* algorithm.
     * g - distance from the starting cell to the current one.
     * h - heuristic (diagonal distance from the current cell to the final one)
     */
    int f, g, h;
    boolean hasVisitedTortuga, isKrakenKilled;

    Cell(CellType type, int y, int x) {
        this.type = type;
        this.y = y;
        this.x = x;
    }

    @Override
    public String toString() {
        return "[" + y + "," + x + "]";
    }

    /**
     * Method that compares two cells by f - distance from the initial point to the final point
     *
     * @param o the object to be compared.
     */
    @Override
    public int compareTo(Cell o) {
        if (this.f == o.f) {
            return this.h - o.h;
        }
        return this.f - o.f;
    }
}

/**
 * Possible types of the cell
 */
enum CellType {
    JackSparrow("\uD83D\uDC24"),
    DavyJones("\uD83D\uDC80"),
    Kraken("\uD83E\uDD91"),
    Rock("\uD83E\uDEA8"),
    DeadManChest("\uD83E\uDE99"),
    Tortuga("\uD83C\uDF7A"),
    PerceptionZone("\uD83D\uDEAB"),
    SeaCell("\uD83C\uDF0A"),
    KrakenBelowRock("\uD83D\uDC7E"),
    JackOnTortuga("\uD83E\uDD9C"),
    PathCell("\uD83D\uDFE2");

    /**
     * Graphical representation of each of the types.
     */
    final String emoji;
    /**
     * Number of units whose locations need to be generated or taken from the input file.
     * (First 6 types).
     */
    final static int NUMBER_OF_UNITS = 6;

    CellType(String emoji) {
        this.emoji = emoji;
    }
}

/**
 * Class that represents a map.
 */
class Map {
    Cell[][] cells;
    /**
     * HashMap of the units whose locations are generated / taken from the input file.
     */
    HashMap<CellType, Cell> units;
    int size;
    int typeOfScenario;

    public Map() {
        size = 9;
        cells = new Cell[size][size];
        units = new HashMap<>(CellType.NUMBER_OF_UNITS);
    }

    public Map(Map map) {
        this.size = map.size;
        this.cells = Arrays.stream(map.cells).map(Cell[]::clone).toArray(Cell[][]::new);
        this.units = new HashMap<>(map.units);
        this.typeOfScenario = map.typeOfScenario;
    }

    public void clear() {
        cells = new Cell[size][size];
        units = new HashMap<>(6);
    }

    /**
     * This method was used to print the arrangement of the maps that were led to fail of an algorithm.
     *
     * @return the current arrangement of the units in the format specified for the input file as a string.
     */
    public String unitsToString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < CellType.NUMBER_OF_UNITS; ++i) {
            builder.append(units.get(CellType.values()[i]));
            if (i < CellType.NUMBER_OF_UNITS - 1) builder.append(" ");
        }
        return builder.toString();
    }

    /**
     * @return the 2D-representation of the map as a string
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                builder.append(cells[i][j].type.emoji);
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * Method that generates the map either randomly or by using the input file.
     */
    public void generateMap() {
        Scanner in = new Scanner(System.in);
        String answer = "0";
        while (!answer.equals("1") && !answer.equals("2")) {
            System.out.println("""
                    Choose the input type (1 or 2):
                    1 - generate the map and manually insert perception scenario from console
                    2 - insert the positions of agents and perception scenario from the input.txt""");
            if (in.hasNext()) {
                answer = in.next();
                if (!answer.equals("1") && !answer.equals("2")) {
                    System.out.println("You typed something wrong. Just type 1 or 2");
                }
            }
        }
        if (answer.equals("1")) {
            generateMapRandomly();
            System.out.println("Map is successfully generated!");
            System.out.println(this);
            String typeOfScenarioStr = "0";
            while (!typeOfScenarioStr.equals("1") && !typeOfScenarioStr.equals("2")) {
                System.out.println("Choose the perception scenario (1 or 2) as specified in the assignment:");
                if (in.hasNext()) {
                    typeOfScenarioStr = in.next();
                    if (!typeOfScenarioStr.equals("1") && !typeOfScenarioStr.equals("2")) {
                        System.out.println("You typed something wrong. Just type 1 or 2");
                    }
                }
            }
            typeOfScenario = Integer.parseInt(typeOfScenarioStr);
            System.out.println("Perception scenario: " + typeOfScenario);
        } else {
            while (true) {
                try {
                    readInput();
                    generateFromInputFile();
                    System.out.println("The map from file is successfully generated");
                    System.out.println(this);
                    System.out.println("Perception scenario: " + typeOfScenario);
                    break;
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Fix the file issue (don't forget to save the file) and type anything to console to try again");
                    in.next();
                }
            }

        }
        in.close();
    }

    /**
     * Method that marks the perception zones of an enemy on the map.
     *
     * @param enemy enemy unit with perception zones.
     */
    private void putPerceptionZones(Cell enemy) {
        assert enemy.type == CellType.Kraken || enemy.type == CellType.DavyJones
                || enemy.type == CellType.KrakenBelowRock;
        for (int j = Math.max(enemy.y - 1, 0); j <= Math.min(enemy.y + 1, 9 - 1); ++j) {
            for (int k = Math.max(enemy.x - 1, 0); k <= Math.min(enemy.x + 1, 9 - 1); ++k) {
                // the cell where the Kraken or Davy Jones stays themselves
                if (j == enemy.y && k == enemy.x) continue;
                // don't fill the diagonal cell as perception zones if it's the Kraken
                if (j != enemy.y && k != enemy.x &&
                        (enemy.type == CellType.Kraken || enemy.type == CellType.KrakenBelowRock)) continue;
                if (cells[j][k] == null) {
                    cells[j][k] = new Cell(CellType.PerceptionZone, j, k);
                }
            }
        }
    }

    /**
     * Method that generates sea cells after all other types of units have been generated.
     */
    private void generateSeaCells() {
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (cells[i][j] == null) {
                    cells[i][j] = new Cell(CellType.SeaCell, i, j);
                }
            }
        }
    }

    public void generateMapRandomly() {
        Random random = new Random();
        boolean[] used = new boolean[CellType.NUMBER_OF_UNITS];
        for (int i = 0; i < CellType.NUMBER_OF_UNITS; ++i) {
            CellType type = CellType.values()[i];
            while (!used[i]) {
                int temp = random.nextInt(0, 9 * 9);
                int y, x;
                if (type == CellType.JackSparrow) {
                    y = 0;
                    x = 0;
                } else {
                    y = temp / 9;
                    x = temp % 9;
                }
                Cell cell = new Cell(type, y, x);
                if (canPutUnit(cell)) {
                    putUnit(cell);
                    units.put(type, cell);
                    used[i] = true;
                }
            }
        }
        generateSeaCells();
    }

    /**
     * @return true if it's not allowed to put a perception zone at cells[y][x]
     */
    private boolean cannotPutPerceptionZone(int y, int x) {
        if (y < 0 || y >= size || x < 0 || x >= size) return true;
        return (cells[y][x] != null && cells[y][x].type != CellType.Kraken
                && cells[y][x].type != CellType.KrakenBelowRock
                && cells[y][x].type != CellType.Rock
                && cells[y][x].type != CellType.DavyJones
                && cells[y][x].type != CellType.PerceptionZone);
    }

    /**
     * @param cell the cell we are checking
     * @return true if we can put the cell (with its cell.y, cell.x, and cell.type) on the map
     */
    private boolean canPutUnit(Cell cell) {
        assert cell.type != CellType.PerceptionZone && cell.type != CellType.SeaCell
                && cell.type != CellType.KrakenBelowRock && cell.type != CellType.JackOnTortuga;
        Cell cellOnMap = cells[cell.y][cell.x];
        switch (cell.type) {
            case JackSparrow -> {
                return cellOnMap == null || cellOnMap.type == CellType.Tortuga;
            }
            case Rock -> {
                return cellOnMap == null || cellOnMap.type == CellType.PerceptionZone || cellOnMap.type == CellType.Kraken;
            }
            case DeadManChest -> {
                return cellOnMap == null;
            }
            case Tortuga -> {
                return cellOnMap == null || cellOnMap.type == CellType.JackSparrow;
            }
            case Kraken, DavyJones -> {
                if (cell.type == CellType.Kraken && cellOnMap != null && cellOnMap.type == CellType.Rock) return true;
                if (cellOnMap != null && cellOnMap.type != CellType.PerceptionZone) return false;
                for (int i = cell.y - 1; i < cell.y + 1; ++i) {
                    for (int j = cell.x - 1; j < cell.x + 1; ++j) {
                        if (i == cell.y && j == cell.x) continue;
                        if (cell.type == CellType.Kraken) {
                            if (i == cell.y || j == cell.x) {
                                if (cannotPutPerceptionZone(i, j)) {
                                    return false;
                                }
                            }
                        } else {
                            if (cannotPutPerceptionZone(i, j)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Puts cell to its place on te map
     *
     * @param cell the cell we're putting on the map.
     */
    private void putUnit(Cell cell) {
        assert canPutUnit(cell);
        if (cells[cell.y][cell.x] != null && cells[cell.y][cell.x].type != CellType.PerceptionZone) {
            switch (cell.type) {
                case Kraken, Rock -> {
                    cell.type = CellType.KrakenBelowRock;
                    cells[cell.y][cell.x] = cell;
                }
                case JackSparrow, Tortuga -> {
                    cell.type = CellType.JackOnTortuga;
                    cells[cell.y][cell.x] = cell;
                }
            }
        } else {
            cells[cell.y][cell.x] = cell;
        }
        switch (cell.type) {
            case Kraken, KrakenBelowRock, DavyJones -> putPerceptionZones(cell);
        }
    }

    /**
     * Generates map using information from the input file.
     * We assume that at the time this method runs, units hashmap is already filled with the input information.
     *
     * @throws IOException if the location of units is invalid.
     */
    private void generateFromInputFile() throws IOException {
        CellType[] orderedTypes = CellType.values();
        for (int i = 0; i < CellType.NUMBER_OF_UNITS; ++i) {
            Cell currentUnit = units.get(orderedTypes[i]);
            if (!canPutUnit(currentUnit)) {
                throw new IOException("Location of units is invalid. Fix it and rerun the program.");
            }
            putUnit(currentUnit);
        }
        generateSeaCells();
    }

    /**
     * Checks if the input specified in the file is valid.
     *
     * @throws FileNotFoundException if the file doesn't exist.
     */
    private boolean isValidInput() throws FileNotFoundException {
        File input = new File("input.txt");
        Scanner in = new Scanner(input);
        in.useDelimiter("\n");
        String regEx = "^\\[0,0]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]";
        String regEx2 = "[12]";
        Pattern pattern = Pattern.compile(regEx);
        if (!in.hasNext(pattern)) {
            in.close();
            return false;
        }
        in.next();
        if (!in.hasNext(Pattern.compile(regEx2))) {
            in.close();
            return false;
        }
        in.close();
        return true;
    }

    /**
     * Reads input from the file.
     *
     * @throws IOException if the input is invalid or the file doesn't exist.
     */
    private void readInput() throws IOException {
        if (!isValidInput()) {
            throw new IOException("The input is invalid. Fix it and rerun the program.");
        }
        File input = new File("input.txt");
        Scanner in = new Scanner(input);
        CellType[] unitTypesOrdered = {CellType.JackSparrow, CellType.DavyJones, CellType.Kraken, CellType.Rock,
                CellType.DeadManChest, CellType.Tortuga};

        for (int i = 0; i < 6; ++i) {
            String unitDataStr = in.next();
            Cell unitCell = new Cell(unitTypesOrdered[i], unitDataStr.charAt(1) - '0', unitDataStr.charAt(3) - '0');
            units.put(unitTypesOrdered[i], unitCell);
        }
        typeOfScenario = in.next().charAt(0) - '0';
        in.close();
    }

    /**
     * Checks if the cell is the Kraken weakness
     *
     * @param cell considering cell.
     */
    public boolean isKrakenWeakness(Cell cell) {
        Cell kraken;
        if (units.containsKey(CellType.Kraken)) {
            kraken = units.get(CellType.Kraken);
        } else {
            kraken = units.get(CellType.KrakenBelowRock);
        }
        return isNonDangerous(cell, false)
                && Math.abs(cell.y - kraken.y) == 1 && Math.abs(cell.x - kraken.x) == 1;
    }

    /**
     * @return list of cells that are Kraken weaknesses.
     */
    public Cell[] getKrakenWeaknesses() {
        Cell kraken;
        if (units.containsKey(CellType.Kraken)) {
            kraken = units.get(CellType.Kraken);
        } else {
            kraken = units.get(CellType.KrakenBelowRock);
        }
        ArrayList<Cell> result = new ArrayList<>(4);
        for (int i = Math.max(0, kraken.y - 1); i <= Math.min(size - 1, kraken.y + 1); ++i) {
            for (int j = Math.max(0, kraken.x - 1); j <= Math.min(size - 1, kraken.x + 1); ++j) {
                if (isKrakenWeakness(cells[i][j])) {
                    result.add(cells[i][j]);
                }
            }
        }
        return result.toArray(new Cell[0]);
    }

    /**
     * @param cell         considering cell
     * @param isKrakenDead true if the Kraken has been killed already.
     * @return true if the cell is non-dangerous.
     */
    public boolean isNonDangerous(Cell cell, boolean isKrakenDead) {
        switch (cell.type) {
            case DavyJones, KrakenBelowRock, Rock -> {
                return false;
            }
            case Kraken -> {
                return isKrakenDead;
            }
            case PerceptionZone -> {
                if (isKrakenDead) {
                    Cell davyJones = units.get(CellType.DavyJones);
                    return !(Math.abs(cell.y - davyJones.y) <= 1 && Math.abs(cell.x - davyJones.x) <= 1);
                } else {
                    return false;
                }
            }
            default -> {
                return true;
            }
        }
    }

    /**
     * @param path the path that needs to be shown
     * @return 2D representation of the map with the path on it as a string.
     */
    public String mapWithPathToString(ArrayList<Cell> path) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (path.contains(cells[i][j])) {
                    stringBuilder.append(CellType.PathCell.emoji);
                } else {
                    stringBuilder.append(cells[i][j].type.emoji);
                }
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * Method that flushes the d field of cells.
     */
    public void clearDistances() {
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                cells[i][j].d = Integer.MAX_VALUE;
            }
        }
    }

    /**
     * Update A* fields of the current cell
     *
     * @param current considering cell
     * @param parent  previous cell of the current in the path
     */
    public void updateAStarInformation(Cell current, Cell parent, Cell destination) {
        current.g = parent.g + 1;
        current.parent = parent;
        current.h = Math.max(Math.abs(destination.y - current.y),
                Math.abs(destination.x - current.x));
        current.f = current.g + current.h;
    }

    /**
     * Method that clears all data that is connected to the A* algorithm
     * (parent, f, g, h fields, etc.)
     */
    public void clearAStarData() {
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                cells[i][j].parent = null;
                cells[i][j].isKrakenKilled = false;
                cells[i][j].hasVisitedTortuga = false;
                cells[i][j].f = cells[i][j].g = cells[i][j].h = 0;
            }
        }
    }
}

/**
 * Class that contains search algorithms.
 */
class SearchAlgorithms {
    /**
     * Supporting function that computes the shortest path from current cell to destination using backtracking algorithm.
     *
     * @param d distance of current part of the considering path
     * @return list of cells that form the shortest path
     */
    private static ArrayList<Cell> backtracking(Map map, ArrayList<Cell> path, Cell current, Cell destination, int d, boolean hasVisitedTortuga, boolean isKrakenKilled) {
        current.d = d;
        if (current.type == CellType.Tortuga || current.type == CellType.JackOnTortuga) {
            hasVisitedTortuga = true;
        }
        if (map.isKrakenWeakness(current) && hasVisitedTortuga) {
            isKrakenKilled = true;
        }
        if (current == destination) {
            return path;
        }
        int minSize = Integer.MAX_VALUE;
        ArrayList<Cell> result = null;
        for (int i = Math.max(0, current.y - 1); i <= Math.min(map.size - 1, current.y + 1); ++i) {
            for (int j = Math.max(0, current.x - 1); j <= Math.min(map.size - 1, current.x + 1); ++j) {
                if (i == current.y && j == current.x) continue;
                if (map.isNonDangerous(map.cells[i][j], isKrakenKilled) && map.cells[i][j].d > d + 1) {
                    ArrayList<Cell> tmp = new ArrayList<>(path);
                    tmp.add(map.cells[i][j]);
                    int i2 = i + (i - current.y), j2 = j + (j - current.x);
                    ArrayList<Cell> tmp2;
                    // if type of scenario is 2 and the neighbour is horizontal or vertical, we can step over it.
                    // But only if the next cell is non-dangerous.
                    // Also, we have to check that we don't step over the destination point,
                    // whether we collected Tortuga and killed Kraken.
                    if (map.typeOfScenario == 2 && i2 >= 0 && i2 < map.size && j2 >= 0 && j2 < map.size &&
                            Math.abs(i2) + Math.abs(j2) == 1 &&
                            map.isNonDangerous(map.cells[i2][j2], isKrakenKilled) && map.cells[i2][j2].d > d + 1 &&
                            map.cells[i][j] != destination) {
                        tmp.add(map.cells[i2][j2]);
                        if (map.cells[i2][j2].type == CellType.Tortuga) {
                            hasVisitedTortuga = true;
                        }
                        if (map.isKrakenWeakness(current) && hasVisitedTortuga) {
                            isKrakenKilled = true;
                        }
                        tmp2 = backtracking(map, tmp, map.cells[i2][j2],
                                destination, d + 2, hasVisitedTortuga, isKrakenKilled);
                    } else {
                        tmp2 = backtracking(map, tmp, map.cells[i][j],
                                destination, d + 1, hasVisitedTortuga, isKrakenKilled);
                    }
                    int currentLength = Integer.MAX_VALUE;
                    if (tmp2 != null) {
                        currentLength = tmp2.size();
                    }
                    if (minSize > currentLength) {
                        minSize = currentLength;
                        result = tmp2;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Method for finding the shortest path using backtracking algorithm
     *
     * @return list of cells that form the shortest path.
     */
    public static ArrayList<Cell> backtracking(Map map) {
        // result1 - path from JackSparrow to Dead Man's chest
        // result1 == null if the algorithm couldn't find a path
        ArrayList<Cell> result1 = backtracking(map, new ArrayList<>(), map.cells[0][0],
                map.units.get(CellType.DeadManChest), 0, false, false);
        map.clearDistances();
        // result2 - path from Jack Sparrow to Tortuga
        ArrayList<Cell> result2 = backtracking(map, new ArrayList<>(), map.cells[0][0],
                map.units.get(CellType.Tortuga), 0, false, false);
        map.clearDistances();
        // result2part2 - path from Tortuga to Dead Man;s chest
        ArrayList<Cell> result2part2 = backtracking(map, new ArrayList<>(), map.units.get(CellType.Tortuga),
                map.units.get(CellType.DeadManChest), 0, false, false);
        // combine both parts of the 2 path if both of the parts have found
        if (result2 != null && result2part2 != null) {
            result2.addAll(result2part2);
        }
        // 2 path does not exist, return 1 path
        else {
            if (result1 != null) result1.add(0, map.cells[0][0]);
            return result1;
        }
        map.clearDistances();
        // if the first one doesn't exist, return second one.
        if (result1 == null) {
            result2.add(0, map.cells[0][0]);
            return result2;
        }
        // otherwise both of the paths exists, return the shortest one.
        if (result1.size() < result2.size()) {
            result1.add(0, map.cells[0][0]);
            return result1;
        }
        result2.add(0, map.cells[0][0]);
        return result2;
    }

    /**
     * Method that finds the shortest path from start cell to finish cell using A* algorithm
     * More specifically, it finds a parent from each of the cells from the path.
     * A parent for a cell is a previous cell in the path.
     */
    private static void AStarAlgorithm(Map map, Cell start, Cell finish, boolean hasVisitedTortuga) {
        Set<Cell> closed = new HashSet<>();
        PriorityQueue<Cell> open = new PriorityQueue<>();
        if (start.type == CellType.JackOnTortuga || start.type == CellType.Tortuga || hasVisitedTortuga) {
            start.hasVisitedTortuga = true;
        }
        if (map.isKrakenWeakness(start) && start.hasVisitedTortuga) {
            start.isKrakenKilled = true;
        }
        start.f = start.g = 0;
        open.add(start);
        while (!open.isEmpty() && !open.contains(finish)) {
            Cell current = open.poll();
            for (int i = Math.max(0, current.y - 1); i <= Math.min(map.size - 1, current.y + 1); ++i) {
                for (int j = Math.max(0, current.x - 1); j <= Math.min(map.size - 1, current.x + 1); ++j) {
                    // map.cells[i][j] - a neighbour of current cell
                    if (i == current.y && j == current.x) continue;
                    if (!closed.contains(map.cells[i][j]) && map.isNonDangerous(map.cells[i][j], current.isKrakenKilled)) {
                        if (!open.contains(map.cells[i][j])) {
                            map.cells[i][j].hasVisitedTortuga =
                                    map.cells[i][j].type == CellType.Tortuga
                                            || map.cells[i][j].type == CellType.JackOnTortuga || hasVisitedTortuga;
                            map.cells[i][j].isKrakenKilled =
                                    (map.isKrakenWeakness(map.cells[i][j]) && map.cells[i][j].hasVisitedTortuga)
                                            || current.isKrakenKilled;

                            map.updateAStarInformation(map.cells[i][j], current, finish);
                            open.offer(map.cells[i][j]);
                        } else {
                            open.remove(map.cells[i][j]);
                            if (current.g + 1 < map.cells[i][j].g) {
                                map.updateAStarInformation(map.cells[i][j], current, finish);
                            }
                            open.offer(map.cells[i][j]);
                        }
                        // if type of scenario is 2 and the neighbour is horizontal or vertical, we can step over it.
                        // But only if the next cell is non-dangerous.
                        // Also, we have to check that we don't step over the destination point,
                        // whether we collected Tortuga and killed Kraken.
                        int i2 = i + (i - current.y), j2 = j + (j - current.x);
                        if (map.typeOfScenario == 2 && i2 >= 0 && i2 < map.size && j2 >= 0 && j2 < map.size &&
                                Math.abs(i2) + Math.abs(j2) == 1 &&
                                map.isNonDangerous(map.cells[i2][j2], current.isKrakenKilled) &&
                                !closed.contains(map.cells[i2][j2])) {
                            if (!open.contains(map.cells[i2][j2])) {
                                map.cells[i2][j2].hasVisitedTortuga =
                                        map.cells[i2][j2].type == CellType.Tortuga
                                                || map.cells[i2][j2].type == CellType.JackOnTortuga
                                                || map.cells[i][j].hasVisitedTortuga || hasVisitedTortuga;
                                map.cells[i2][j2].isKrakenKilled =
                                        (map.isKrakenWeakness(map.cells[i2][j2]) && map.cells[i2][j2].hasVisitedTortuga)
                                                || (map.isKrakenWeakness(map.cells[i][j]) && map.cells[i][j].hasVisitedTortuga)
                                                || current.isKrakenKilled || map.cells[i][j].isKrakenKilled;
                                map.updateAStarInformation(map.cells[i2][j2], map.cells[i][j], finish);
                                open.offer(map.cells[i2][j2]);
                            } else {
                                open.remove(map.cells[i2][j2]);
                                if (current.g + 1 < map.cells[i2][j2].g) {
                                    map.updateAStarInformation(map.cells[i2][j2], map.cells[i][j], finish);
                                }
                                open.offer(map.cells[i2][j2]);
                            }

                        }

                    }
                }
            }
            closed.add(current);
        }
    }

    /**
     * Calculate the shortest path from start cell to finish cell
     * using parents of the cells that were obtained by A* algorithm
     *
     * @return shortest path
     */
    private static ArrayList<Cell> AStarPath(Cell start, Cell finish) {
        Cell current = finish;
        ArrayList<Cell> result = new ArrayList<>();
        result.add(current);
        while (current != start) {
            if (current == null) {
                return null;
            }
            current = current.parent;
            result.add(current);
        }
        result.remove(result.size() - 1);
        Collections.reverse(result);
        return result;
    }


    /**
     * Methods that finds the shortest path using A* algorithm several times.
     *
     * @return the shortest path
     */
    public static ArrayList<Cell> AStar(Map map) {
        Cell jackSparrow = map.cells[0][0];
        Cell tortuga = map.units.get(CellType.Tortuga);
        // this line is needed because on the map there can be JackWithTortuga instead of Tortuga,
        // but in the units hashmap Jack Sparrow and Tortuga are separate units always
        tortuga = map.cells[tortuga.y][tortuga.x];
        Cell[] krakenWeaknesses = map.getKrakenWeaknesses();
        Cell deadManChest = map.units.get(CellType.DeadManChest);

        // result1 - the shortest path from Jack Sparrow to Dead Man's chest
        // null if it doesn't exist
        AStarAlgorithm(map, jackSparrow, deadManChest, false);
        ArrayList<Cell> result1 = AStarPath(jackSparrow, deadManChest);
        map.clearAStarData();

        // result2 - the path from Jack Sparrow to Tortuga
        AStarAlgorithm(map, jackSparrow, tortuga, false);
        ArrayList<Cell> result2 = AStarPath(jackSparrow, tortuga);
        // if it's null return the first one
        if (result2 == null) {
            if (result1 != null)
                result1.add(0, map.cells[0][0]);
            return result1;
        }
        map.clearAStarData();

        // these are the shortest paths from Tortuga to Dead Man's chest through one of the Kraken weakness cells
        ArrayList<ArrayList<Cell>> pathsKillingKraken = new ArrayList<>(4);
        int minPathIndex = -1;
        int minPathLength = Integer.MAX_VALUE;
        for (int i = 0; i < krakenWeaknesses.length; ++i) {
            AStarAlgorithm(map, tortuga, krakenWeaknesses[i], true);
            pathsKillingKraken.add(AStarPath(tortuga, krakenWeaknesses[i]));
            if (pathsKillingKraken.get(i) == null) {
                continue;
            }
            map.clearAStarData();
            AStarAlgorithm(map, krakenWeaknesses[i], deadManChest, true);
            ArrayList<Cell> fromKrakenWeaknessToDeadManChest = AStarPath(krakenWeaknesses[i], deadManChest);
            map.clearAStarData();
            // if the second part of the path doesn't exist, the whole path doesn't exist
            if (fromKrakenWeaknessToDeadManChest == null) {
                pathsKillingKraken.set(i, null);
                continue;
            }
            pathsKillingKraken.get(i).addAll(fromKrakenWeaknessToDeadManChest);
            if (pathsKillingKraken.get(i) != null && pathsKillingKraken.get(i).size() < minPathLength) {
                minPathLength = pathsKillingKraken.get(i).size();
                minPathIndex = i;
            }
        }
        if (minPathIndex < 0) result2 = null;
            // if we found such an index, that means that the path exists and the pathsKillingKraken.get(minPathIndex) is
            // the shortest one.
            // Add to the first part of the 2 path (from Jack to Tortuga) the second part.
        else result2.addAll(pathsKillingKraken.get(minPathIndex));

        // result is the shortest path among result1 and result2 (if they're not null)
        // if result == null the path doesn't exist.
        ArrayList<Cell> result = result1;
        if (result == null) result = result2;
        else if (result2 != null && result1.size() > result2.size()) {
            result = result2;
        }
        if (result != null)
            result.add(0, map.cells[0][0]);
        return result;
    }
}