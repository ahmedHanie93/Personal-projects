package DataStructures;

public class ListNode {

  public int data;
  public ListNode next;

  public ListNode(int d) {
    this.data = d;
    this.next = null;
  }

  public ListNode insert(ListNode list, int data) {
    if (list == null) {
      return new ListNode(data);
    }
    list.next = new ListNode(data);

    return list;
  }

  public void appendToTail(int d) {
    ListNode end = new ListNode(d);
    ListNode n = this;
    while (n.next != null) {
      n = n.next;
    }
    n.next = end;
  }

  public void printList(ListNode list) {
    ListNode currNode = list;

    System.out.print("\nLinkedList: ");
    while (currNode != null) {
      System.out.print(currNode.data + " -> ");
      currNode = currNode.next;
    }
    System.out.println();
  }

  public static ListNode deleteByKey(ListNode head, int key) {
    ListNode node = head;

    if (node != null && node.data == key) {
      System.out.println(key + " found and deleted");
      return head.next;
    }
    while (node.next != null) {
      if (node.next.data == key) {
        node.next = node.next.next;

        System.out.println(key + " found and deleted");
        return head;
      }
      node = node.next;
    }
    System.out.println(key + " not found");

    return head;
  }

  public static ListNode deleteAtPosition(ListNode list, int index) {
    ListNode currNode = list, prev = null;

    if (index == 0 && currNode != null) {
      list = currNode.next;

      System.out.println(index + " position element deleted");
      return list;
    }
    int counter = 0;

    while (currNode != null) {
      if (counter == index) {
        prev.next = currNode.next;

        System.out.println(index + " position element deleted");
        break;
      } else {
        prev = currNode;
        currNode = currNode.next;
        counter++;
      }
    }

    if (currNode == null) {
      System.out.println(index + " position element not found");
    }

    return list;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + data;
    result = prime * result + ((next == null) ? 0 : next.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    ListNode other = (ListNode) obj;
    if (data != other.data)
      return false;
    if (next == null) {
      if (other.next != null)
        return false;
    } else if (!next.equals(other.next))
      return false;
    return true;
  }

}
