package Demo2020_4;

public class Generalized_GCD {
  // Generalized GCD
  private static int gcd(int a, int b) {
    if (a == 0)
      return b;
    return gcd(b % a, a);
  }

  public static int generalizedGCD(int n, int arr[]) {
    int result = arr[0];
    for (int i = 1; i < n; i++) {
      result = gcd(arr[i], result);
      if (result == 1) {
        return 1;
      }
    }
    return result;
  }

}
