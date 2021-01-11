package Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Utils {

  public static void print2DList(List<List<? extends Object>> _2Dlist) {
    for (List<? extends Object> row : _2Dlist) {
      printList(row);
      System.out.println();
    }
  }

  public static void printList(List<? extends Object> row) {
    for (Object column : row) {
      System.out.print(column + " ");
    }
  }

  public static void print2DListIntegers(List<List<Integer>> pairs) {
    for (List<? extends Object> row : pairs) {
      printList(row);
      System.out.println();
    }
  }

  public static void printMapOfLists(Map<String, List<String>> map) {
    for (Entry<String, List<String>> entry : map.entrySet()) {
      System.out.println(entry.getKey() + ":");
      for (String s : entry.getValue()) {
        System.out.print(" " + s);
      }
      System.out.println();
    }
  }

  public static void printMap(Map<String, Object> map) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      System.out.println(entry.getKey() + ":" + entry.getValue().toString());
    }
  }

  public static List<String> initListFromArray(String[] songs) {
    List<String> list = new ArrayList<String>();
    for (String s : songs)
      list.add(s);
    return list;
  }

  public static List<Character> initListFromArray(Character[] songs) {
    List<Character> list = new ArrayList<>();
    for (Character s : songs)
      list.add(s);
    return list;
  }

  public static List<List<Integer>> init2DListUsing2DArray(int[][] array) {
    List<List<Integer>> _2Dlist = new ArrayList<>();
    for (int[] ints : array) {
      List<Integer> list = new ArrayList<>();
      for (int i : ints) {
        list.add(i);
      }
      _2Dlist.add(list);
    }
    return _2Dlist;
  }

  public static List<int[]> initListOfArrayUsing2DArray(int[][] array) {
    List<int[]> _2Dlist = new ArrayList<>();
    for (int[] ints : array) {
      _2Dlist.add(ints);
    }
    return _2Dlist;
  }

}
