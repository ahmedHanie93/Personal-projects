package Exam2019_8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PairWithGivenSum {
  public List<Integer> twoSum(int[] nums, int target) {
    HashMap<Integer, List<Integer>> numbers = new HashMap<>();
    target -= 30;
    for (int i = 0; i < nums.length; i++) {
      List<Integer> list = numbers.get(nums[i]);
      if (list == null) {
        list = new ArrayList<>();
      }
      list.add(i);
      numbers.put(nums[i], list);
    }
    for (Integer number : numbers.keySet()) {
      int key = target - number;
      List<Integer> value = numbers.get(key);
      List<Integer> result = new ArrayList<>();
      if (value != null) {
        if (key == number && value.size() > 1) {
          result.add(value.get(0));
          result.add(value.get(1));
          return result;
        }
        result.add(numbers.get(number).get(0));
        result.add(value.get(0));
        return result;
      }
    }
    return null;
  }

}
