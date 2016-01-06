package net.jppresents.space;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
  public static boolean touchMode = false;
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
  public static Color insideColor;
  public static Color outsideColor;

  public SpaceMain() {
  }

  public SpaceMain(boolean touchMode, boolean noSound) {
    SpaceMain.touchMode = touchMode;
    soundOn = !noSound;
  }


  @Override
  public void create() {
    camera = new OrthographicCamera();

    viewport = new ExtendViewport(1280, 720, camera);
    camera.translate(1280 / 2, 720 / 2);
    batch = new SpriteBatch();

    lights = new Lights();
    insideColor = new Color(0.4f, 0.4f, 0.4f, 1);
    outsideColor = new Color(0.5f, 0.3f, 0.3f, 1);
    lights.setDefaultAmbientColor(insideColor);

    world = new World();
    tileSize = world.getTileSize();

    assets = new Assets(soundOn);

    spriterDataManager = new SpriterDataManager(batch);
    spriterDataManager.load("guy");
    spriterDataManager.load("alien");
    spriterDataManager.load("effects");

    ui = new UserInterface();
    combat = new Combat();

    gameLogic = new GameLogic(world, gameObjects, spriterDataManager, ui, combat);

    new InputHandler(true, camera, gameLogic, touchMode);
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
      if (o1.getSecondarySortAttrib() == -1 && o2.getSecondarySortAttrib() != -1) {
        return -1;
      }
      if (o2.getSecondarySortAttrib() == -1 && o1.getSecondarySortAttrib() != -1) {
        return 1;
      }
      if (o2.getY() == o1.getY()) {
        return o1.getSecondarySortAttrib() - o2.getSecondarySortAttrib();
      }
      return Math.round(o2.getY() - o1.getY());
    }
  }

  private static YSortComparator ySortComparator = new YSortComparator();


  int tick = 0;

  //FPSLogger fps = new FPSLogger();

  @Override
  public void render() {
    //fps.log();

    tick++;

    //update gameObjects
    for (GameObject obj : gameObjects) {
      obj.update(tick);
    }
    Collections.sort(gameObjects, ySortComparator);

    gameLogic.update(tick);
    gameLogic.controlCamera(camera, tick);

    camera.update();

    //render the world
    world.render(camera);

    //render the gameObjects
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    for (GameObject obj : gameObjects) {
      obj.render(batch);
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
