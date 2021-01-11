package Exam2019_8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Optimal_Utilization {
  // Given 2 lists a and b. Each element is a pair of integers where the first integer represents
  // the unique id and the second integer represents a value. Your task is to find an element from a
  // and an element form b such that the sum of their values is less or equal to target and as close
  // to target as possible. Return a list of ids of selected elements. If no pair is possible,
  // return an empty list.
  public static List<List<Integer>> getPairs(List<int[]> a, List<int[]> b, int target) {
    List<List<Integer>> pairs = new ArrayList<>();
    // reverse sort we use [1] to sort by value
    Collections.sort(a, (i, j) -> i[1] + j[1]);
    // sort we use [1] to sort by value
    Collections.sort(b, (i, j) -> i[1] - j[1]);
    int max = Integer.MIN_VALUE;
    for (int i = 0; i < a.size(); i++) {
      for (int j = 0; j < b.size(); j++) {
        int sum = a.get(i)[1] + b.get(j)[1];
        if (sum <= target) {
          List<Integer> pair = new ArrayList<>();
          pair.add(i + 1);
          pair.add(j + 1);
          if (sum > max) {
            max = sum;
            pairs.clear();
            pairs.add(pair);
          } else if (sum == max) {
            pairs.add(pair);
          }
        }
      }
    }
    return pairs;
  }
}
