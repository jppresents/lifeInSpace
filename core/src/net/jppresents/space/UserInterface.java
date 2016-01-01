package net.jppresents.space;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class UserInterface {
  private final TextureAtlas.AtlasRegion marker;
  private final TextureAtlas.AtlasRegion markerError;
  private final TextureAtlas.AtlasRegion selector;
  private final TextureAtlas.AtlasRegion markerTarget;
  private final TextureAtlas.AtlasRegion markerSkip;
  private final int barHeight, barWidth;

  private boolean error = false;
  private boolean target = false;
  private boolean skip = false;

  private int selectorX, selectorY;

  private ProgressBar healthBar;
  private ProgressBar actionBar;
  private int actionBarY = 0;
  private int currentActionBarY = 0;

  public UserInterface() {
    marker = SpaceMain.assets.getSprites().findRegion("marker");
    markerError = SpaceMain.assets.getSprites().findRegion("markerError");
    markerTarget = SpaceMain.assets.getSprites().findRegion("markerTarget");
    selector = SpaceMain.assets.getSprites().findRegion("selector");
    markerSkip = SpaceMain.assets.getSprites().findRegion("markerSkip");
    selectorX = -1;
    selectorY = -1;
    barHeight = SpaceMain.assets.getSprites().findRegion("apBar").getRegionHeight();
    barWidth =  SpaceMain.assets.getSprites().findRegion("apBar").getRegionWidth();
    actionBarY = -SpaceMain.assets.getSprites().findRegion("apBar").getRegionHeight();
    healthBar = new ProgressBar(0, 0, 100, 100, SpaceMain.assets.getSprites().findRegion("hpBar"), SpaceMain.assets.getSprites().findRegion("hpBarFill"));
    currentActionBarY = actionBarY;
    actionBar = new ProgressBar(0, 0, 3, 3, SpaceMain.assets.getSprites().findRegion("apBar"), SpaceMain.assets.getSprites().findRegion("apBarFill"));
  }

  public void render(SpriteBatch batch, Camera camera) {
    if (selectorX != -1 && selectorY != -1) {

      if (!target) {
        batch.setColor(0.5f, 0.8f, 0.9f, 1);
        batch.draw(selector, selectorX - selector.getRegionWidth() / 2, selectorY - selector.getRegionHeight() / 2);
      }

      if (skip) {
        batch.setColor(0.2f, 0, 0, 1);
        batch.draw(markerSkip, selectorX - markerSkip.getRegionWidth() / 2, selectorY - markerSkip.getRegionHeight() / 2);
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

  public void setSkip(boolean skip) {
    this.skip = skip;
  }

  public void setActionPoints(float value) {
    actionBar.setValue(value);
  }

  public void showActionBar(boolean show) {
    if (show) {
      actionBarY = 10;
    } else {
      actionBarY = -SpaceMain.assets.getSprites().findRegion("apBar").getRegionHeight();
    }
  }

  public void setMaxHealthPoints(int healthPoints) {
    this.healthBar.setMaxValue(healthPoints);
  }

  public void setMaxActionPoints(int healthPoints) {
    this.actionBar.setMaxValue(healthPoints);
  }

  public void setHealthPoints(int healthPoints) {
    this.healthBar.setValue(healthPoints);
  }

  public void resize(Camera camera) {
    healthBar.setX(10);
    healthBar.setY((int)camera.viewportHeight - barHeight);

    actionBarY = -barHeight;
    currentActionBarY = -barHeight;
    actionBar.setX((int)camera.viewportWidth - barWidth);
    actionBar.setY(currentActionBarY);
  }
}
