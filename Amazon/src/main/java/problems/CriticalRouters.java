package problems;

import static java.lang.Math.min;
import java.util.List;

public class CriticalRouters {

  private int n, id, rootNodeOutcomingEdgeCount;
  private boolean solved;
  private int[] low, ids;
  private boolean[] visited, isArticulationPoint;
  private List<List<Integer>> graph;

  public CriticalRouters(List<List<Integer>> graph, int n) {
    if (graph == null || n <= 0 || graph.size() != n)
      throw new IllegalArgumentException();
    this.graph = graph;
    this.n = n;
  }

  // Returns the indexes for all articulation points in the graph even if the
  // graph is not fully connected.
  public boolean[] findArticulationPoints() {
    if (solved)
      return isArticulationPoint;

    id = 0;
    low = new int[n]; // Low link values
    ids = new int[n]; // Nodes ids
    visited = new boolean[n];
    isArticulationPoint = new boolean[n];

    for (int i = 0; i < n; i++) {
      if (!visited[i]) {
        rootNodeOutcomingEdgeCount = 0;
        dfs(i, i, -1);
        isArticulationPoint[i] = (rootNodeOutcomingEdgeCount > 1);
      }
    }

    solved = true;
    return isArticulationPoint;
  }

  private void dfs(int root, int at, int parent) {

    if (parent == root)
      rootNodeOutcomingEdgeCount++;

    visited[at] = true;
    low[at] = ids[at] = id++;

    List<Integer> edges = graph.get(at);
    for (Integer to : edges) {
      if (to == parent)
        continue;
      if (!visited[to]) {
        dfs(root, to, at);
        low[at] = min(low[at], low[to]);
        if (ids[at] <= low[to]) {
          isArticulationPoint[at] = true;
        }
      } else {
        low[at] = min(low[at], ids[to]);
      }
    }
  }

}
