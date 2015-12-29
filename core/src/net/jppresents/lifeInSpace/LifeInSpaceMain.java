package net.jppresents.lifeInSpace;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

public class LifeInSpaceMain extends ApplicationAdapter {
  private World world;

  private SpriterDataManager spriterDataManager;

  private SpriteBatch batch;
  private Viewport viewport;
  private OrthographicCamera camera;

  public static Lights lights;

  public static Assets assets;

  private List<AnimatedGameObject> gameObjects = new ArrayList<AnimatedGameObject>(10);

  private Input input;
  private GameLogic gameLogic;
  private UserInterface ui;

  private Combat combat;

  @Override
  public void create() {
    camera = new OrthographicCamera();

    viewport = new ExtendViewport(1280, 720, camera);
    camera.translate(1280/2, 720/2);
    batch = new SpriteBatch();

    lights = new Lights();
    lights.setAmbientColor(0.5f, 0.3f, 0.3f, 1);

    world = new World();

    assets = new Assets();

    spriterDataManager = new SpriterDataManager(batch);
    spriterDataManager.load("guy");
    spriterDataManager.load("alien");

    ui = new UserInterface();
    combat = new Combat();

    gameLogic = new GameLogic(world, gameObjects, spriterDataManager, ui, combat);

    input = new Input(true, camera, gameLogic);
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, false);
    camera.update();
    lights.resize(width, height);
  }


  @Override
  public void render() {

    //update gameObjects
    for (AnimatedGameObject obj: gameObjects) {
      obj.update();
    }
    gameObjects.sort(AnimatedGameObject.getYSortComparator());

    gameLogic.update();
    gameLogic.controlCamera(camera);

    camera.update();

    //render the world
    world.render(camera);

    //render the gameObjects
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    for (AnimatedGameObject obj: gameObjects) {
      obj.draw();
    }
    combat.render(batch);
    batch.end();

    //render the lights on top
    lights.render(camera);

    //ui on top (no lighting)
    batch.begin();
    ui.render(batch);
    batch.end();
  }

  @Override
  public void dispose() {
    spriterDataManager.dispose();
    lights.dispose();
    world.dispose();
    assets.dispose();
  }


}
