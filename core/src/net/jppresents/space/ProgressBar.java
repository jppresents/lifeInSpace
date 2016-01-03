package net.jppresents.space;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class ProgressBar {
  private float maxValue;
  private float value;
  private float x;
  private float y;
  private final TextureRegion barRegion;
  private final TextureRegion fillRegion;
  private final TextureRegion costRegion;
  private int targetWidth;
  private int currentWidth;
  private boolean fixedToCamera = true;
  private float costValue = 0;
  private boolean showValues = true;
  private int costAlpha = 0;
  private float costValueDisplay;

  public ProgressBar(float x, float y, float maxValue, float value, TextureRegion barRegion, TextureRegion fillRegion, TextureRegion costRegion) {
    this.x = x;
    this.y = y;
    this.barRegion = barRegion;
    this.fillRegion = fillRegion;
    this.costRegion = costRegion;
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

    //cost value
    if (costValue > 0 && costRegion != null) {
      costAlpha++;
      batch.setColor(1, 1, 1, 0.2f + 0.8f * Math.abs(MathUtils.sinDeg(costAlpha*2)));
      int costWidth = Math.round(costRegion.getRegionWidth() * costValue / maxValue);
      int costStart = Math.round(costRegion.getRegionWidth() * (maxValue - value)/maxValue);
      batch.draw(costRegion.getTexture(), renderX + costRegion.getRegionWidth() - costWidth - costStart, renderY, costWidth, costRegion.getRegionHeight(), costRegion.getRegionX() + (costRegion.getRegionWidth() - costWidth -costStart), costRegion.getRegionY(), costWidth, costRegion.getRegionHeight(), false, false);
      batch.setColor(1, 1, 1, 1);
    }

    if (showValues) {
      BitmapFont font = SpaceMain.assets.getFont();
      font.setColor(1, 1, 1, 1);
      String text;
      if (costValue == 0) {
        text = (int) value + " / " + (int) maxValue;
      } else {
        text = (int) value + "(-" + (int) costValueDisplay + ") / " + (int) maxValue;
        if (costValueDisplay > value) {
          font.setColor(1, 0, 0, 1);
        }
      }
      font.draw(batch, text, renderX + barRegion.getRegionWidth() / 2 - font.getSpaceWidth() * text.length()/2, renderY + barRegion.getRegionHeight()/2 + font.getLineHeight()/2);
    }

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

  public void showCost(int value) {
    costValue = value;
    costValueDisplay = value;
    if (this.value - value < 0) {
      costValue = this.value;
    }
  }

  public void setFixedToCamera(boolean fixedToCamera) {
    this.fixedToCamera = fixedToCamera;
  }

  public void setShowValues(boolean showValues) {
    this.showValues = showValues;
  }
}
