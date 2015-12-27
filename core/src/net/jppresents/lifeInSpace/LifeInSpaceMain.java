package net.jppresents.lifeInSpace;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LifeInSpaceMain extends ApplicationAdapter implements InputProcessor {
  private World world;

  private SpriterDataManager spriterDataManager;

  private SpriteBatch batch;
  private Viewport viewport;
  private OrthographicCamera camera;

  private Vector3 touchPoint = new Vector3();

  private Lights lights;
  private Light testLight;

  private AnimatedGameObject guy, alien;

  @Override
  public void dispose() {
    spriterDataManager.dispose();
    lights.dispose();
    world.dispose();
  }

  @Override
  public void create() {
    camera = new OrthographicCamera();

    viewport = new ExtendViewport(1280, 720, camera);
    camera.translate(1280/2, 720/2);
    batch = new SpriteBatch();

    lights = new Lights();
    lights.setAmbientColor(0.5f, 0.3f, 0.3f, 1);

    world = new World();

    testLight = new Light(100, 100, 600, lights);
    testLight.setColor(1, 1, 1, 1);

    spriterDataManager = new SpriterDataManager(batch);
    spriterDataManager.load("guy");
    spriterDataManager.load("alien");

    guy = new AnimatedGameObject(spriterDataManager, "guy");
    alien = new AnimatedGameObject(spriterDataManager, "guy");
    alien.setPosition(500, 500);

    Gdx.input.setInputProcessor(this);
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, false);
    camera.update();
    lights.resize(width, height);
  }

  @Override
  public void render() {
    //update animations
    guy.update();
    alien.update();

    testLight.setPosition(guy.getX(), guy.getY());

    //Center Cam on player
    guy.centerCamera(camera);

    world.restrictCamera(camera);
    camera.update();

    //render the world
    world.render(camera);

    //render the objects
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    guy.draw();
    alien.draw();
    batch.end();

    //render the lights on top
    lights.render(camera);
  }


  @Override
  public boolean keyDown(int keycode) {
    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    return false;
  }

  @Override
  public boolean keyTyped(char character) {
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    camera.unproject(touchPoint.set(screenX, screenY, 0));
    if (!world.isBlocking(touchPoint.x, touchPoint.y)) {
      guy.setTarget(touchPoint.x, touchPoint.y);
    }
    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return false;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    return false;
  }

  @Override
  public boolean scrolled(int amount) {
    return false;
  }
}
