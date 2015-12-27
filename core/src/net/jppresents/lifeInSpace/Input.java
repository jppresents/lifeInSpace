package net.jppresents.lifeInSpace;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

public class Input implements InputProcessor {
  private final GameLogic gameLogic;
  private final OrthographicCamera camera;
  private Vector3 touchPoint = new Vector3(-1, -1, 0);

  public Input(boolean registerAsGdxInput, OrthographicCamera camera, GameLogic gameLogic) {
    if (registerAsGdxInput) {
      Gdx.input.setInputProcessor(this);
    }
    this.camera = camera;
    this.gameLogic = gameLogic;
  }

  @Override
  public boolean scrolled(int amount) {
    return false;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    camera.unproject(touchPoint.set(screenX, screenY, 0));
    gameLogic.touchDown(touchPoint.x, touchPoint.y);
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
