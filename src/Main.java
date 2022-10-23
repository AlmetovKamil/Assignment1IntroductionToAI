import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;

public class Main {
    //TODO - file reading several times is not working
    //TODO - in the units map JackSparrow and Tortuga are different members, even if they are in the same cell
    public static void main(String[] args) throws IOException {
        Map map = new Map();
        map.generateMap();
        map.printUnits();
        //System.out.println(map.units);
        ArrayList<Cell> res = SearchAlgorithms.backtracking(map);
        PrintWriter printWriter = new PrintWriter("output.txt");

        if (res == null) {
            printWriter.println("Lose");
        } else {
            printWriter.println("Win");
            for (var cell : res) {
                printWriter.printf("[%d,%d] ", cell.x, cell.y);
            }
            printWriter.println();
            printWriter.println(map.mapWithPathToString(res));
            printWriter.println("TODO - TIME");
        }
        printWriter.close();
        //System.out.println(res);
        //map.print();
    }
}

class Cell {
    int x, y;
    CellType type;
    int d = Integer.MAX_VALUE;
    Cell parent;

    Cell(CellType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "x=" + x +
                ", y=" + y +
                ", type=" + type +
                ", d=" + d +
                '}';
    }

    public boolean isNonDangerous() {
        return this.type != CellType.Kraken && this.type != CellType.DavyJones && this.type != CellType.Rock
                && this.type != CellType.KrakenBelowRock && this.type != CellType.PerceptionZone;
    }
}

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

    final String emoji;
    final static int NUMBER_OF_UNITS = 6;

    CellType(String emoji) {
        this.emoji = emoji;
    }
}

class Map {
    Cell[][] cells;
    HashMap<CellType, Cell> units;
    int size;
    int typeOfScenario;

    public Map() {
        size = 9;
        cells = new Cell[size][size];
        units = new HashMap<>(6);
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

    public void printUnits() {
        for (int i = 0; i < CellType.NUMBER_OF_UNITS; ++i) {
            System.out.printf("[%d,%d] ", units.get(CellType.values()[i]).x, units.get(CellType.values()[i]).y);
        }
        System.out.println();
    }

    // TODO - should support output to the file
    public void print() {
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                System.out.print(cells[i][j].type.emoji);
            }
            System.out.println();
        }
    }

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
            print();
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
                    readInput("input.txt");
                    generateFromInputFile();
                    System.out.println("The map from file is successfully generated");
                    print();
                    System.out.println("Perception scenario: " + typeOfScenario);
                    break;
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Fix the file issue and type anything to console to try again");
                    in.next();
                }
            }

        }
        in.close();
    }

    private void putPerceptionZones(Cell enemy) {
        assert enemy.type == CellType.Kraken || enemy.type == CellType.DavyJones
                || enemy.type == CellType.KrakenBelowRock;
        for (int j = Math.max(enemy.x - 1, 0); j <= Math.min(enemy.x + 1, 9 - 1); ++j) {
            for (int k = Math.max(enemy.y - 1, 0); k <= Math.min(enemy.y + 1, 9 - 1); ++k) {
                // the cell where the Kraken or Davy Jones stays themselves
                if (j == enemy.x && k == enemy.y) continue;
                // don't fill the diagonal cell as perception zones if it's the Kraken
                if (j != enemy.x && k != enemy.y &&
                        (enemy.type == CellType.Kraken || enemy.type == CellType.KrakenBelowRock)) continue;
                if (cells[j][k] == null) {
                    cells[j][k] = new Cell(CellType.PerceptionZone, j, k);
                }
            }
        }
    }

    private void generateSeaCells() {
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (cells[i][j] == null) {
                    cells[i][j] = new Cell(CellType.SeaCell, i, j);
                }
            }
        }
    }

    private void generateMapRandomly() {
        Random random = new Random();
        boolean[] used = new boolean[CellType.NUMBER_OF_UNITS];
        for (int i = 0; i < CellType.NUMBER_OF_UNITS; ++i) {
            CellType type = CellType.values()[i];
            while (!used[i]) {
                int temp = random.nextInt(0, 9 * 9);
                int x, y;
                if (type == CellType.JackSparrow) {
                    x = 0;
                    y = 0;
                } else {
                    x = temp / 9;
                    y = temp % 9;
                }
                // System.out.println("x = " + x + ", y = " + y);
                Cell cell = new Cell(type, x, y);
                if (canPutUnit(cell)) {
                    putUnit(cell);
                    units.put(type, cell);
                    used[i] = true;
                }
            }
        }
        generateSeaCells();
    }

    private boolean canPutPerceptionZone(int x, int y) {
        if (x < 0 || x >= size || y < 0 || y >= size) return false;
        return (cells[x][y] == null || cells[x][y].type == CellType.Kraken
                || cells[x][y].type == CellType.KrakenBelowRock
                || cells[x][y].type == CellType.Rock
                || cells[x][y].type == CellType.DavyJones
                || cells[x][y].type == CellType.PerceptionZone);
    }

    private boolean canPutUnit(Cell cell) {
        assert cell.type != CellType.PerceptionZone && cell.type != CellType.SeaCell
                && cell.type != CellType.KrakenBelowRock && cell.type != CellType.JackOnTortuga;
        Cell cellOnMap = cells[cell.x][cell.y];
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
                for (int i = cell.x - 1; i < cell.x + 1; ++i) {
                    for (int j = cell.y - 1; j < cell.y + 1; ++j) {
                        if (i == cell.x && j == cell.y) continue;
                        if (cell.type == CellType.Kraken) {
                            if (i == cell.x || j == cell.y) {
                                if (!canPutPerceptionZone(i, j)) {
                                    return false;
                                }
                            }
                        } else {
                            if (!canPutPerceptionZone(i, j)) {
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

    private void putUnit(Cell cell) {
        assert canPutUnit(cell);
        if (cells[cell.x][cell.y] != null && cells[cell.x][cell.y].type != CellType.PerceptionZone) {
            switch (cell.type) {
                case Kraken, Rock -> {
                    cell.type = CellType.KrakenBelowRock;
                    cells[cell.x][cell.y] = cell;
                }
                case JackSparrow, Tortuga -> {
                    cell.type = CellType.JackOnTortuga;
                    cells[cell.x][cell.y] = cell;
                }
            }
        } else {
            cells[cell.x][cell.y] = cell;
        }
        switch (cell.type) {
            case Kraken, KrakenBelowRock, DavyJones -> putPerceptionZones(cell);
        }
    }

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

    // TODO - 2 open questions about generation
//    public void createFromInputFile() throws IOException {
//        CellType[] orderedTypes = CellType.values();
//        for (int i = 0; i < CellType.NUMBER_OF_UNITS; ++i) {
//            Cell currentUnit = units.get(orderedTypes[i]);
//            Cell currentCellOnMap = cells[currentUnit.x][currentUnit.y];
//            if (currentCellOnMap != null) {
//                // it's work because types are ordered as we want
//                if (currentCellOnMap.type == CellType.Kraken && currentUnit.type == CellType.Rock) {
//                    currentCellOnMap.type = CellType.KrakenBelowRock;
//                    units.remove(CellType.Kraken);
//                    units.remove(CellType.Rock);
//                    units.put(CellType.KrakenBelowRock, currentCellOnMap);
//                    cells[currentUnit.x][currentUnit.y] = currentCellOnMap;
//                } else if ((currentUnit.type == CellType.Kraken || currentUnit.type == CellType.Rock)
//                        && currentCellOnMap.type == CellType.PerceptionZone) {
//                    cells[currentUnit.x][currentUnit.y] = currentUnit;
//                } else if (currentUnit.type == CellType.Tortuga && currentCellOnMap.type == CellType.JackSparrow) {
//                    currentCellOnMap.type = CellType.JackOnTortuga;
//                    cells[currentUnit.x][currentUnit.y] = currentCellOnMap;
//                } else {
//                    throw new IOException("Location of units is invalid. Fix it and rerun the program.");
//                }
//            } else {
//                cells[currentUnit.x][currentUnit.y] = currentUnit;
//            }
//            switch (currentUnit.type) {
//                case DavyJones, Kraken -> generatePerceptionZones(currentUnit);
//            }
//        }
//        generateSeaCells();
//    }

    private boolean isValidInput(String path) throws FileNotFoundException {
        File input = new File(path);
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

    public void readInput(String path) throws IOException {
        if (!isValidInput(path)) {
            throw new IOException("The input is invalid. Fix it and rerun the program.");
        }
        File input = new File(path);
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

    public boolean isKrakenWeakness(Cell cell) {
        Cell kraken;
        if (units.containsKey(CellType.Kraken)) {
            kraken = units.get(CellType.Kraken);
        } else {
            kraken = units.get(CellType.KrakenBelowRock);
        }
        return Math.abs(cell.x - kraken.x) == 1 && Math.abs(cell.y - kraken.y) == 1;
    }

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
                    return !(Math.abs(cell.x - davyJones.x) <= 1 && Math.abs(cell.y - davyJones.y) <= 1);
                } else {
                    return false;
                }
            }
            default -> {
                return true;
            }
        }
    }

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
}

class SearchAlgorithms {
    // returns the path from current to destination
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
        for (int i = Math.max(0, current.x - 1); i <= Math.min(map.size - 1, current.x + 1); ++i) {
            for (int j = Math.max(0, current.y - 1); j <= Math.min(map.size - 1, current.y + 1); ++j) {
                if (i == current.x && j == current.y) continue;
                if (map.isNonDangerous(map.cells[i][j], isKrakenKilled) && map.cells[i][j].d > d + 1) {
                    ArrayList<Cell> tmp = new ArrayList<>(path);
                    tmp.add(map.cells[i][j]);
                    ArrayList<Cell> tmp2 =
                            backtracking(map, tmp, map.cells[i][j], destination, d + 1, hasVisitedTortuga, isKrakenKilled);
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

    public static ArrayList<Cell> backtracking(Map map) {
        ArrayList<Cell> result = backtracking(map, new ArrayList<>(), map.cells[0][0],
                map.units.get(CellType.DeadManChest), 0, false, false);
        for (int i = 0; i < map.size; ++i) {
            for (int j = 0; j < map.size; ++j) {
                map.cells[i][j].d = Integer.MAX_VALUE;
            }
        }
        if (result == null) {
            result = backtracking(map, new ArrayList<>(), map.cells[0][0],
                    map.units.get(CellType.Tortuga), 0, false, false);
            for (int i = 0; i < map.size; ++i) {
                for (int j = 0; j < map.size; ++j) {
                    map.cells[i][j].d = Integer.MAX_VALUE;
                }
            }
            if (result == null) {
                return null;
            }
            result.addAll(backtracking(map, new ArrayList<>(), map.units.get(CellType.Tortuga),
                    map.units.get(CellType.DeadManChest), 0, false, false));
            for (int i = 0; i < map.size; ++i) {
                for (int j = 0; j < map.size; ++j) {
                    map.cells[i][j].d = Integer.MAX_VALUE;
                }
            }
        }
        result.add(0, map.cells[0][0]);
        return result;
    }
}