package Leetcode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TwoCityScheduling {

  public static int twoCitySchedCost(int[][] costs) {
    int l = costs.length;
    int sum = 0;
    Map<Integer, List<int[]>> hm = new TreeMap(Collections.reverseOrder());
    for (int i = 0; i < l; i++) {
      int[] arr = new int[2];
      arr[0] = costs[i][0];
      arr[1] = costs[i][1];
      int diff = Math.abs(arr[0] - arr[1]);
      List<int[]> list = hm.get(diff);
      if (list == null) {
        list = new ArrayList<int[]>();
      }
      list.add(arr);
      hm.put(diff, list);
    }
    int cnta = 0;
    int cntb = 0;
    for (Integer dif : hm.keySet()) {
      List<int[]> list = hm.get(dif);
      for (int[] pair : list) {
        int a = pair[0];
        int b = pair[1];
        if ((a < b && cnta < l / 2) || cntb >= l / 2) {
          sum += a;
          cnta++;
        } else {
          sum += b;
          cntb++;
        }
      }
    }
    return sum;
  }
}
