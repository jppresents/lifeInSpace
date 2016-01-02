package net.jppresents.space;

public interface GameObject {
  void render();
  void update();
  float getX();
  float getY();
  int getHealth();
}
