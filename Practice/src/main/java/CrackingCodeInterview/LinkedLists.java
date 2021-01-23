package CrackingCodeInterview;

import java.util.Stack;
import DataStructures.ListNode;

public class LinkedLists {

  // Remove Dups: Write code to remove duplicates from an unsorted linked list.
  // solve this problem if a temporary buffer is not allowed?
  public static ListNode removeDuplicate(ListNode list) {
    ListNode itr = list;

    while (itr.next != null) {
      ListNode deleter = itr;
      while (deleter.next != null) {
        if (deleter.next.data == itr.data) {
          deleter.next = deleter.next.next;
        } else {
          deleter = deleter.next;
        }
      }
      itr = itr.next;
    }

    return list;
  }

  // Return Kth to Last: Implement an algorithm to find the kth to last element of a singly linked
  // list. Reccursion would cost o(N) space complexity while iterative o(1)
  static class Index {
    int val = 0;
  }

  public static ListNode kthTolast(ListNode head, int k) {
    Index idx = new Index();
    return kthToLast(head, k, idx);
  }

  private static ListNode kthToLast(ListNode head, int k, Index idx) {
    if (head == null)
      return null;

    ListNode node = kthTolast(head.next, k);
    idx.val++;

    if (idx.val == k)
      return head;

    return node;
  }

  // Delete Middle Node: Implement an algorithm to delete a node in the middle (i.e., any node but
  // the first and last node, not necessarily the exact middle) of a singly linked list, given only
  // access to that node.
  // lnput:the node c from the linked list a->b->c->d->e->f
  // Result: nothing is returned, but the new linked list looks like a ->b->d- >e- >f
  public static boolean deleteMidleNode(ListNode node) {
    if (node == null || node.next == null) {
      return false;
    }
    ListNode next = node.next;
    node.data = next.data;
    node.next = next.next;
    return true;
  }

  // Partition: Write code to partition a linked list around a value x, such that all nodes less
  // than x come before all nodes greater than or equal to x. If x is contained within the list the
  // values of x only need to be after the elements less than x (see below). The partition element x
  // can appear anywhere in the "right partition"; it does not need to appear between the left and
  // right partitions.
  // EXAMPLE
  // Input: 3 -> 5 -> 8 -> 5 -> 10 -> 2 -> 1 [partition= 5]
  // Output: 3 -> 1 -> 2 -> 10 -> 5 -> 5 -> 8
  // Maintain Order Solution
  public static ListNode partitionMaintainOrder(ListNode node, int x) {
    ListNode beforeStart = null;
    ListNode beforeEnd = null;
    ListNode afterStart = null;
    ListNode afterEnd = null;

    while (node != null) {
      ListNode next = node.next;
      node.next = null;
      if (node.data < x) {
        updatePartitionNode(node, beforeStart, beforeEnd);
      } else {
        updatePartitionNode(node, afterStart, afterEnd);
      }
      node = next;
    }
    if (beforeStart == null) {
      return afterStart;
    }
    /* Merge before list and after list */
    beforeEnd.next = afterStart;
    return beforeStart;
  }

  private static void updatePartitionNode(ListNode node, ListNode start, ListNode end) {
    if (start == null) {
      start = node;
      end = start;
    } else {
      end.next = node;
      end = node;
    }
  }

  // Do not Maintain order solution
  public static ListNode partition(ListNode node, int x) {
    ListNode head = node;
    ListNode tail = head;

    while (node != null) {
      ListNode next = node.next;
      if (node.data < x) {
        node.next = head;
        head = node;
      } else {
        tail.next = node;
        tail = node;
      }
      node = next;
    }
    tail.next = null;
    return head;
  }

  // Sum Lists: You have two numbers represented by a linked list, where each node contains a single
  // digit. The digits are stored in reverse order, such that the 1 's digit is at the head of the
  // list. Write a
  // function that adds the two numbers and returns the sum as a linked list.
  public static ListNode addLists(ListNode a, ListNode b) {
    int counter = 0;
    int sum = 0;
    while (a != null) {
      int digit = (int) Math.pow(10, counter);
      sum += a.data * digit;
      counter++;
      a = a.next;
    }
    counter = 0;
    while (b != null) {
      int digit = (int) Math.pow(10, counter);
      sum += b.data * digit;
      counter++;
      b = b.next;
    }
    a = null;
    while (sum > 0) {
      if (a == null) {
        a = new ListNode(sum % 10);
        b = a;
      } else {
        a.next = new ListNode(sum % 10);
        a = a.next;
      }
      sum /= 10;
    }
    return b;
  }

  // Palindrome: Implement a function to check if a linked list is a palindrome.
  public static boolean isPalindrome(ListNode head) {
    ListNode reverse = new ListNode(head.data);
    ListNode itr = head.next;
    while (itr != null) {
      ListNode node = new ListNode(itr.data);
      node.next = reverse;
      reverse = node;
      itr = itr.next;
    }
    while (head != null) {
      if (head.data != reverse.data) {
        return false;
      }
      head = head.next;
      reverse = reverse.next;
    }
    return true;
  }

  public static boolean isPalindromeSlowFastRunner(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;
    Stack<Integer> stack = new Stack();

    while (fast != null && fast.next != null) {
      stack.push(slow.data);
      slow = slow.next;
      fast = fast.next.next;
    }
    // odd size LinkedList
    if (fast != null) {
      slow = slow.next;
    }

    while (slow != null) {
      if (slow.data != stack.pop()) {
        return false;
      }
      slow = slow.next;
    }
    return true;
  }

  public static boolean isPalindromReccursive(ListNode head) {
    int length = getLinkedListLength(head);
    Result res = getRecurrsiveRes(head, length);

    return res.res;
  }

  private static Result getRecurrsiveRes(ListNode head, int length) {
    if (length == 0 || head == null)
      return new Result(head, true);

    if (length == 1)
      return new Result(head.next, true);

    Result res = getRecurrsiveRes(head.next, length - 2);

    if (!res.res || res.node == null) {
      return res;
    }
    res.res = res.node.data == head.data;
    res.node = res.node.next;

    return res;
  }

  private static int getLinkedListLength(ListNode head) {
    ListNode runner = head;
    int size = 0;
    while (runner != null) {
      size++;
      runner = runner.next;
    }
    return size;
  }

  static class Result {
    boolean res;
    ListNode node;

    public Result() {
      res = false;
    }

    Result(ListNode node, boolean res) {
      this.res = res;
      this.node = node;
    }
  }

  // Intersection: Given two (singly) linked lists, determine if the two lists intersect. Return the
  // intersecting node. Note that the intersection is defined based on reference, not value. That
  // is, if the kth node of the first linked list is the exact same node (by reference) as the jth
  // node of the second linked list, then they are intersecting.
  public static ListNode findintersection(ListNode l1, ListNode l2) {
    if (l1 == null || l2 == null)
      return null;

    TailAndSize tailAndSize1 = getLinkedListTailAndLength(l1);
    TailAndSize tailAndSize2 = getLinkedListTailAndLength(l2);

    if (!tailAndSize1.tail.equals(tailAndSize2.tail))
      return null;

    if (tailAndSize1.size > tailAndSize2.size) {
      return getIntersectionNode(l2, l1, tailAndSize2.size, tailAndSize1.size);
    } else {
      return getIntersectionNode(l1, l2, tailAndSize1.size, tailAndSize2.size);
    }

  }

  private static ListNode getIntersectionNode(ListNode shortRunner, ListNode tallRunner,
      int shortSize, int tallSize) {
    while (tallRunner != null && !tallRunner.equals(shortRunner)) {
      tallRunner = tallRunner.next;
      if (tallSize == shortSize) {
        shortRunner = shortRunner.next;
        shortSize--;
      }
      tallSize--;
    }
    return tallRunner;
  }

  private static TailAndSize getLinkedListTailAndLength(ListNode head) {
    ListNode runner = head;
    int size = 1;
    while (runner.next != null) {
      size++;
      runner = runner.next;
    }
    return new TailAndSize(size, runner);
  }

  static class TailAndSize {
    int size;
    ListNode tail;

    public TailAndSize(int size, ListNode tail) {
      this.size = size;
      this.tail = tail;
    }
  }

  public static ListNode findBeginingOfLoop(ListNode head) {
    ListNode fast = head;
    ListNode slow = head;

    while (fast != null && fast.next != null) {
      fast = fast.next.next;
      slow = slow.next;
      if (fast == slow)
        break;
    }

    if (fast == null || fast.next == null) {
      return null;
    }

    // Move slow to Head. Keep fast at Meeting Point. Each are k steps from the
    // Loop Start. If they move at the same pace, they must meet at Loop Start .
    slow = head;
    while (fast != slow) {
      fast = fast.next;
      slow = slow.next;
    }

    return fast;
  }

}
