package net.jppresents.space;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class UserInterface {
  private final TextureAtlas.AtlasRegion marker;
  private final TextureAtlas.AtlasRegion markerError;
  private final TextureAtlas.AtlasRegion selector;
  private final TextureAtlas.AtlasRegion markerTarget;

  private boolean error = false;
  private boolean target = false;

  private int selectorX, selectorY;

  private ProgressBar healthBar;
  private ProgressBar actionBar;
  private int actionBarY = 0;
  private int currentActionBarY = 0;

  public UserInterface() {
    marker = brokenMain.assets.getSprites().findRegion("marker");
    markerError = brokenMain.assets.getSprites().findRegion("markerError");
    markerTarget = brokenMain.assets.getSprites().findRegion("markerTarget");
    selector = brokenMain.assets.getSprites().findRegion("selector");
    selectorX = -1;
    selectorY = -1;
    healthBar = new ProgressBar(10, 670, 100, 100, brokenMain.assets.getSprites().findRegion("hpBar"), brokenMain.assets.getSprites().findRegion("hpBarFill"));
    actionBarY = -brokenMain.assets.getSprites().findRegion("apBar").getRegionHeight();
    currentActionBarY = actionBarY;
    actionBar = new ProgressBar(850, currentActionBarY, 3, 3, brokenMain.assets.getSprites().findRegion("apBar"), brokenMain.assets.getSprites().findRegion("apBarFill"));
  }

  public void render(SpriteBatch batch, Camera camera) {
    if (selectorX != -1 && selectorY != -1) {

      if (!target) {
        batch.setColor(0.5f, 0.8f, 0.9f, 1);
        batch.draw(selector, selectorX - selector.getRegionWidth() / 2, selectorY - selector.getRegionHeight() / 2);
      }

      if (error) {
        batch.setColor(1, 0, 0, 1);
        batch.draw(markerError, selectorX - markerError.getRegionWidth() / 2, selectorY - markerError.getRegionHeight() / 2);
      }

      if (target) {
        batch.setColor(1, 0, 0, 1);
        batch.draw(markerTarget, selectorX - markerTarget.getRegionWidth() / 2, selectorY - markerTarget.getRegionHeight() / 2);
      }
    }
    batch.setColor(1, 1, 1, 1);
    healthBar.render(batch, camera);
    if (currentActionBarY < actionBarY) {
      currentActionBarY += 2;
    }
    if (currentActionBarY > actionBarY) {
      currentActionBarY -= 2;
    }
    if (Math.abs(currentActionBarY - actionBarY) < 2) {
      currentActionBarY = actionBarY;
    }
    actionBar.setY(currentActionBarY);
    actionBar.render(batch, camera);
  }

  public void setSelectorPos(int x, int y) {
    selectorX = x;
    selectorY = y;
  }

  public void hideSelector() {
    selectorX = -1;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public void setTarget(boolean target) {
    this.target = target;
  }

  public void setActionPoints(float value) {
    actionBar.setValue(value);
  }

  public void showActionBar(boolean show) {
    if (show) {
      actionBarY = 10;
    } else {
      actionBarY = -brokenMain.assets.getSprites().findRegion("apBar").getRegionHeight();
    }
  }
}
