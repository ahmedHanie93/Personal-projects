package Codility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StacksAndQueues {

  public static int solution(String S) {
    HashMap<Character, Character> beginToEnd = new HashMap();
    beginToEnd.put('(', ')');
    beginToEnd.put('{', '}');
    beginToEnd.put('[', ']');
    List<Character> endChars = new ArrayList();
    List<Character> beginChars = new ArrayList();
    for (int i = 0; i < S.length(); i++) {
      char c = S.charAt(i);
      Character endOfC = beginToEnd.get(c);
      if (endOfC == null) {
        endChars.add(c);
        int index = beginChars.size() - 1;
        if (i == 0 || index == -1 || beginChars.get(index) != c)
          return 0;
        beginChars.remove(index);
        endChars.remove(0);
      } else {
        beginChars.add(endOfC);
      }
    }
    if (beginChars.size() != endChars.size())
      return 0;
    return 1;
  }

}
