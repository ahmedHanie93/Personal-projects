package Codility;

import java.util.HashMap;
import java.util.Map;
import practice.Utils;

public class ArraysLesson {

  public static int[] getCyclicRotation(int[] A, int K) {
    int[] B = A.clone();
    int length = A.length;
    for (int i = 0; i < length; i++) {
      int rotatedIndex = (i + K) % length;
      B[rotatedIndex] = A[i];
    }
    Utils.printIntArray(B);
    return B;
  }

  public static int getOddOccurrencesInArray(int[] A) {
    Map<Integer, Integer> hm = new HashMap();
    for (Integer key : A)
      hm.put(key, hm.get(key) == null ? 1 : hm.get(key) + 1);
    for (Integer key : hm.keySet()) {
      if (hm.get(key) % 2 != 0)
        return key;
    }
    return 0;
  }
}
