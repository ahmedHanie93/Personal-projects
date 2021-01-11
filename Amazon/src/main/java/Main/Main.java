package Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Demo2020_4.Generalized_GCD;
import Exam2019_8.Optimal_Utilization;
import Exam2020_4.Problem1;
import problems.ColumnName;
import problems.CriticalRouters;
import problems.FavoriteMusicGenres;
import problems.LoadBalancer;
import problems.ReorderDataInLogFiles;
import problems.TopKFrequentKeywords;
import problems.ZombieInMatrix;

public class Main {

  public static void main(String[] args) {
    // testColumnName();
    // testLoadBalancer();
    // testZombieInAMatrix();
    // testOptimalUtilization();
    // testReorderLogFiles();
    // testTopKFrequentKeywords();
    // testGeneralizedGCD();
    // testFavoriteGenre();
    // testCriticalRouters();
    // Character[] chars = {'a', 'b', 'c'};
    Character[] chars = {'z', 'z', 'c', 'b', 'z', 'c', 'h', 'f', 'i', 'h', 'i'};
    System.out.println(Problem1.lengthEachScene(Utils.initListFromArray(chars)));
  }


  private static void testCriticalRouters() {
    int nodesNumber = 7;
    List<List<Integer>> graph = createGraph(nodesNumber);
    int[][] edgesArray = {{0, 1}, {0, 2}, {1, 3}, {2, 3}, {3, 4}, {2, 5}, {5, 6}};

    List<int[]> edges = Utils.initListOfArrayUsing2DArray(edgesArray);
    edges.forEach(pair -> {
      addEdge(graph, pair[0], pair[1]);
    });
    CriticalRouters solver = new CriticalRouters(graph, nodesNumber);
    boolean[] isArticulationPoint = solver.findArticulationPoints();

    for (int i = 0; i < nodesNumber; i++)
      if (isArticulationPoint[i])
        System.out.printf("Node %d is an articulation\n", i);

    // testExample1();
    // testExample2();
  }


  private static void testExample1() {
    int n = 9;
    List<List<Integer>> graph = createGraph(n);

    addEdge(graph, 0, 1);
    addEdge(graph, 0, 2);
    addEdge(graph, 1, 2);
    addEdge(graph, 2, 3);
    addEdge(graph, 3, 4);
    addEdge(graph, 2, 5);
    addEdge(graph, 5, 6);
    addEdge(graph, 6, 7);
    addEdge(graph, 7, 8);
    addEdge(graph, 8, 5);

    CriticalRouters solver = new CriticalRouters(graph, n);
    boolean[] isArticulationPoint = solver.findArticulationPoints();

    // Prints:
    // Node 2 is an articulation
    // Node 3 is an articulation
    // Node 5 is an articulation
    for (int i = 0; i < n; i++)
      if (isArticulationPoint[i])
        System.out.printf("Node %d is an articulation\n", i);
  }

  // Tests a graph with 3 nodes in a line: A - B - C
  // Only node 'B' should be an articulation point.
  private static void testExample2() {
    int n = 3;
    List<List<Integer>> graph = createGraph(n);

    addEdge(graph, 0, 1);
    addEdge(graph, 1, 2);

    CriticalRouters solver = new CriticalRouters(graph, n);
    boolean[] isArticulationPoint = solver.findArticulationPoints();

    // Prints:
    // Node 1 is an articulation
    for (int i = 0; i < n; i++)
      if (isArticulationPoint[i])
        System.out.printf("Node %d is an articulation\n", i);
  }


  /* Graph helpers */

  // Initialize a graph with 'n' nodes.
  public static List<List<Integer>> createGraph(int n) {
    List<List<Integer>> graph = new ArrayList<>(n);
    for (int i = 0; i < n; i++)
      graph.add(new ArrayList<>());
    return graph;
  }

  // Add an undirected edge to a graph.
  public static void addEdge(List<List<Integer>> graph, int from, int to) {
    graph.get(from).add(to);
    graph.get(to).add(from);
  }

  private static void testFavoriteGenre() {
    Map<String, List<String>> userSongs = new HashMap<>();
    String[] songs = {"song1", "song2", "song3", "song4", "song8"};
    userSongs.put("David", Utils.initListFromArray(songs));
    String[] songs1 = {"song5", "song6", "song7"};
    userSongs.put("Emma", Utils.initListFromArray(songs1));
    Map<String, List<String>> songGenres = new HashMap<>();
    String[] songs2 = {"song1", "song3",};
    songGenres.put("Rock", Utils.initListFromArray(songs2));
    String[] songs3 = {"song7"};
    songGenres.put("Dubstep", Utils.initListFromArray(songs3));
    String[] songs4 = {"song2", "song4"};
    songGenres.put("Techno", Utils.initListFromArray(songs4));
    String[] songs5 = {"song5", "song6"};
    songGenres.put("Pop", Utils.initListFromArray(songs5));
    String[] songs6 = {"song8", "song9"};
    songGenres.put("Jazz", Utils.initListFromArray(songs6));
    Map<String, List<String>> map = FavoriteMusicGenres.favoriteGenre(userSongs, songGenres);
    Utils.printMapOfLists(map);

    Map<String, List<String>> userSongs1 = new HashMap<>();
    String[] songs7 = {"song1", "song2"};
    String[] songs8 = {"song3", "song4"};
    userSongs1.put("David", Utils.initListFromArray(songs7));
    userSongs1.put("Emma", Utils.initListFromArray(songs8));
    Map<String, List<String>> songGenres1 = new HashMap<>();
    Map<String, List<String>> map1 = FavoriteMusicGenres.favoriteGenre(userSongs1, songGenres1);
    Utils.printMapOfLists(map1);
  }



  private static void testTopKFrequentKeywords() {
    int k1 = 2;
    String[] keywords1 = {"anacell", "cetracular", "betacellular"};
    String[] reviews1 =
        {"Anacell provides the best services in the city", "betacellular has awesome services",
            "Best services provided by anacell, everyone should use anacell",};
    int k2 = 2;
    String[] keywords2 = {"anacell", "betacellular", "cetracular", "deltacellular", "eurocell"};
    String[] reviews2 = {"I love anacell Best services; Best services provided by anacell",
        "betacellular has great services",
        "deltacellular provides much better services than betacellular",
        "cetracular is worse than anacell", "Betacellular is better than deltacellular.",};
    System.out.println(TopKFrequentKeywords.getTopKFrequentKeywords(k1, keywords1, reviews1));
    System.out.println(TopKFrequentKeywords.getTopKFrequentKeywords(k2, keywords2, reviews2));
  }

  private static void testReorderLogFiles() {
    String[] logs = {"1 n u", "r 527", "j 893", "6 14", "6 82"};
    String[] reorderedLogFiles = ReorderDataInLogFiles.reorderLogFiles(logs);
    for (String x : reorderedLogFiles)
      System.out.println(x);
  }

  private static void testOptimalUtilization() {
    int[][][] As = {{{1, 2}, {2, 4}, {3, 6}}, {{1, 3}, {2, 5}, {3, 7}, {4, 10}},
        {{1, 8}, {2, 7}, {3, 14}}, {{1, 8}, {2, 15}, {3, 9}}};
    int[][][] Bs = {{{1, 2}}, {{1, 2}, {2, 3}, {3, 4}, {4, 5}}, {{1, 5}, {2, 10}, {3, 14}},
        {{1, 8}, {2, 11}, {3, 12}}};
    int[] targets = {7, 10, 20, 20};


    for (int i = 0; i < 4; i++) {
      Utils.print2DListIntegers(
          Optimal_Utilization.getPairs(Utils.initListOfArrayUsing2DArray(As[i]),
              Utils.initListOfArrayUsing2DArray(Bs[i]), targets[i]));
      System.out.println("End of Test Case");
    }
  }

  private static void testZombieInAMatrix() {
    int[][] _2DArray = {{0, 1, 1, 0, 1}, {0, 1, 0, 1, 0}, {0, 0, 0, 0, 1}, {0, 1, 0, 0, 0}};
    List<List<Integer>> _2Dlist = Utils.init2DListUsing2DArray(_2DArray);
    System.out.println(ZombieInMatrix.minHours(_2Dlist.size(), _2Dlist.get(0).size(), _2Dlist));
  }

  private static void testGeneralizedGCD() {
    int[] nums = {2, 4, 6, 8, 10};
    System.out.println(Generalized_GCD.generalizedGCD(5, nums));
  }

  private static void testColumnName() {
    String columnName = ColumnName.getColumnName(26);
    System.out.println(columnName);
  }

  private static void testLoadBalancer() {
    int[] nums1 = {1, 3, 4, 2, 2, 2, 1, 1, 2}, nums2 = {1, 1, 1, 1, 1, 1};
    System.out.println(LoadBalancer.isBalancable(nums1));
    System.out.println(LoadBalancer.isBalancable(nums2));
  }

}
