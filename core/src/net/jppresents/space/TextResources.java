package net.jppresents.space;

import java.util.*;

public class TextResources {
  private ArrayList<String> audio;
  private ArrayList<String>  worlds;
  private HashMap<String, String> texts;

  public String getText(String key) {
    if (texts.containsKey(key))
      return texts.get(key);
    return "Error :(\nText " + key + " not found";
  }

  public List<String> getWorlds() {
    return worlds;
  }

  public boolean isAudioAvailable(String radioFile) {
    return audio.contains(radioFile);
  }
}
