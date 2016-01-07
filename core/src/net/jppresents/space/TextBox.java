package net.jppresents.space;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Align;

public class TextBox {
  private static NinePatch ninePatch;
  String text = "";
  BitmapFont font;
  int alpha = 0;
  boolean fullSize = false;

  public TextBox(TextureAtlas textureAtlas, String name, boolean fullSize) {
    if (ninePatch == null) {
      ninePatch = textureAtlas.createPatch(name);
    }
    font = SpaceMain.assets.getFont();
    this.fullSize = fullSize;
  }

  public void render(Batch batch, Camera camera) {
    if (!isActive())
      return;

    if (alpha < 255) {
      alpha += 5;
    }

    float left = camera.position.x - camera.viewportWidth/2 + camera.viewportWidth * 0.1f;
    float bottom = camera.position.y - camera.viewportHeight/2;
    batch.setColor(1, 1, 1, alpha/255f);
    float height;
    if (fullSize) {
      height = camera.viewportHeight/2;
    } else {
      height = camera.viewportHeight;
    }
    ninePatch.draw(batch, left, bottom, camera.viewportWidth * 0.8f, height);
    font.setColor(102/255f, 1, 0, alpha/255f);
    font.draw(batch, text, left + 50, bottom + height - 50, camera.viewportWidth * 0.8f - 100, Align.left, true);
  }

  public void hide() {
    setText("");
  }

  public void setText(String text) {
    alpha = 0;
    this.text = text;
  }

  boolean isDone() {
    return alpha == 255;
  }

  boolean isActive() {
    return text.length() != 0;
  }
}
