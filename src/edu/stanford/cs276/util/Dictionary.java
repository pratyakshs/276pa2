package edu.stanford.cs276.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Dictionary implements Serializable {

  private int termCount;
  private HashMap<Integer, Integer> map;

  public int termCount() {
    return termCount;
  }

  public Dictionary() {
    termCount = 0;
    map = new HashMap<Integer, Integer>();
  }

  public void add(Integer term) {
    termCount++;
    if (map.containsKey(term)) {
      map.put(term, map.get(term) + 1);
    } else {
      map.put(term, 1);
    }
  }

  public int count(Integer term) {
    if (map.containsKey(term)) {
      return map.get(term);
    } else {
      return 0;
    }
  }


  public Iterator<Map.Entry<Integer, Integer>> getIterator() {
      return map.entrySet().iterator();
  }
}
