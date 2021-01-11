package problems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FavoriteMusicGenres {

  public static Map<String, List<String>> favoriteGenre(Map<String, List<String>> userSongs,
      Map<String, List<String>> songGenres) {
    Map<String, String> songToGenre = new HashMap<>();
    songGenres.forEach((genre, songs) -> songs.forEach(song -> songToGenre.put(song, genre)));
    Map<String, List<String>> favoriteGenre = new HashMap<>();
    userSongs.forEach(
        (user, songs) -> favoriteGenre.put(user, calculateFavoriteGenre(songs, songToGenre)));
    return favoriteGenre;
  }

  private static List<String> calculateFavoriteGenre(List<String> songs,
      Map<String, String> songToGenre) {
    TreeMap<Integer, List<String>> occOfGenresMap = new TreeMap<>(Collections.reverseOrder());
    HashMap<String, Integer> genreOccurences = new HashMap<>();
    for (String song : songs) {
      String genre = songToGenre.get(song);
      Integer occ = genreOccurences.get(genre);
      if (occ == null) {
        occ = 0;
      }
      genreOccurences.put(genre, ++occ);
    }
    for (String genre : genreOccurences.keySet()) {
      Integer occ = genreOccurences.get(genre);
      List<String> genrePerOcc = occOfGenresMap.get(occ);
      if (genrePerOcc == null) {
        genrePerOcc = new ArrayList<>();
      }
      genrePerOcc.add(genre);
      occOfGenresMap.put(occ, genrePerOcc);
    }
    List<String> result = occOfGenresMap.firstEntry().getValue();
    return result == null ? new ArrayList<String>() : result;
  }

}
