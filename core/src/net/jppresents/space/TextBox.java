package net.jppresents.space;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Align;

public class TextBox {
  private static NinePatch ninePatch;
  String text = "";
  BitmapFont font;
  int alpha = 0;
  int textPos = 0;
  boolean fullSize = false;
  private boolean quick;

  public TextBox(TextureAtlas textureAtlas, String name) {
    if (ninePatch == null) {
      ninePatch = textureAtlas.createPatch(name);
    }
    font = SpaceMain.assets.getFont();
    this.fullSize = false;
  }

  public void render(Batch batch, Camera camera) {
    if (!isActive())
      return;

    if (alpha < 255) {
      alpha += 5;
    }

    if (textPos < text.length()) {
      textPos++;
      if (quick)
        textPos += 3;
      if (textPos > text.length()) {
        textPos = text.length();
      }
    }

    float left = camera.position.x - camera.viewportWidth/2 + camera.viewportWidth * 0.1f;
    float bottom = camera.position.y - camera.viewportHeight/2;
    batch.setColor(1, 1, 1, alpha/255f);
    float height;
    if (fullSize) {
      height = camera.viewportHeight;
    } else {
      height = camera.viewportHeight/2;
    }
    ninePatch.draw(batch, left, bottom, camera.viewportWidth * 0.8f, height);
    font.setColor(102/255f, 1, 0, alpha/255f);
    font.draw(batch, getTextPart(), left + 50, bottom + height - 50, camera.viewportWidth * 0.8f - 100, Align.left, true);
  }

  private String getTextPart() {
    return text.substring(0, textPos);
  }

  public void hide() {
    setText("", fullSize);
  }

  public void setText(String text, boolean fullSize) {
    alpha = 0;
    textPos = 0;
    quick = false;
    this.fullSize = fullSize;
    this.text = text;
  }

  boolean isDone() {
    return alpha == 255 && textPos == text.length();
  }

  boolean isActive() {
    return text.length() != 0;
  }

  public void setQuick(boolean quick) {
    this.quick = quick;
  }
}
