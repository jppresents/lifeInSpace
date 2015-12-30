package net.jppresents.space;

import com.badlogic.gdx.math.Vector2;

import java.util.List;

public interface SetPath {
  List<Vector2> getPath();
  void setPathLength(int length);
}
