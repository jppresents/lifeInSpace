package net.jppresents.space;

public class GameEvent {
  public boolean active;

  public enum EventType {TEXT, ENDING, NONE, TELEPORT}

  int locX, locY, width, height;
  EventType type;
  String key;

  public GameEvent(int locX, int locY, int width, int height, EventType type, String key) {
    this.locX = locX;
    this.locY = locY;
    this.width = width;
    this.height = height;
    this.type = type;
    this.key = key;
    active = true;
  }

}
