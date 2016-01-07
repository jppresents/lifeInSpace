package net.jppresents.space;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class InputHandler implements InputProcessor {
  private final GameLogic gameLogic;
  private final OrthographicCamera camera;
  private Vector3 touchPoint = new Vector3(-1, -1, 0);
  private boolean touchMode = true;
  private Vector2 lastTileTouch = new Vector2(-1, -1);
  private boolean touchModeWasUp = false;
  private int touchModeMoveCount = 0;

  public InputHandler(OrthographicCamera camera, GameLogic gameLogic, boolean touchMode) {
    Gdx.input.setInputProcessor(this);
    this.camera = camera;
    this.gameLogic = gameLogic;
    this.touchMode = touchMode;
  }

  @Override
  public boolean scrolled(int amount) {
    return false;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    if (!touchMode) {
      camera.unproject(touchPoint.set(screenX, screenY, 0));
      gameLogic.setAndDisplayAction(touchPoint.x, touchPoint.y);
    }
    return true;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    gameLogic.cameraDragged(screenX, screenY);

    // If you drag on the phone, you don't want the action to execute. - but if you touch a little too long, that launches an accidental touchDragged event
    // (if you move your finger a little)
    // right now if you drag for less than 1/3 of a second it's handled as accidental and the action is still performed
    // this could/should be changed to a check of how many pixels the drag distance is
    touchModeMoveCount++;
    if (touchModeMoveCount > 20) {
      touchModeWasUp = false;
    }

    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (touchMode) {
      if (!touchModeWasUp) {
        touchModeWasUp = true;
        return true;
      }
      int x = MathUtils.floor(touchPoint.x/SpaceMain.tileSize);
      int y = MathUtils.floor(touchPoint.y/SpaceMain.tileSize);

      if (lastTileTouch.x == x && lastTileTouch.y == y) {
        gameLogic.executeAction();
        lastTileTouch.x = -1;
        lastTileTouch.y = -1;
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    camera.unproject(touchPoint.set(screenX, screenY, 0));

    if (touchMode) {
      touchModeMoveCount = 0;
      int x = MathUtils.floor(touchPoint.x/SpaceMain.tileSize);
      int y = MathUtils.floor(touchPoint.y/SpaceMain.tileSize);

      gameLogic.setAndDisplayAction(touchPoint.x, touchPoint.y);
      gameLogic.startCameraDrag(screenX, screenY);

      if (lastTileTouch.x != x || lastTileTouch.y != y) {
        touchModeWasUp = false;
        lastTileTouch.x = x;
        lastTileTouch.y = y;
      }

    } else {
      if (button == com.badlogic.gdx.Input.Buttons.LEFT) {
        gameLogic.setAndDisplayAction(touchPoint.x, touchPoint.y);
        gameLogic.executeAction();
      }

      if (button == com.badlogic.gdx.Input.Buttons.RIGHT) {
        gameLogic.startCameraDrag(screenX, screenY);
      }
    }
    return true;
  }

  @Override
  public boolean keyTyped(char character) {
    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    return false;
  }

  @Override
  public boolean keyDown(int keycode) {
    return false;
  }
}
