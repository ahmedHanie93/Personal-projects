package Klarna;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import Klarna.Shopping;

public class ShoppingTest {

  private List<String> initListFromArray(String[] songs) {
    List<String> list = new ArrayList<String>();
    for (String s : songs)
      list.add(s);
    return list;
  }

  private static void initMap(Map<String, List<String>> itemsCategories, String key,
      List<String> value) {
    itemsCategories.put(key, value);
  }


  @Test
  public void validInputOnePopularCategory() {
    String[] arr = {"shirt", "sweater", "bag", "belt", "watch"};
    String[] arr1 = {"headphones"};
    String[] arr2 = {"Accessories"};
    Map<String, List<String>> usersPurchases = new HashMap<String, List<String>>();
    initMap(usersPurchases, "Anna", initListFromArray(arr));
    initMap(usersPurchases, "Sofie", initListFromArray(arr1));

    assertEquals(initListFromArray(arr2), Shopping.popularShoppingCategories(usersPurchases));
  }

  @Test
  public void validInputManyPopularCategories() {
    Map<String, List<String>> usersPurchases = new HashMap<String, List<String>>();
    String[] arr = {"Football", "Shirt", "Shoes", "headphones"};
    String[] arr1 = {"TV", "football"};
    String[] arr2 = {"shirt", "sweater", "", "belt", "bag"};
    String[] arr3 = {"Clothing", "Sports"};

    initMap(usersPurchases, "Michael", initListFromArray(arr));
    initMap(usersPurchases, "Sara", initListFromArray(arr1));
    initMap(usersPurchases, "Daniel", initListFromArray(arr2));

    assertEquals(initListFromArray(arr3), Shopping.popularShoppingCategories(usersPurchases));
  }

  @Test
  public void validInputManyPopularCategories2() {
    Map<String, List<String>> usersPurchases = new HashMap<String, List<String>>();
    String[] arr = {"headphones", "hat", "laptop", "helmet"};
    String[] arr1 = {"Shirt", "Shoes", "football"};
    String[] arr2 = {"shirt", "sweater", "watch", "belt", "bag"};
    String[] arr3 = {"Accessories", "Clothing", "Sports"};

    initMap(usersPurchases, "Michael", initListFromArray(arr));
    initMap(usersPurchases, "Sara", initListFromArray(arr1));
    initMap(usersPurchases, "Daniel", initListFromArray(arr2));

    assertEquals(initListFromArray(arr3), Shopping.popularShoppingCategories(usersPurchases));
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidInput() {
    Map<String, List<String>> usersPurchases = new HashMap<String, List<String>>();
    String[] arr = {"shirt", "dress"};
    initMap(usersPurchases, "Jane", initListFromArray(arr));
    Shopping.popularShoppingCategories(usersPurchases);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidInput2() {
    Map<String, List<String>> usersPurchases = new HashMap<String, List<String>>();
    String[] arr = {null, "shirt"};
    initMap(usersPurchases, "Jane", initListFromArray(arr));
    Shopping.popularShoppingCategories(usersPurchases);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidInput3() {
    Map<String, List<String>> usersPurchases = new HashMap<String, List<String>>();
    String[] arr = {"", "", ""};
    initMap(usersPurchases, "Jane", initListFromArray(arr));
    Shopping.popularShoppingCategories(usersPurchases);
  }

  @Test
  public void emptyInput() {
    Map<String, List<String>> usersPurchases = new HashMap();
    assertEquals(Collections.emptyList(), Shopping.popularShoppingCategories(usersPurchases));
  }

}
