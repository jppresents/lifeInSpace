package net.jppresents.space;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Align;

public class TextBox {
  NinePatch ninePatch;
  String text = "x";
  BitmapFont font;
  int alpha = 0;

  public TextBox(TextureAtlas textureAtlas, String name) {
    ninePatch = textureAtlas.createPatch(name);
    font = SpaceMain.assets.getFont();
  }

  public void render(SpriteBatch batch, Camera camera) {
    if (!isActive())
      return;

    if (alpha < 255) {
      alpha += 5;
    }

    float left = camera.position.x - camera.viewportWidth/2 + camera.viewportWidth * 0.1f;
    float bottom = camera.position.y - camera.viewportHeight/2;
    batch.setColor(1, 1, 1, alpha/255f);
    ninePatch.draw(batch, left, bottom, camera.viewportWidth * 0.8f, camera.viewportHeight/2);
    font.setColor(102/255f, 1, 0, alpha/255f);
    font.draw(batch, text, left + 50, bottom + camera.viewportHeight/2 - 50, camera.viewportWidth * 0.8f - 100, Align.left, true);
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
