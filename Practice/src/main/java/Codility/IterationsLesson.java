package Codility;

import java.util.ArrayList;
import java.util.List;

public class IterationsLesson {

  public static int getBinaryGap(int n) {
    String binaryString = Integer.toBinaryString(n);
    List<Integer> gapLengths = new ArrayList();
    int count = 0;
    int j = 1;
    int max = 0;
    for (int i = 0; i < binaryString.length(); i++) {
      char c = binaryString.charAt(i);
      if (c == '0') {
        count++;
      } else if (count > 0) {
        if (count > max)
          max = count;
        gapLengths.add(count);
        System.out.println("gap" + j + " = " + count);
        count = 0;
      }
    }
    return max;
  }

}
