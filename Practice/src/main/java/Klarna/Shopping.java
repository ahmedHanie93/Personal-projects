package Klarna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class Shopping {

  private static final String CLOTHING = "Clothing";
  private static final String SPORTS = "Sports";
  private static final String ACCESSORIES = "Accessories";
  private static final String ELECTRONICS = "Electronics";
  private static final Map<String, List<String>> itemsCategories = initCategoris();

  private static Map<String, List<String>> initCategoris() {
    Map<String, List<String>> itemsCategories = new HashMap();

    String[] cloth = {"shirt", "sweater", "jacket", "shorts", "shoes"};
    addItemsCategory(itemsCategories, cloth, CLOTHING);

    String[] sport = {"socks", "shoes", "shorts", "football", "helmet", "bag"};
    addItemsCategory(itemsCategories, sport, SPORTS);

    String[] accessories = {"bag", "belt", "hat", "sunglasses", "watch"};
    addItemsCategory(itemsCategories, accessories, ACCESSORIES);

    String[] electronics = {"tv", "camera", "headphones", "laptop"};
    addItemsCategory(itemsCategories, electronics, ELECTRONICS);

    return itemsCategories;
  }

  private static void addItemsCategory(Map<String, List<String>> itemsCategories, String[] cloth,
      String category) {
    for (String s : cloth) {
      List<String> categories = itemsCategories.get(s);

      if (categories == null) {
        categories = new ArrayList<String>();
        itemsCategories.put(s, categories);
      }
      categories.add(category);
    }
  }

  public static List<String> popularShoppingCategories(Map<String, List<String>> usersPurchases) {
    if (usersPurchases == null || usersPurchases.isEmpty()) {
      return Collections.emptyList();
    }

    Map<String, Integer> categoryOccurences = new HashMap();
    Map<Integer, TreeSet<String>> shoppingCategories = new HashMap();
    List<String> purchasedItems = new ArrayList();
    Integer maxOcc = 0;

    for (String user : usersPurchases.keySet()) {
      purchasedItems.addAll(usersPurchases.get(user));
    }

    for (String item : purchasedItems) {
      if (item == "") {
        continue;
      }
      List<String> categories = itemsCategories.get(formatItem(item));

      if (categories == null) {
        throw new IllegalArgumentException();
      }
      maxOcc = getMaxOccAndUpdateCategoryOccurences(categoryOccurences, maxOcc, categories);
    }

    if (maxOcc == 0) {
      throw new IllegalArgumentException();
    }
    setShoppingCategories(categoryOccurences, shoppingCategories);

    return new ArrayList(shoppingCategories.get(maxOcc));
  }

  private static void setShoppingCategories(Map<String, Integer> categoryOccurences,
      Map<Integer, TreeSet<String>> shoppingCategories) {
    for (String category : categoryOccurences.keySet()) {
      Integer occ = categoryOccurences.get(category);
      TreeSet<String> occCategories = shoppingCategories.get(occ);

      if (occCategories == null) {
        occCategories = new TreeSet();
        shoppingCategories.put(occ, occCategories);
      }
      occCategories.add(category);
    }
  }

  private static Integer getMaxOccAndUpdateCategoryOccurences(
      Map<String, Integer> categoryOccurences, Integer maxOcc, List<String> categories) {
    for (String category : categories) {
      Integer occ = categoryOccurences.get(category);

      if (occ == null) {
        occ = 0;
      }
      categoryOccurences.put(category, ++occ);
      maxOcc = maxOcc > occ ? maxOcc : occ;
    }
    return maxOcc;
  }

  private static String formatItem(String item) {
    return item == null ? null : item.toLowerCase();
  }

}
