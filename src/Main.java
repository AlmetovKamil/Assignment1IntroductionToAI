import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static Cell[][] generateMap() {
        Cell[][] map = new Cell[9][9];
        Random random = new Random();
        boolean[] used = new boolean[CellType.values().length];
        for (int i = 0; i < CellType.values().length; ++i) {
            CellType type = CellType.values()[i];
            if (type == CellType.KrakenBelowRock || type == CellType.PerceptionZone ||
                    type == CellType.SeaCell || used[i])
                continue;
            while (!used[i]) {
                int temp = random.nextInt(0, 9 * 9);
                int x = temp / 9, y = temp % 9;
                // System.out.println("x = " + x + ", y = " + y);
                // if nothing is in the considering cell yet
                if (map[x][y] == null) {
                    Cell enemy = new Cell(type, x, y);
                    if (type == CellType.Kraken || type == CellType.DavyJones) {
                        generatePerceptionZones(map, enemy);
                    }
                    map[x][y] = enemy;
                    used[i] = true;
                }
                // if there is already something in the considering cell
                else {
                    // this types of cell cannot occur neither at the perception zone nor with anyone else
                    if (type == CellType.Tortuga || type == CellType.DeadManChest || type == CellType.JackSparrow) {
                        continue;
                    }
                    // Kraken can be with the rock at one cell
                    if (type == CellType.Rock && map[x][y].type == CellType.Kraken) {
                        map[x][y] = new Cell(CellType.KrakenBelowRock, x, y);
                        used[i] = true;
                    }
                    // The rock can be inside the perception zone
                    else if (type == CellType.Rock && map[x][y].type == CellType.PerceptionZone) {
                        map[x][y] = new Cell(CellType.Rock, x, y);
                        used[i] = true;
                    }
                }
            }
        }
        generateSeaCells(map);
        return map;
    }

    public static void generatePerceptionZones(Cell[][] map, Cell enemy)  {
        assert enemy.type == CellType.Kraken || enemy.type == CellType.DavyJones;
        for (int j = Math.max(enemy.x - 1, 0); j <= Math.min(enemy.x + 1, 9 - 1); ++j) {
            for (int k = Math.max(enemy.y - 1, 0); k <= Math.min(enemy.y + 1, 9 - 1); ++k) {
                // the cell where the Kraken or Davy Jones stays themselves
                if (j == enemy.x && k == enemy.y) continue;
                // don't fill the diagonal cell as perception zones if it's the Kraken
                if (j != enemy.x && k != enemy.y && enemy.type == CellType.Kraken) continue;
                if (map[j][k] == null) {
                    map[j][k] = new Cell(CellType.PerceptionZone, j, k);
                }
            }
        }
    }

    public static void generateSeaCells(Cell[][] map) {
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (map[i][j] == null) {
                    map[i][j] = new Cell(CellType.SeaCell, i, j);
                }
            }
        }
    }

    public static Cell[][] createMapWithInput() {
        Cell[] characters = input();
        Cell[][] map = new Cell[9][9];
        for (var character : characters) {
            map[character.x][character.y] = character;
            if (character.type == CellType.Kraken || character.type == CellType.DavyJones) {
                generatePerceptionZones(map, character);
            }
        }
        generateSeaCells(map);
        return map;
    }

    public static Cell[] input() {
        File input = new File("input.txt");
        Scanner in;
        try {
            in = new Scanner(input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            in = new Scanner(System.in);
        }
        Pattern wholeLinePattern = Pattern.compile("\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s\\[\\d,\\d]\\s");
        Pattern pattern = Pattern.compile("\\[\\d,\\d]\\s");
        String inputLine = in.nextLine() + "\n";
        Matcher matcher = wholeLinePattern.matcher(inputLine);
        while (!matcher.find()) {
            System.out.println("The input in the file is incorrect, try again...");
            inputLine = in.nextLine();
            matcher = pattern.matcher(inputLine);
        }
        matcher = pattern.matcher(inputLine);
        Cell[] characters = new Cell[6];
        CellType[] orderedTypes = new CellType[6];
        orderedTypes[0] = CellType.JackSparrow;
        orderedTypes[1] = CellType.DavyJones;
        orderedTypes[2] = CellType.Kraken;
        orderedTypes[3] = CellType.Rock;
        orderedTypes[4] = CellType.DeadManChest;
        orderedTypes[5] = CellType.Tortuga;
        for (int i = 0; i < 6; ++i) {
            // now we already checked whether the string is valid or not
            // therefore, characterStr = "\\[\\d,\\d]\\s"
            // that means characterStr[1] = x coordinate,
            // characterStr[3] = y coordinate
            boolean isFound = matcher.find();
            String characterStr = inputLine.substring(matcher.start(), matcher.end());
            int x = characterStr.charAt(1) - '0';
            int y = characterStr.charAt(3) - '0';
            characters[i] = new Cell(orderedTypes[i], x, y);
        }
        return characters;
    }

    public static void printMap(Cell[][] map) {
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                System.out.print(map[i][j].type.emoji);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Cell[][] map = createMapWithInput();
        printMap(map);
    }
}

class Point {
    int x, y;

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class Cell {
    int x, y;
    CellType type;


    Cell(CellType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }
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