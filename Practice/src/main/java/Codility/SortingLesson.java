package Codility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class SortingLesson {

  public static int getDistinctount(int[] A) {
    HashSet<Integer> hs = new HashSet();
    for (Integer x : A)
      hs.add(x);
    return hs.size();
  }

  public static int getMaxProductOfTriplet(int[] A) {
    int length = A.length;
    Arrays.sort(A);
    return Math.max(A[0] * A[1] * A[length - 1], A[length - 3] * A[length - 2] * A[length - 1]);
  }

  public static int getNumberOfDiscsIntersections(int[] A) {
    int length = A.length;
    int[] leftX = new int[length];
    int[] rightX = new int[length];
    HashMap<Integer, List<Integer>> leftXRightX = new HashMap();
    for (int i = 0; i < length; i++) {
      leftX[i] = i - A[i];
      List<Integer> list = leftXRightX.get(leftX[i]);
      if (list == null) {
        list = new ArrayList();
      }
      list.add(i + A[i]);
      rightX[i] = i + A[i];
      leftXRightX.put(leftX[i], list);
    }
    Arrays.sort(leftX);
    Arrays.sort(rightX);
    int n = 0;
    for (int i = 0; i < length - 1; i++) {
      Arrays.binarySearch(rightX, leftX[i]);
      List<Integer> rightXs = leftXRightX.get(leftX[i]);
      int j = 1;
      if (i > 0 && leftX[i] != leftX[i - 1] || i == 0) {
        for (Integer x : rightXs) {
          int intersectionPoint = Arrays.binarySearch(leftX, x) * -1;
          n += --intersectionPoint - i - j++;
          if (n > 10000000)
            return -1;
        }
      }
    }
    return n;
  }

  public static int containTriangle(int[] A) {
    int length = A.length;
    if (length < 3)
      return 0;
    Arrays.sort(A);
    for (int i = 0; i < length - 2; i++) {
      if (A[i] + A[i + 1] > A[i + 2])
        return 1;
    }
    return 0;
  }

}
