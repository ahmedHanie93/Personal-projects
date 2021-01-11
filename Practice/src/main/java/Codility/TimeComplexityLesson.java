package Codility;

public class TimeComplexityLesson {

  public static int getFrogJmp(int X, int Y, int D) {
    if (X == Y)
      return 0;
    int diff = Y - X;
    return diff % D == 0 ? diff / D : diff / D + 1;
  }

  public static int getMissingElement(int[] A) {
    int length = A.length;
    if (length == 0)
      return 1;
    int sum = 0;
    int correctSum = 0;
    for (int i = 0; i < length; i++) {
      correctSum += i + 1;
      sum += A[i];
    }
    return correctSum + length + 1 - sum;
  }

  public static int getMinDiff(int[] A) {
    int length = A.length;
    int sumA = 0;
    int sumB = 0;
    for (Integer x : A)
      sumB += x;
    int min = Integer.MAX_VALUE;
    for (int i = 0; i < length - 1; i++) {
      sumA += A[i];
      sumB -= A[i];
      int diff = Math.abs(sumA - sumB);
      min = Math.min(diff, min);
    }
    return min;
  }

}
