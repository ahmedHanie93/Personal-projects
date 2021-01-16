package Zailando;

import java.util.Arrays;
import java.util.HashMap;

public class Exam_2021 {

  // Consistent == right next to each others
  // 50552 ==> 5
  // 10101 ==> 10
  // 88 ==> 88
  public static int getBiggestTwoDigitConsistentFragment(String S) {
    int length = S.length();
    int max = 0;
    for (int i = 0; i < length - 1; i++) {
      int consistentDigit = Integer.parseInt("" + S.charAt(i) + S.charAt(i + 1));
      max = Math.max(consistentDigit, max);
    }
    return max;
  }

  // get deleted chars tomake each character occurence number unique
  // aaaabbbb ==> 1
  // ccaaffddecee ==> 6
  // eeee ==> 0
  // example ==> 4
  public static int getDeletedNChars(String S) {
    int length = S.length();
    HashMap<Character, Integer> occ = new HashMap();
    HashMap<Integer, Character> sameOccChars = new HashMap();

    for (int i = 0; i < length; i++) {
      char c = S.charAt(i);
      Integer charOcc = occ.get(c);

      if (charOcc == null) {
        charOcc = 0;
      }
      occ.put(c, ++charOcc);
    }

    int n = 0;
    for (Character c : occ.keySet()) {
      Integer charOcc = occ.get(c);
      Character character = sameOccChars.get(charOcc);

      while (character != null && charOcc != 0) {
        n++;
        character = sameOccChars.get(--charOcc);
      }
      sameOccChars.put(charOcc, c);
    }
    return n;
  }

  // break the chain in 2 weakest positions, avoid first and last elements
  // {5, 2, 4, 6, 3, 7} ==> 1, 4
  public static int getMinTwoNonAdjacentIntSum(int[] A) {
    int length = A.length;
    HashMap<Integer, Integer> elementIndex = new HashMap();
    int[] B = new int[length - 2];

    for (int i = 1; i < length - 1; i++) {
      elementIndex.put(A[i], i);
      B[i - 1] = A[i];
    }

    Arrays.sort(B);
    Integer secondBIndex = elementIndex.get(B[1]);
    Integer firstBIndex = elementIndex.get(B[0]);

    return secondBIndex == firstBIndex - 1 || secondBIndex == firstBIndex + 1 ? B[0] + B[2]
        : B[0] + B[1];
  }

  public static int anotherSolution3(int[] A) {
    int length = A.length;
    int minSum = A[1] + A[3];

    for (int i = 1; i < length - 1; i++) {
      for (int j = i + 2; j < length - 1; j++) {
        if (A[i] + A[j] < minSum)
          minSum = A[i] + A[j];
      }
    }

    return minSum;
  }

}
