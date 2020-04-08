package Adyen;

import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

public class MaxNailsWithSameLength {
	public static int getMaxNailsWithSameLength(int[] A, int Y) {
		final int MAX_N_WITH_SAME_LENGTH = A.length;
		if (MAX_N_WITH_SAME_LENGTH == 0 || MAX_N_WITH_SAME_LENGTH == 1) {
			return MAX_N_WITH_SAME_LENGTH;
		}
		TreeMap<Integer, Integer> occurences = getOccurences(A);
		HashMap<Integer, Integer> occOfElementAndNextElements = new HashMap<>();
		setOccOfElementAndNextElements(Y, occurences, occOfElementAndNextElements);
 
		return getMaxSum(occOfElementAndNextElements);
	}
 
	private static int getMaxSum(HashMap<Integer, Integer> occOfElementAndNextElements) {
		int max = 0;
		for (Integer occ : occOfElementAndNextElements.keySet()) {
			int s = occOfElementAndNextElements.get(occ) + occ;
			max = max < s ? s : max;
		}
 
		return max;
	}
 
	private static void setOccOfElementAndNextElements(int Y, TreeMap<Integer, Integer> occAsValueMap,
			HashMap<Integer, Integer> occOfElementAndNextElements) {
		boolean lastElement = true;
		int sum = 0;
		for (Integer element : occAsValueMap.keySet()) {
			Integer occOfElemnt = occAsValueMap.get(element);
			if (lastElement) {
				sum += occOfElemnt;
				lastElement = false;
			} else {
				int key = occOfElemnt;
				sum = sum > Y ? Y : sum;
				occOfElementAndNextElements.put(key, sum);
				sum += occOfElemnt;
			}
 
		}
	}
 
	private static TreeMap<Integer, Integer> getOccurences(int[] A) {
		TreeMap<Integer, Integer> occ = new TreeMap<>(Collections.reverseOrder());
		for (int i = 0; i < A.length; i++) {
			Integer occurence = occ.get(A[i]);
			if (occurence == null) {
				occurence = 0;
			}
			occ.put(A[i], ++occurence);
		}
		return occ;
	}
 
	public static void main(String[] args) {
		int[] a = { 1, 1, 1, 2, 3, 4, 4, 4, 4, 4, 5, 5, 5, 5 };
		int y = 5;
		System.out.println(getMaxNailsWithSameLength(a, y));
	}
 
}