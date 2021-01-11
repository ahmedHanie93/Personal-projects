package Exam2020_4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Problem1 {

  public static List<Integer> lengthEachScene(List<Character> inputList) {
    List<Integer> output = new ArrayList<Integer>();

    HashSet<Character> chars = new HashSet<Character>();
    HashMap<Character, Integer> occ = new HashMap<Character, Integer>();
    HashMap<Integer, String> outputMap = new HashMap<>();
    String previousVideo = "";
    int characterCount = 0;
    int counter = -1;
    for (Character c : inputList) {
      previousVideo += c;
      Integer occOfC = occ.get(c);
      if (occOfC == null) {
        occOfC = 0;
      }
      occ.put(c, ++occOfC);
      chars.add(c);
      characterCount++;
      if (occOfC > 1 && characterCount > 2 || inputList.get(inputList.size() - 1) == c) {
        if (outputMap.get(counter) == null || !outputMap.get(counter).contains(c + "")) {
          if (characterCount == chars.size()) {
            for (int i = 0; i < characterCount; i++)
              output.add(1);
          } else {
            output.add(characterCount);
          }
        } else {
          Integer prevCount = output.get(output.size() - 1);
          output.set(output.size() - 1, prevCount + characterCount);
        }
        characterCount = 0;
        outputMap.put(++counter, previousVideo);
        previousVideo = "";
        occ.clear();
      } else if (occOfC > 1) {
        occ.put(c, 0);
      }
    }

    return output;
  }

}
