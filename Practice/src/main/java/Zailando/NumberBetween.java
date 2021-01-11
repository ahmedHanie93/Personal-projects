package Zailando;

import java.util.ArrayList;
import java.util.List;

public class NumberBetween {

  public static List<Integer> getNumbersBetween(int a, int b) {
    List<Integer> list = new ArrayList();
    while (a * (a + 1) <= b) {
      list.add(a++);
    }

    return list;
  }

}
