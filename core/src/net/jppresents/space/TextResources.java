package net.jppresents.space;

import java.util.ArrayList;
import java.util.HashMap;

public class TextResources {
  private ArrayList<String> worlds;
  private HashMap<String, String> texts;

  public String getText(String key) {
    if (texts.containsKey(key))
      return texts.get(key);
    return "Error :(\nText " + key + " not found";
  }

  public ArrayList<String> getWorlds() {
    return worlds;
  }
}
