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
  private boolean actionOnNextUp = false;
  private int lastTouchDownX, lastTouchDownY;

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
    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (touchMode) {

      double dragRange = Math.sqrt(Math.pow(screenX - lastTouchDownX, 2) + Math.pow(screenY - lastTouchDownY, 2));

      if (dragRange  < 100) {
        //if the drag distance was less then 50 it must we cancel movement
        gameLogic.touchUpNoScroll();
      }

      if (!actionOnNextUp) {
        actionOnNextUp = true;
        return true;
      }
      int x = MathUtils.floor(touchPoint.x/SpaceMain.TILE_SIZE);
      int y = MathUtils.floor(touchPoint.y/SpaceMain.TILE_SIZE);

      //at the last down the tile was an exact match (otherwhise actionOnNextUp would be false)
      //so now we accept even if you moved your finger one tile off - but if it has been dragged more then 100, it is not an execute
      if (dragRange < 100 && Math.abs(lastTileTouch.x - x) <= 2 && Math.abs(lastTileTouch.y - y) <= 2) {
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
      lastTouchDownX = screenX;
      lastTouchDownY = screenY;

      int x = MathUtils.floor(touchPoint.x/SpaceMain.TILE_SIZE);
      int y = MathUtils.floor(touchPoint.y/SpaceMain.TILE_SIZE);

      gameLogic.setAndDisplayAction(touchPoint.x, touchPoint.y);
      gameLogic.startCameraDrag(screenX, screenY);

      if (lastTileTouch.x != x || lastTileTouch.y != y) {
        actionOnNextUp = false; //new tile selected
        lastTileTouch.x = x;
        lastTileTouch.y = y;
      }

    } else {
      if (button == com.badlogic.gdx.Input.Buttons.LEFT) {
        gameLogic.setAndDisplayAction(touchPoint.x, touchPoint.y);
        gameLogic.executeAction();
      } else {
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
