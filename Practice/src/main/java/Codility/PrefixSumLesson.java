package Codility;

import java.util.HashMap;

public class PrefixSumLesson {

  public static int getCountofDiv(int A, int B, int K) {

    int diff = B / K - A / K;
    return A % K == 0 ? diff + 1 : diff;
  }

  public static int[] getGenomicRangeQuery(String S, int[] P, int[] Q) {
    int[] ans = new int[P.length];
    HashMap<Character, Integer> impactFactor = new HashMap();
    impactFactor.put('A', 1);
    impactFactor.put('C', 2);
    impactFactor.put('G', 3);
    impactFactor.put('T', 4);
    int[][] occ = new int[S.length()][4];
    for (int i = 0; i < S.length(); i++) {
      char character = S.charAt(i);
      int j = impactFactor.get(character) - 1;
      occ[i][j] = 1;
    }
    for (int i = 1; i < S.length(); i++)
      for (int j = 0; j < 4; j++)
        occ[i][j] += occ[i - 1][j];

    for (int i = 0; i < P.length; i++) {
      int p = P[i];
      int q = Q[i];
      ans[i] = Integer.MAX_VALUE;
      int previousCharsCount = 0;
      for (int j = 0; j < 4; j++) {
        if (p - 1 >= 0)
          previousCharsCount = occ[p - 1][j];
        if (occ[q][j] - previousCharsCount > 0 && j + 1 < ans[i])
          ans[i] = j + 1;
      }
    }
    return ans;
  }

  public static int getMinAvgTwoSlices(int[] A) {
    int[] sums = new int[A.length];
    for (int i = 0; i < A.length; i++)
      sums[i] += i == 0 ? A[i] : sums[i - 1] + A[i];

    return 0;
  }

  public static int getPassingCasPairs(int[] A) {
    int n = 0;
    int zeroes = 0;
    for (int i = 0; i < A.length; i++) {
      if (A[i] == 0) {
        zeroes++;
      } else {
        if (n > 1000000000)
          return -1;
        n += zeroes;
      }
    }
    return n;
  }
}
