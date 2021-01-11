package problems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReorderDataInLogFiles {
  // the first word in each log is an alphanumeric identifier.
  // Reorder the logs so that all of the letter-logs come before any digit-log. The letter-logs are
  // ordered lexicographically ignoring identifier, with the identifier used in case of ties. The
  // digit-logs should be put in their original order.

  public static String[] reorderLogFiles(String[] logs) {
    final String regex = "\\d+";
    Pattern pattern = Pattern.compile(regex);
    int i = 0;
    String[] result = new String[logs.length];
    List<String> stringsList = new ArrayList<>();
    List<String> digitList = new ArrayList<>();
    seperateLogsToDigitAndStringLists(logs, pattern, stringsList, digitList);
    Collections.sort(stringsList, (o1, o2) -> {
      String[] o1Partition = o1.split(" ");
      String[] o2Partition = o2.split(" ");

      for (int j = 1; j < Math.min(o1Partition.length, o2Partition.length); j++) {
        int compareTo = o1Partition[j].compareTo(o2Partition[j]);
        if (compareTo != 0)
          return compareTo;
      }
      return o1Partition[0].compareTo(o2Partition[0]);
    });
    stringsList.addAll(digitList);
    for (String x : stringsList) {
      result[i++] = x;
    }
    return result;
  }

  private static void seperateLogsToDigitAndStringLists(String[] logs, Pattern pattern,
      List<String> stringsList, List<String> digitList) {
    for (String log : logs) {
      String[] logsPartition = log.split(" ");
      Matcher matcher = pattern.matcher(logsPartition[1]);
      if (matcher.matches()) {
        digitList.add(log);
      } else {
        stringsList.add(log);
      }
    }
  }

}
