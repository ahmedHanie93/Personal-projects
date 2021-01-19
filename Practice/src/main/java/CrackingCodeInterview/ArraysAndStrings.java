package CrackingCodeInterview;

import java.util.HashMap;
import java.util.HashSet;

public class ArraysAndStrings {

  // Implement an algorithm to determine if a string has all unique characters.
  // What if you cannot use additional data structures?
  public static boolean isUnique(String s) {
    // Number of ASCII characters
    int length = s.length();
    if (length > 128) {
      return false;
    }

    boolean[] charsOcc = new boolean[128];

    for (int i = 0; i < length; i++) {
      int charAscii = s.charAt(i);

      if (charsOcc[charAscii]) {
        return false;
      }
      charsOcc[charAscii] = true;
    }

    return true;
  }

  // Given two strings, write a method to decide if one is a permutation of the other.
  public static boolean checkPermutation(String s1, String s2) {
    if (s1.length() != s2.length()) {
      return false;
    }
    HashMap<Character, Integer> occDiff = new HashMap();

    for (int i = 0; i < s1.length(); i++) {
      char c1 = s1.charAt(i);
      char c2 = s2.charAt(i);
      Integer occ1 = initOccIfNewChar(occDiff.get(c1));
      Integer occ2 = initOccIfNewChar(occDiff.get(c2));

      occDiff.put(c1, ++occ1);
      occDiff.put(c2, --occ2);
    }
    for (Character c : occDiff.keySet()) {
      if (occDiff.get(c) != 0) {
        return false;
      }
    }

    return true;
  }

  private static Integer initOccIfNewChar(Integer occ1) {
    return occ1 == null ? 0 : occ1;
  }

  // URLify: Write a method to replace all spaces in a string with '%20'. You may assume that the
  // string has sufficient space at the end to hold the additional characters, and that you are
  // given the "true" length of the string. (Note: if implementing in Java, please use a character
  // array so that you can perform this operation in place.)
  public static void urlifyString(char[] a, int truelength) {
    int spaceCount = 0;
    int index;
    int i;

    for (i = 0; i < truelength; i++) {
      if (a[i] == ' ') {
        spaceCount++;
      }
    }
    index = truelength + spaceCount * 2;
    if (a.length > truelength) {
      a[truelength] = '\0';
    }
    for (i = truelength - 1; i >= 0; i--) {
      if (a[i] == ' ') {
        a[index - 1] = '0';
        a[index - 2] = '2';
        a[index - 3] = '%';
        index -= 3;
      } else {
        a[index - 1] = a[i];
        index--;
      }
    }
  }

  // Palindrome Permutation: Given a string, write a function to check if it is a permutation of
  // a palindrome. A palindrome is a word or phrase that is the same forwards and backwards. A
  // permutation is a rearrangement of letters. The palindrome does not need to be limited to just
  // dictionary words.
  public static boolean isPalindromePermutation(String s) {
    HashMap<Character, Integer> occ = new HashMap();

    for (int i = 0; i < s.length(); i++) {
      Integer value = occ.get(s.charAt(i));
      if (value == null)
        value = 0;
      occ.put(s.charAt(i), ++value);
    }
    int oddOccs = 0;

    for (Character key : occ.keySet()) {
      if (occ.get(key) % 2 != 0) {
        oddOccs++;
        if (oddOccs > 1)
          return false;
      }
    }

    return true;
  }

  // One Away: There are three types of edits that can be performed on strings: insert a character,
  // remove a character, or replace a character. Given two strings, write a function to check if
  // they are
  // one edit (or zero edits) away
  public static boolean isOneEditAway(String first, String second) {
    int l1 = first.length();
    int l2 = second.length();
    boolean isSameLength = l1 == l2;

    if (!isSameLength && Math.abs(l1 - l2) > 1) {
      return false;
    }
    int index1 = 0;
    int index2 = 0;
    String s1 = l1 < l2 ? first : second;
    second = l1 < l2 ? second : first;
    boolean isEditted = false;

    while (index1 < s1.length() && index2 < second.length()) {
      if (s1.charAt(index1) != second.charAt(index2)) {
        if (isEditted) {
          return false;
        }
        if (isSameLength) {
          index1++;
        }
      } else {
        index1++;
      }
      index2++;
    }

    return true;
  }

  // String Compression: Implement a method to perform basic string compression using the counts
  // of repeated characters. For example, the string aabcccccaaa would become a2blc5a3. If the
  // "compressed" string would not become smaller than the original string, your method should
  // return the original string. You can assume the string has only uppercase and lowercase letters
  // (a -z).
  public static String getCompressedString(String s) {
    int length = s.length();
    StringBuilder sb = new StringBuilder(length);
    int charsCount = 0;

    for (int i = 0; i < length; i++) {
      charsCount++;
      if (i + 1 >= length || s.charAt(i) != s.charAt(i + 1)) {
        sb.append(s.charAt(i));
        sb.append(charsCount);
        charsCount = 0;
      }
    }

    return sb.toString().length() > length ? sb.toString() : s;
  }

  // Rotate Matrix: Given an image represented by an NxN matrix, where each pixel in the image is 4
  // bytes, write a method to rotate the image by 90 degrees. Can you do this in place?
  public static boolean rotateMatrix(int[][] matrix) {
    int length = matrix.length;
    if (length == 0 || length != matrix[0].length) {
      return false;
    }

    for (int layer = 0; layer < length / 2; layer++) {
      int first = layer;
      int last = length - 1 - layer;
      for (int i = first; i < last; i++) {
        int offset = last - i;
        int top = matrix[first][i];
        // Left --> Top
        matrix[first][i] = matrix[offset][first];
        // Bottom --> Left
        matrix[offset][first] = matrix[last][offset];
        // Right --> Bottom
        matrix[last][offset] = matrix[offset][last];
        // top --> Right
        matrix[offset][last] = top;
      }
    }

    return true;
  }

  // Zero Matrix: Write an algorithm such that if an element in an MxN matrix is 0, its entire row
  // and column are set to 0.
  public static void setZeroMatix(int[][] matrix) {
    HashSet<Integer> rows = new HashSet();
    HashSet<Integer> columns = new HashSet();
    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[0].length; j++) {
        if (matrix[i][j] == 0) {
          rows.add(i);
          columns.add(j);
        }
      }
    }
    for (Integer i : rows) {
      for (int j = 0; j < matrix[i].length; j++)
        matrix[i][j] = 0;
    }
    for (Integer i : columns) {
      for (int j = 0; j < matrix[i].length; j++)
        matrix[j][i] = 0;
    }
  }

  // String Rotation: Assume you have a method i 5Su b 5 tr ing which checks if one word is a
  // substring of another. Given two strings, 51 and 52, write code to check if 52 is a rotation of
  // 51 using only one call to isSubstring (e.g., "waterbottle" is a rotation of" erbottlewat").
  public static boolean isRotation(String s1, String s2) {
    if (s1.length() == s2.length() && s2.length() > 0) {
      String s1s1 = s1 + s1;

      // substring usage
      return s1s1.indexOf(s2) != -1;
    }
    return false;
  }


}
