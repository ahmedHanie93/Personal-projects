package Tests;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import Demo2020_4.Cells_state;

public class TestDemo2020_4 {

  @Test
  public void case1() {
    int[] states = {1, 0, 0, 0, 0, 1, 0, 0};
    int[] resultArray = {0, 1, 0, 0, 1, 0, 1, 0};
    List<Integer> excpectedResultList = initList(resultArray);
    assertEquals(excpectedResultList, Cells_state.cellCompete(states, 1));
  }

  @Test
  public void case2() {
    int[] states = {1, 1, 1, 0, 1, 1, 1, 1};
    int[] resultArray = {0, 0, 0, 0, 0, 1, 1, 0};
    List<Integer> excpectedResultList = initList(resultArray);
    assertEquals(excpectedResultList, Cells_state.cellCompete(states, 2));
  }

  private List<Integer> initList(int[] resultArray) {
    List<Integer> excpectedResultList = new ArrayList<>();
    for (int x : resultArray)
      excpectedResultList.add(x);
    return excpectedResultList;
  }

}
