package net.jppresents.space;

import java.util.HashMap;

public class Texts {
  public HashMap<String, String> texts;

  public String getText(String key) {
    if (texts.containsKey(key))
      return texts.get(key);
    return "Error :(\nText " + key + " not found";
  }
}
