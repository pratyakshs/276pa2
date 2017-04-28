package edu.stanford.cs276.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Dictionary implements Serializable {

  private int termCount;
  private HashMap<String, Pair<Integer, Double>> map;

  public int termCount() {
    return termCount;
  }

  public Dictionary() {
    termCount = 0;
    map = new HashMap<String, Pair<Integer, Double>>();
  }

  public void add(String term) {
    termCount++;
    if (map.containsKey(term)) {
      map.get(term).setFirst(map.get(term).getFirst() + 1);
//      map.put(term, new Pair<Integer, Double>(map.get(term).getFirst() + 1, 0.0));
    } else {
      map.put(term, new Pair<Integer, Double>(1, 0.0));
    }
  }

  public int count(String term) {
    if (map.containsKey(term)) {
      return map.get(term).getFirst();
    } else {
      return 0;
    }
  }

  public void updateProb(String term, Double prob) {
      map.get(term).setSecond(prob);
  }

  public Double getProb(String term) {
      if (map.containsKey(term)) {
          return map.get(term).getSecond();
      } else {
          return 0.;
      }
  }

  public Iterator<Map.Entry<String, Pair<Integer, Double>>> getIterator() {
      return map.entrySet().iterator();
  }
}
