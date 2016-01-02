package net.jppresents.space;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SpaceMain extends ApplicationAdapter {
  public static int tileSize = 0;
  private World world;

  private SpriterDataManager spriterDataManager;

  private SpriteBatch batch;
  private Viewport viewport;
  private OrthographicCamera camera;

  public static Lights lights;

  public static Assets assets;

  private List<GameObject> gameObjects = new ArrayList<GameObject>(10);

  private GameLogic gameLogic;
  private UserInterface ui;

  private Combat combat;

  private boolean soundOn = true;

  public SpaceMain() {
  }

  public SpaceMain(boolean noSound) {
    soundOn = !noSound;
  }


  @Override
  public void create() {
    camera = new OrthographicCamera();

    viewport = new ExtendViewport(1280, 720, camera);
    camera.translate(1280 / 2, 720 / 2);
    batch = new SpriteBatch();

    lights = new Lights();
    lights.setDefaultAmbientColor(0.5f, 0.3f, 0.3f, 1);

    world = new World();
    tileSize = world.getTileSize();

    assets = new Assets(soundOn);

    spriterDataManager = new SpriterDataManager(batch);
    spriterDataManager.load("guy");
    spriterDataManager.load("alien");

    ui = new UserInterface();
    combat = new Combat();

    gameLogic = new GameLogic(world, gameObjects, spriterDataManager, ui, combat);

    new Input(true, camera, gameLogic);
    assets.startMusic();
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, false);
    camera.update();
    lights.resize(width, height);
    ui.resize(camera);
  }


  private static class YSortComparator implements Comparator<GameObject> {
    @Override
    public int compare(GameObject o1, GameObject o2) {
      if (o2.getY() == o1.getY()) {
        return o1.getHealth() - o2.getHealth();
      }
      return Math.round(o2.getY() - o1.getY());
    }
  }

  private static YSortComparator ySortComparator = new YSortComparator();


  int tick = 0;

  @Override
  public void render() {
    tick++;

    //update gameObjects
    for (GameObject obj : gameObjects) {
      obj.update();
    }
    Collections.sort(gameObjects, ySortComparator);

    gameLogic.update(tick);
    gameLogic.controlCamera(camera);

    camera.update();

    //render the world
    world.render(camera);

    //render the gameObjects
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    for (GameObject obj : gameObjects) {
      obj.render();
    }
    combat.render(batch);
    batch.end();

    //render the lights on top
    lights.render(camera);

    //ui on top (no lighting)
    batch.begin();
    ui.render(batch, camera);
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
