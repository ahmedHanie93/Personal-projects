package Demo2020_4;

import java.util.ArrayList;
import java.util.List;

public class Cells_state {
  // get Cells state after n days; cell is active if adjacent neighbors are different, and end and
  // first cells has unoccuped inactive neighbor
  public static List<Integer> cellCompete(int[] states, int days) {
    List<Integer> endCells = new ArrayList<>();
    List<Integer> cells = new ArrayList<>();
    cells.add(0);
    for (int i : states) {
      cells.add(i);
    }
    cells.add(0);
    for (int i = 0; i < days; i++) {
      if (i > 0) {
        cells.clear();
        cells.add(0);
        cells.addAll(endCells);
        cells.add(0);
        endCells.clear();
      }
      computeEndCells(endCells, cells);
    }

    return endCells;
  }

  private static void computeEndCells(List<Integer> endCells, List<Integer> cells) {
    for (int i = 1; i < 9; i++) {
      Integer neighbor1 = cells.get(i - 1);
      Integer neighbor2 = cells.get(i + 1);
      if (neighbor1 == neighbor2) {
        endCells.add(0);
      } else {
        endCells.add(1);
      }
    }
  }
}
