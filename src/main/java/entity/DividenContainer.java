package entity;

import java.util.HashMap;
import java.util.List;


public class DividenContainer {
  private HashMap<String, List<ImmutableDividen>> map;

  private void remove(String key){
    map.remove(key);
  }

  public void add(String key, List<ImmutableDividen> list){
    map.put(key, list);
  }

  public List<ImmutableDividen> get(String key){
    return map.get(key);
  }
}