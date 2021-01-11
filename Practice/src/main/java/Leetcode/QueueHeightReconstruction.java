package Leetcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class QueueHeightReconstruction {

  public static int[][] reconstructQueue(int[][] people) {
    int[][] reconstructedQueue = new int[people.length][2];
    HashMap<Integer, TreeMap<Integer, List<Integer>>> afterPersonsToPairs = new HashMap();

    for (int[] array : people) {
      TreeMap<Integer, List<Integer>> pairs = afterPersonsToPairs.get(array[1]);
      List<Integer> afterPersons;
      if (pairs == null) {
        pairs = new TreeMap();
        afterPersons = new ArrayList();
      } else {
        afterPersons = pairs.get(array[0]);
      }
      afterPersons.add(array[1]);
      pairs.put(array[0], afterPersons);
    }

    int iAfterPeople = 0;
    int itrator = 0;
    while (itrator < people.length) {
      TreeMap<Integer, List<Integer>> pairsWithIAfterPersons =
          afterPersonsToPairs.get(iAfterPeople);
      if (pairsWithIAfterPersons == null) {
        pairsWithIAfterPersons = afterPersonsToPairs.get(1);
      } else {

      }
      Set<Integer> iPeople = pairsWithIAfterPersons.keySet();
      for (Integer i : iPeople) {
        int nSameHeights = pairsWithIAfterPersons.get(i).size();
        for (; nSameHeights >= 0; nSameHeights--) {
          reconstructedQueue[itrator][0] = i;
          reconstructedQueue[itrator][1] = iAfterPeople;
          itrator++;
          iAfterPeople++;
        }
      }

      break;
    }

    return reconstructedQueue;
  }
}
