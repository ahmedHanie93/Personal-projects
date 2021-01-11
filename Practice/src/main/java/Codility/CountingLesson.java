package Codility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CountingLesson {

  public static int getFrogRiverOne(int X, int[] A) {
    Set<Integer> positions = new HashSet();
    for (int i = 1; i <= X; i++)
      positions.add(i);

    for (int i = 0; i < A.length; i++) {
      positions.remove(A[i]);
      if (positions.isEmpty())
        return i;
    }
    return -1;
  }

  public static int[] getMaxCounter(int N, int[] A) {
    int[] counters = new int[N];
    int max = 0;
    int lastResetCounter = 0;
    for (int i = 0; i < A.length; i++) {
      if (A[i] < N) {
        if (counters[A[i] - 1] < lastResetCounter) {
          counters[A[i] - 1] = lastResetCounter;
        }
      }
      if (A[i] >= 1 && A[i] <= N) {
        counters[A[i] - 1]++;
        max = Math.max(max, counters[A[i] - 1]);
      } else if (A[i] == N + 1) {
        lastResetCounter = max;
      }
    }
    for (int i = 0; i < N; i++) {
      if (counters[i] < lastResetCounter)
        counters[i] = lastResetCounter;
    }
    return counters;
  }

  public static int getMissingNumber(int[] A) {
    Map<Integer, Integer> occ = new HashMap();
    for (int i = 0; i < A.length; i++) {
      occ.put(A[i], 1);
    }
    for (int i = 1;; i++) {
      Integer val = occ.get(i);
      if (val == null)
        return i;
    }
  }

  public static int isPermutation(int[] A) {
    int length = A.length;
    int[] visited = new int[length + 1];
    for (int i = 0; i < length; i++) {
      if (A[i] > length || visited[A[i]] == 1)
        return 0;
      visited[A[i]] = 1;
    }
    return 1;
  }

}
