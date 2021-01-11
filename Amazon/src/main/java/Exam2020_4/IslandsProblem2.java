package Exam2020_4;

import java.util.List;

public class IslandsProblem2 {
  int numberAmazonGoStores(int rows, int column, List<List<Integer>> grid1) {
    Integer[][] grid =
        grid1.stream().map(l -> l.stream().toArray(Integer[]::new)).toArray(Integer[][]::new);
    if (grid.length == 0)
      return 0;
    int m = rows, n = column;
    int ans = 0;
    boolean[][] visited = new boolean[m][n];
    for (int r = 0; r < m; r++) {
      for (int c = 0; c < n; c++) {
        ans += dfs(grid, m, n, visited, r, c);
      }
    }
    return ans;
  }

  int[] DIR = new int[] {0, 1, 0, -1, 0};

  int dfs(Integer[][] grid, int m, int n, boolean[][] visited, int r, int c) {
    if (r < 0 || r == m || c < 0 || c == n || visited[r][c] || grid[r][c] == '0')
      return 0;
    visited[r][c] = true;
    for (int i = 0; i < 4; i++) {
      dfs(grid, m, n, visited, r + DIR[i], c + DIR[i + 1]);
    }
    return 1;
  }

}
