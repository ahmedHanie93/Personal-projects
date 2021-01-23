package practice;

import CrackingCodeInterview.LinkedLists;
import DataStructures.ListNode;

/**
 * Hello world!
 *
 */
public class App {
  public static void main(String[] args) {
    // int[][] edgesArray = {{0, 1}, {0, 2}, {1, 3}, {2, 3}, {3, 4}, {2, 5}, {5, 6}};
    //
    // List<int[]> edges = Utils.initListOfArrayUsing2DArray(edgesArray);
    // System.out.println(StacksAndQueues.solution("())(()"));
    // int[] P = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
    // System.out.println(SortingLesson.containTriangle(P));
    // int[] Q = {4, 5, 6};
    // // ArraysLesson.getCyclicRotation(A, 42);
    // // System.out.println(PrefixSumLesson.getCountofDiv(5, 11, 2));
    // Utils.printIntArray(PrefixSumLesson.getGenomicRangeQuery("CAGCCTA", P, Q));
    // System.out.println("Binary Gap is: " + BinaryGap.getBinaryGap(561892));
    testLinkedListsIntersection();
  }

  private static void testLinkedList() {
    ListNode list = new ListNode(1);
    // list.appendToTail(2);
    // list.appendToTail(3);
    list.appendToTail(4);
    list.appendToTail(5);
    list.appendToTail(6);
    // list.appendToTail(7);
    // list.appendToTail(8);
    LinkedLists.partition(list, 5);
    list.printList(list);
    list.deleteByKey(list, 1);
    list.printList(list);
    list.deleteByKey(list, 4);
    list.printList(list);
    list.deleteByKey(list, 10);
    list.printList(list);
    list.deleteAtPosition(list, 0);
    list.printList(list);
    list.deleteAtPosition(list, 2);
    list.printList(list);
    list.deleteAtPosition(list, 10);
    list.printList(list);
  }

  private static void testAddLinkedList() {
    ListNode list = new ListNode(7);
    list.appendToTail(1);
    list.appendToTail(6);
    ListNode list1 = new ListNode(5);
    list1.appendToTail(9);
    list1.appendToTail(2);
    list.printList(LinkedLists.addLists(list, list1));
  }

  private static void testPalindromeLinkedList() {
    ListNode list = new ListNode(0);
    list.appendToTail(1);
    list.appendToTail(2);
    list.appendToTail(1);
    list.appendToTail(0);
    System.out.println(LinkedLists.isPalindromReccursive(list));
  }

  private static void testLinkedListsIntersection() {
    ListNode list = new ListNode(3);
    list.appendToTail(1);
    list.appendToTail(2);
    list.appendToTail(7);
    list.appendToTail(2);
    list.appendToTail(1);
    ListNode list1 = new ListNode(5);
    list1.appendToTail(7);
    list1.appendToTail(2);
    list1.appendToTail(1);
    list.printList(LinkedLists.findintersection(list, list1));
  }

}
