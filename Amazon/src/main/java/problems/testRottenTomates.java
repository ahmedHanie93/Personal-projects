package problems;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class testRottenTomates {

  private static final int ZOMBIE = 1;
  private static final int[] rowShifting = {-1, 1, 0, 0};
  private static final int[] columnShifting = {0, 0, -1, 1};

  public static int getFullInfectionHours(int rows, int columns, List<List<Integer>> grid) {
    Queue<Integer> zombieCoordinats = new ArrayDeque<>();
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        int cell = grid.get(r).get(c);
        if (cell == ZOMBIE) {
          int coordinate = r * columns + c;
          zombieCoordinats.add(coordinate);
        }
      }
    }
    int hours = 0;
    while (!zombieCoordinats.isEmpty()) {
      hours++;
      int coordinate = zombieCoordinats.remove();
      int r = coordinate / columns;
      int c = coordinate % columns;
      for (int i = 0; i < 4; i++) {
        r += rowShifting[i];
        c += columnShifting[i];
        if (r >= 0 && c >= 0 && r < rows & c < columns) {
          List<Integer> list = grid.get(r);
          int adjacentCell = list.get(c);
          if (adjacentCell != ZOMBIE) {
            list.set(c, ZOMBIE);
            grid.set(r, list);
            coordinate = r * columns + c;
            zombieCoordinats.add(coordinate);
          }
        }
      }
    }
    return hours;
  }
}
