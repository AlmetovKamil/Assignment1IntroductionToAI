import java.util.*;

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
                int temp = random.nextInt(0, 9*9);
                int x = temp / 9, y = temp % 9;
                // System.out.println("x = " + x + ", y = " + y);
                // if nothing is in the considering cell yet
                if (map[x][y] == null) {
                    if (type == CellType.Kraken || type == CellType.DavyJones) {
                        for (int j = Math.max(x-1, 0); j <= Math.min(x+1, 9 - 1); ++j) {
                            for (int k = Math.max(y-1, 0); k <= Math.min(y+1, 9 - 1); ++k) {
                                // the cell where the Kraken or Davy Jones stays themselves
                                if (j == x && k == y) continue;
                                // don't fill the diagonal cell as perception zones if it's the Kraken
                                if (j != x && k != y && type == CellType.Kraken) continue;
                                if (map[j][k] == null) {
                                    map[j][k] = new Cell(CellType.PerceptionZone, j, k);
                                }
                            }
                        }

                    }
                    map[x][y] = new Cell(type, x, y);
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
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (map[i][j] == null) {
                    map[i][j] = new Cell(CellType.SeaCell, i, j);
                }
            }
        }
        return map;
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
        Cell[][] map = generateMap();
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