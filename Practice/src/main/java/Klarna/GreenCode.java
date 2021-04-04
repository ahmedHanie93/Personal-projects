package Klarna;

public class GreenCode {

  // More than one rule FALSE ==> RED
  // One rule FALSE ==> YELLOW
  // All rules TRUE ==> GREEN
  private static final String RED = "red";
  private static final String YELLOW = "yellow";
  private static final String GREEN = "green";

  public static String verify(Float[] cpuUsage, Integer[] usedHeap) {
    if (cpuUsage == null && usedHeap == null)
      return RED;

    int cpuUsageSize = cpuUsage.length;
    int usedHeapSize = usedHeap.length;
    int violations = 0;
    float maxCpuUsage = 0;
    int maxUsedHeap = 0;
    int cpuUsageNullCount = 0;
    int usedHeapNullCount = 0;

    for (int i = 0; i < cpuUsageSize || i < usedHeapSize; i++) {
      if (violations > 1) {
        return RED;
      }

      if (i < cpuUsageSize) {
        if (cpuUsage[i] != null) {
          if (isCpuUsageMoreThenOrEqualNinety(cpuUsage[i])) {
            violations++;
          }

          if (cpuUsage[i] > maxCpuUsage) {
            maxCpuUsage = cpuUsage[i];
          }
        } else {
          cpuUsageNullCount++;
        }
      }

      if (i < usedHeapSize) {
        if (usedHeap[i] != null) {
          if (i + 2 < usedHeapSize && hasUsedHeapThreeConsecutiveAscendingElements(usedHeap[i],
              usedHeap[i + 1], usedHeap[i + 2])) {
            violations++;
          }
          if (isUsedHeapMoreThenOrEqualSixty(usedHeap[i])) {
            violations++;
          }
          if (usedHeap[i] > maxUsedHeap) {
            maxUsedHeap = usedHeap[i];
          }
        } else {
          usedHeapNullCount++;
        }
      }
    }

    if (isBothAllNulls(cpuUsageSize, usedHeapSize, cpuUsageNullCount, usedHeapNullCount)
        || isCpuUsageNullsMoreThen30Percent(cpuUsageNullCount, cpuUsageSize)
        || isUsedHeapNullsMoreThen25Percent(usedHeapNullCount, usedHeapSize)) {
      return RED;
    }

    if (isCpuUsageAndMemoryMax(cpuUsage, usedHeap, cpuUsageSize, usedHeapSize, maxCpuUsage,
        maxUsedHeap)) {
      violations++;
    }

    return violations == 0 ? GREEN : violations > 1 ? RED : YELLOW;
  }

  // RULE 1
  private static boolean isCpuUsageMoreThenOrEqualNinety(float cpuUsage) {
    return cpuUsage >= 0.9f;
  }

  // RULE 2
  private static boolean isCpuUsageAndMemoryMax(Float[] cpuUsage, Integer[] usedHeap,
      int cpuUsageSize, int usedHeapSize, float maxCpuUsage, int maxUsedHeap) {
    if (cpuUsageSize == 1 && usedHeapSize == 1) {
      return false;
    }

    for (int i = 0; i < cpuUsageSize && i < usedHeapSize; i++) {
      if (cpuUsage[i] != null && usedHeap[i] != null && cpuUsage[i] == maxCpuUsage
          && usedHeap[i] == maxUsedHeap) {
        return true;
      }
    }

    return false;
  }

  // RULE 3
  private static boolean isUsedHeapMoreThenOrEqualSixty(float usedHeap) {
    return usedHeap / 32768 >= 0.6f;
  }

  // RULE 4
  private static boolean hasUsedHeapThreeConsecutiveAscendingElements(Integer usedHeap1,
      Integer usedHeap2, Integer usedHeap3) {
    if (usedHeap1 == null || usedHeap2 == null || usedHeap3 == null)
      return false;
    return usedHeap3 > usedHeap2 && usedHeap2 > usedHeap1;
  }

  // RULE 5 Always RED
  private static boolean isCpuUsageNullsMoreThen30Percent(float nullsCount, int cpuUsageSize) {
    return nullsCount / cpuUsageSize > 0.3f;
  }

  // RULE 6 Always RED
  private static boolean isUsedHeapNullsMoreThen25Percent(float nullsCount, int usedHeapSize) {
    return nullsCount / usedHeapSize > 0.25f;
  }

  // RULE 7 Always RED
  private static boolean isBothAllNulls(int cpuUsageSize, int usedHeapSize, int cpuUsageNullCount,
      int usedHeapNullCount) {
    return cpuUsageNullCount == cpuUsageSize && usedHeapNullCount == usedHeapSize;
  }

}

