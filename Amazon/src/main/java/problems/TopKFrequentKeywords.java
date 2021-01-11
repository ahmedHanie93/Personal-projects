package problems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class TopKFrequentKeywords {
  // Given a list of reviews, a list of keywords and an integer k. Find the most popular k keywords
  // in order of most to least frequently mentioned.
  // The comparison of strings is case-insensitive. If keywords are mentioned an equal number of
  // times in reviews, sort alphabetically.
  public static List<String> getTopKFrequentKeywords(int k, String[] keywords, String[] reviews) {
    List<String> frequentKeywords = new ArrayList<>();
    HashMap<String, Integer> KeywordsOccurences = new HashMap<>();
    TreeMap<Integer, List<String>> occurencesKeywords = new TreeMap<>(Collections.reverseOrder());
    for (String keyword : keywords) {
      KeywordsOccurences.put(keyword.toLowerCase(), 0);
    }
    for (String review : reviews) {
      String[] words = review.toLowerCase().split(" ");
      for (String word : words) {
        Integer occ = KeywordsOccurences.get(word);
        if (occ != null) {
          KeywordsOccurences.put(word, ++occ);
        }
      }
    }
    for (String key : KeywordsOccurences.keySet()) {
      List<String> keywordsList = occurencesKeywords.get(KeywordsOccurences.get(key));
      if (keywordsList == null) {
        keywordsList = new ArrayList<>();
      }
      keywordsList.add(key);
      occurencesKeywords.put(KeywordsOccurences.get(key), keywordsList);
    }
    Iterator<List<String>> iterator = occurencesKeywords.values().iterator();
    int i = 0;
    while (iterator.hasNext() && i < k) {
      List<String> keywordsList = iterator.next();
      i += keywordsList.size();
      frequentKeywords.addAll(keywordsList);
    }
    return frequentKeywords;
  }
}
