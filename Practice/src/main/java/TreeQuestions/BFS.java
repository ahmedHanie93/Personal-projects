package TreeQuestions;

import java.util.LinkedList;
import java.util.Queue;

public class BFS {

  public static void LevelOrderTraversal(Node root) {
    Queue<Node> queue = new LinkedList();
    queue.add(root);
    while (!queue.isEmpty()) {
      Node currentNode = queue.poll();
      System.out.print(currentNode.data + " ");
      if (currentNode.left != null) {
        queue.add(currentNode.left);
      }
      if (currentNode.right != null) {
        queue.add(currentNode.right);
      }
    }
  }

}
