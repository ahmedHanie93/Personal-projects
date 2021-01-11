package practice;

import Codility.PrefixSumLesson;

/**
 * Hello world!
 *
 */
public class App {
  public static void main(String[] args) {
    // int[][] arr = {{259, 770}, {448, 54}, {926, 667}, {184, 139}, {840, 118}, {577, 469}};
    // System.out.println(TwoCityScheduling.twoCitySchedCost(arr));

    // Utils.printList(NumberBetween.getNumbersBetween(10, 200));
    int[] A = {0, 1, 0, 1, 1};
    System.out.println(PrefixSumLesson.getPassingCasPairs(A));
    int[] P = {2, 5, 0};
    int[] Q = {4, 5, 6};
    // ArraysLesson.getCyclicRotation(A, 42);
    // System.out.println(PrefixSumLesson.getCountofDiv(5, 11, 2));
    Utils.printIntArray(PrefixSumLesson.getGenomicRangeQuery("CAGCCTA", P, Q));
    // System.out.println("Binary Gap is: " + BinaryGap.getBinaryGap(561892));
  }
}
