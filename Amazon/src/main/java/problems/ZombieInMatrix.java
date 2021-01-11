package problems;

import java.util.ArrayList;
import java.util.List;

public class ZombieInMatrix {

  private static final int ZOMBIE = 1;
  private static final int[] rowShifting = {-1, 1, 0, 0};
  private static final int[] columnShifting = {0, 0, -1, 1};

  public static int minHours(int rows, int columns, List<List<Integer>> grid) {
    List<Integer> zombieCoordinatesList = new ArrayList<>();
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        Integer cell = grid.get(row).get(column);
        if (cell == ZOMBIE) {
          zombieCoordinatesList.add(row * columns + column);
        }
      }
    }

    int target = rows * columns;
    int zombies = zombieCoordinatesList.size();
    int hours = 0;
    while (zombies != target) {
      hours++;
      List<Integer> newZombies = new ArrayList<>();
      for (Integer zombieCoordinates : zombieCoordinatesList) {
        Integer row = zombieCoordinates / columns;
        Integer column = zombieCoordinates % columns;
        for (int i = 0; i < 4; i++) {
          Integer shiftedRow = row + rowShifting[i];
          Integer shiftedColumn = column + columnShifting[i];
          if (isCoordinatesValid(rows, columns, shiftedRow, shiftedColumn)) {
            Integer shiftedCell = grid.get(shiftedRow).get(shiftedColumn);
            if (shiftedCell != ZOMBIE) {
              grid.get(shiftedRow).set(shiftedColumn, 1);
              newZombies.add(shiftedRow * columns + shiftedColumn);
              zombies++;
            }
          }
        }
      }
      zombieCoordinatesList = new ArrayList<Integer>(newZombies);
    }
    return hours;
  }

  private static boolean isCoordinatesValid(int rows, int columns, Integer shiftedRow,
      Integer shiftedColumn) {
    return shiftedRow < rows && shiftedRow > -1 && shiftedColumn < columns
        && shiftedColumn > -1;
  }
}
