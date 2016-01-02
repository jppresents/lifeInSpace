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
  private boolean fixedToCamera = true;

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
    targetWidth = Math.round(fillRegion.getRegionWidth() * value / maxValue);
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

    float renderX = x;
    float renderY = y;
    if (fixedToCamera) {
      renderX += camera.position.x - camera.viewportWidth / 2;
      renderY += camera.position.y - camera.viewportHeight / 2;
    } else {
      renderX -= barRegion.getRegionWidth()/2;
      renderY += barRegion.getRegionHeight()/2;
    }

    batch.draw(barRegion, renderX, renderY);
    batch.draw(fillRegion.getTexture(), renderX, renderY, currentWidth, fillRegion.getRegionHeight(), fillRegion.getRegionX(), fillRegion.getRegionY(), currentWidth, fillRegion.getRegionHeight(), false, false);
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

  public void setValueNoAnimation(int value) {
    setValue(value);
    currentWidth = targetWidth;
  }

  public void setFixedToCamera(boolean fixedToCamera) {
    this.fixedToCamera = fixedToCamera;
  }
}
