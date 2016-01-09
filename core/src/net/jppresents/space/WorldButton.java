package net.jppresents.space;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class WorldButton extends TextButton {
  private String world;

  public WorldButton(String text, Skin skin, String world) {
    super(text, skin);
    this.world = world;
  }

  public String getWorld() {
    return world;
  }
}
