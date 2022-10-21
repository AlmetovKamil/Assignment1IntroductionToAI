import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
//    public static Cell[][] generateMap() {
//        Cell[][] map = new Cell[9][9];
//        Random random = new Random();
//        boolean[] used = new boolean[CellType.values().length];
//        for (int i = 0; i < CellType.values().length; ++i) {
//            CellType type = CellType.values()[i];
//            if (type == CellType.KrakenBelowRock || type == CellType.PerceptionZone ||
//                    type == CellType.SeaCell || used[i])
//                continue;
//            while (!used[i]) {
//                int temp = random.nextInt(0, 9 * 9);
//                int x = temp / 9, y = temp % 9;
//                // System.out.println("x = " + x + ", y = " + y);
//                // if nothing is in the considering cell yet
//                if (map[x][y] == null) {
//                    Cell enemy = new Cell(type, x, y);
//                    if (type == CellType.Kraken || type == CellType.DavyJones) {
//                        generatePerceptionZones(map, enemy);
//                    }
//                    map[x][y] = enemy;
//                    used[i] = true;
//                }
//                // if there is already something in the considering cell
//                else {
//                    // this types of cell cannot occur neither at the perception zone nor with anyone else
//                    if (type == CellType.Tortuga || type == CellType.DeadManChest || type == CellType.JackSparrow) {
//                        continue;
//                    }
//                    // Kraken can be with the rock at one cell
//                    if (type == CellType.Rock && map[x][y].type == CellType.Kraken) {
//                        map[x][y] = new Cell(CellType.KrakenBelowRock, x, y);
//                        used[i] = true;
//                    }
//                    // The rock can be inside the perception zone
//                    else if (type == CellType.Rock && map[x][y].type == CellType.PerceptionZone) {
//                        map[x][y] = new Cell(CellType.Rock, x, y);
//                        used[i] = true;
//                    }
//                }
//            }
//        }
//        generateSeaCells(map);
//        return map;
//    }
//    public static Cell[][] createMapWithInput(Cell[] units) {
//        Cell[][] map = new Cell[9][9];
//        for (var unit : units) {
//            map[unit.x][unit.y] = unit;
//            if (unit.type == CellType.Kraken || unit.type == CellType.DavyJones) {
//                generatePerceptionZones(map, unit);
//            }
//        }
//        generateSeaCells(map);
//        return map;
//    }
//
//    public static Cell[] input() {
//        File input = new File("input.txt");
//        Scanner in;
//        try {
//            in = new Scanner(input);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            in = new Scanner(System.in);
//        }
//        Pattern wholeLinePattern = Pattern.compile("\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s");
//        Pattern pattern = Pattern.compile("\\[\\d,\\d]\\s");
//        String inputLine = in.nextLine() + "\n";
//        Matcher matcher = wholeLinePattern.matcher(inputLine);
//        while (!matcher.find()) {
//            System.out.println("The input in the file is incorrect, try again...");
//            inputLine = in.nextLine();
//            matcher = pattern.matcher(inputLine);
//        }
//        matcher = pattern.matcher(inputLine);
//        Cell[] units = new Cell[6];
//        CellType[] orderedTypes = new CellType[6];
//        orderedTypes[0] = CellType.JackSparrow;
//        orderedTypes[1] = CellType.DavyJones;
//        orderedTypes[2] = CellType.Kraken;
//        orderedTypes[3] = CellType.Rock;
//        orderedTypes[4] = CellType.DeadManChest;
//        orderedTypes[5] = CellType.Tortuga;
//        for (int i = 0; i < 6; ++i) {
//            // now we already checked whether the string is valid or not
//            // therefore, unitStr = "\\[\\d,\\d]\\s"
//            // that means unitStr[1] = x coordinate,
//            // unitStr[3] = y coordinate
//            boolean isFound = matcher.find();
//            String unitStr = inputLine.substring(matcher.start(), matcher.end());
//            int x = unitStr.charAt(1) - '0';
//            int y = unitStr.charAt(3) - '0';
//            units[i] = new Cell(orderedTypes[i], x, y);
//        }
//        return units;
//    }



    public static int backtracking(Cell[][] map, Cell start, Cell end, Cell current, boolean[][] used, int d) {
        assert current.isNonDangerous();
        // statement to return
        current.d = d;
        used[current.x][current.y] = true;
        if (current.type == CellType.DeadManChest) {
            return d;
        }

        int res = Integer.MAX_VALUE;
        for (int i = Math.max(current.x - 1, 0); i <= Math.min(current.x + 1, 9 - 1); ++i) {
            for (int j = Math.max(current.y - 1, 0); j <= Math.min(current.y + 1, 9 - 1); ++j) {
                if (i == current.x && j == current.y) continue;
                if (map[i][j].isNonDangerous() && map[i][j].d > d + 1) {
                    res = Math.min(backtracking(map, start, end, map[i][j], used, d + 1), res);
                }
            }
        }
        return res;
    }




    public static void main(String[] args) {
        //Cell[][] map = createMapWithInput(input());
        //Cell[][] map = generateMap();
//        printMap(map);
//        boolean[][] used = new boolean[9][9];
//        int res = backtracking(map, map[6][5], map[5][6], map[6][5], used, 0);
//        System.out.println(res);
        Map map = new Map();
        try {
            map.readInput("input.txt");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println(map.units);
        try {
            map.generate();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        map.print();
    }
}

class Cell {
    int x, y;
    CellType type;
    int d = Integer.MAX_VALUE;

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

//    public void setUnitsFromInputFile(File input) throws FileNotFoundException {
//        Scanner in = new Scanner(input);
//        String wholeLineRegEx = "\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s";
//        String singleUnitRegEx = "\\[\\d,\\d]\\s";
//        Pattern wholeLinePattern = Pattern.compile(wholeLineRegEx);
//        Pattern singleUnitPattern = Pattern.compile(singleUnitRegEx);
//        String inputLine = in.nextLine() + "\n";
//        Matcher wholeLineMatcher = wholeLinePattern.matcher(inputLine);
//        Matcher singleUnitMatcher;
//        while (!wholeLineMatcher.find()) {
//            System.out.println("The input in the file is incorrect, try again...");
//            inputLine = in.nextLine();
//            wholeLineMatcher = singleUnitPattern.matcher(inputLine);
//        }
//        wholeLineMatcher = singleUnitPattern.matcher(inputLine);
//        Cell[] units = new Cell[6];
//        CellType[] orderedTypes = new CellType[6];
//        orderedTypes[0] = CellType.JackSparrow;
//        orderedTypes[1] = CellType.DavyJones;
//        orderedTypes[2] = CellType.Kraken;
//        orderedTypes[3] = CellType.Rock;
//        orderedTypes[4] = CellType.DeadManChest;
//        orderedTypes[5] = CellType.Tortuga;
//        for (int i = 0; i < 6; ++i) {
//            // now we already checked whether the string is valid or not
//            // therefore, unitStr = "\\[\\d,\\d]\\s"
//            // that means unitStr[1] = x coordinate,
//            // unitStr[3] = y coordinate
//            boolean isFound = wholeLineMatcher.find();
//            String unitStr = inputLine.substring(wholeLineMatcher.start(), wholeLineMatcher.end());
//            int x = unitStr.charAt(1) - '0';
//            int y = unitStr.charAt(3) - '0';
//            units[i] = new Cell(orderedTypes[i], x, y);
//        }
//        //return units;
//    }
}

enum CellType {
    DavyJones("\uD83D\uDC80"),
    Kraken("\uD83E\uDD91"),
    Rock("\uD83E\uDEA8"),
    Tortuga("\uD83C\uDF7A"),
    DeadManChest("\uD83E\uDE99"),
    JackSparrow("\uD83D\uDC24"),
    KrakenBelowRock("\uD83D\uDC7E"),
    SeaCell("\uD83C\uDF0A"),
    PerceptionZone("\uD83D\uDEAB");

    final String emoji;

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

    // TODO - should support output to the file
    public void print() {
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                System.out.print(cells[i][j].type.emoji);
            }
            System.out.println();
        }
    }

    private void generatePerceptionZones(Cell enemy) {
        assert enemy.type == CellType.Kraken || enemy.type == CellType.DavyJones;
        for (int j = Math.max(enemy.x - 1, 0); j <= Math.min(enemy.x + 1, 9 - 1); ++j) {
            for (int k = Math.max(enemy.y - 1, 0); k <= Math.min(enemy.y + 1, 9 - 1); ++k) {
                // the cell where the Kraken or Davy Jones stays themselves
                if (j == enemy.x && k == enemy.y) continue;
                // don't fill the diagonal cell as perception zones if it's the Kraken
                if (j != enemy.x && k != enemy.y && enemy.type == CellType.Kraken) continue;
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

    // TODO - 2 open questions about generation
    public void generate() throws IOException {
        CellType[] orderedTypes = CellType.values();
        for (int i = 0; i < 6; ++i) {
            Cell currentUnit = units.get(orderedTypes[i]);
            Cell currentCellOnMap = cells[currentUnit.x][currentUnit.y];
            if (currentCellOnMap != null) {
                // it's work because types are ordered as we want
                if (currentCellOnMap.type == CellType.Kraken && currentUnit.type == CellType.Rock) {
                    currentCellOnMap.type = CellType.KrakenBelowRock;
                    units.remove(CellType.Kraken);
                    units.remove(CellType.Rock);
                    units.put(CellType.KrakenBelowRock, currentCellOnMap);
                    cells[currentUnit.x][currentUnit.y] = currentCellOnMap;
                }
                else if ((currentUnit.type == CellType.Kraken || currentUnit.type == CellType.Rock)
                        && currentCellOnMap.type == CellType.PerceptionZone) {
                    cells[currentUnit.x][currentUnit.y] = currentUnit;
                }
                else {
                    throw new IOException("Location of units is invalid. Fix it and rerun the program.");
                }
            }
            else {
                cells[currentUnit.x][currentUnit.y] = currentUnit;
            }
            switch (currentUnit.type) {
                case DavyJones, Kraken -> generatePerceptionZones(currentUnit);
            }
        }
        generateSeaCells();
    }

    private boolean isValidInput(String path) throws FileNotFoundException {
        File input = new File(path);
        Scanner in = new Scanner(input);
        in.useDelimiter("\n");
        String regEx = "^\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]";
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
        Scanner in = new Scanner(new File(path));
        CellType[] unitTypesOrdered = {CellType.JackSparrow, CellType.DavyJones, CellType.Kraken, CellType.Rock,
                CellType.DeadManChest, CellType.Tortuga};

        for (int i = 0; i < 6; ++i) {
            String unitDataStr = in.next();
            Cell unitCell = new Cell(unitTypesOrdered[i], unitDataStr.charAt(1) - '0', unitDataStr.charAt(3) - '0');
            units.put(unitTypesOrdered[i], unitCell);
        }

        typeOfScenario = in.next().charAt(0) - '0';
    }

    public void generateUnitCells() {

    }
}