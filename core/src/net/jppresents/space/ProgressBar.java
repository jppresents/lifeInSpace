package net.jppresents.space;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ProgressBar {
  private float maxValue;
  private float value;
  private float x;
  private float y;
  private TextureRegion barRegion;
  private TextureRegion fillRegion;
  private int targetWidth;
  private int currentWidth;

  public ProgressBar(float x, float y, float maxValue, float value, TextureRegion barRegion, TextureRegion fillRegion) {
    this.x = x;
    this.y = y;
    this.barRegion = barRegion;
    this.fillRegion = fillRegion;
    this.value = value;
    setMaxValue(maxValue);
  }

  public void setValue(float value) {
    this.value = value;
    targetWidth = Math.round(fillRegion.getRegionWidth() * value/maxValue);
  }

  public void setMaxValue(float maxValue) {
    this.maxValue = maxValue;
    setValue(value);
  }

  public void render(Batch batch, Camera camera) {
    if (currentWidth < targetWidth) {
      currentWidth += 20;
    } else if (currentWidth > targetWidth) {
      currentWidth -= 20;
    }

    if (Math.abs(currentWidth - targetWidth) < 20) {
      currentWidth = targetWidth;
    }

    batch.draw(barRegion, x + camera.position.x - camera.viewportWidth/2, y + camera.position.y - camera.viewportHeight/2);
    batch.draw(fillRegion.getTexture(), x + camera.position.x - camera.viewportWidth/2, y + camera.position.y - camera.viewportHeight/2, currentWidth, fillRegion.getRegionHeight(), fillRegion.getRegionX(), fillRegion.getRegionY(), currentWidth, fillRegion.getRegionHeight(), false, false );
  }

  public int getHeight() {
    return barRegion.getRegionHeight();
  }

  public int getWidth() {
    return barRegion.getRegionWidth();
  }

  public void setY(int y) {
    this.y = y;
  }

  public void setX(int x) {
    this.x = x;
  }
}
