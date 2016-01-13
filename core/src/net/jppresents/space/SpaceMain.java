package net.jppresents.space;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SpaceMain extends ApplicationAdapter {
  public static int MOVE_SPEED = 5;
  public static final int TILE_SIZE = 64;
  public static boolean touchMode = false;
  public static boolean returnToMenu;
  private World world;

  private SpriterDataManager spriterDataManager;

  private SpriteBatch batch;
  public static Viewport viewport;
  private OrthographicCamera camera;

  public static Lights lights;

  public static Assets assets;

  private List<GameObject> gameObjects = new ArrayList<GameObject>(10);

  private GameLogic gameLogic;
  private UserInterface ui;

  private Combat combat;

  public static Color insideColor;
  public static Color outsideColor;

  public static MainMenu mainMenu;
  private InputHandler input;
  public static Viewport stageViewPort;

  public static Preferences prefs;

  class Pref {
    final static String SOUND = "soundOn";
    final static String MUSIC = "musicOn";
    final static String WIN = "win";
    final static String BEAT_UP_TO = "beat_level_up_to_"; // level name without .tmx is concatenated
  }

  public SpaceMain() {
  }

  public SpaceMain(boolean touchMode, boolean fastMode) {
    SpaceMain.touchMode = touchMode;
    if (fastMode) {
      MOVE_SPEED = 25;
    }
  }

  @Override
  public void create() {
    prefs = Gdx.app.getPreferences("life-in-space-and-how-to-get-rid-of-it");
    camera = new OrthographicCamera();

    viewport = new ExtendViewport(1280, 720, camera);
    stageViewPort = new ExtendViewport(1280, 720, new OrthographicCamera());
    batch = new SpriteBatch();

    lights = new Lights();
    insideColor = new Color(0.4f, 0.4f, 0.4f, 1);
    outsideColor = new Color(0.5f, 0.3f, 0.3f, 1);
    lights.setDefaultAmbientColor(insideColor);

    world = new World();

    assets = new Assets();

    spriterDataManager = new SpriterDataManager(batch);
    spriterDataManager.load("guy");
    spriterDataManager.load("alien");
    spriterDataManager.load("effects");

    ui = new UserInterface();
    combat = new Combat();

    gameLogic = new GameLogic(world, gameObjects, spriterDataManager, ui, combat);

    input = new InputHandler(camera, gameLogic, touchMode);
    assets.startMusic(Assets.GameMusic.MENU);



    mainMenu = new MainMenu();

    Gdx.input.setCatchBackKey(true); //android
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, false);
    stageViewPort.update(width, height, false);
    camera.update();
    lights.resize(width, height);
    ui.resize(camera);
  }


  private static class YSortComparator implements Comparator<GameObject> {
    @Override
    public int compare(GameObject o1, GameObject o2) {
      if (o1.getSecondarySortAttrib() == 0 && o2.getSecondarySortAttrib() != 0) {
        return -1;
      }
      if (o2.getSecondarySortAttrib() == 0 && o1.getSecondarySortAttrib() != 0) {
        return 1;
      }
      return Math.round(o2.getY() - o1.getY());
    }
  }

  private static YSortComparator ySortComparator = new YSortComparator();


  int tick = 0;

  //FPSLogger fps = new FPSLogger();

  public void startGame(String nextWorld) {
    world.changeLevel(nextWorld);
    gameLogic.reset();
  }

  private boolean mainMenuWasActive;

  @Override
  public void render() {
    assets.update();

    if (Gdx.input.isKeyPressed(Input.Keys.BACK) ||Gdx.input.isKeyPressed(Input.Keys.ESCAPE)  || returnToMenu){
     if (!mainMenu.isActive()) {
       returnToMenu = false;
       mainMenu.setCanResume(gameLogic.canResume());
       mainMenu.setActive(true);
     }
    }

    if (mainMenu.isActive()) {
      mainMenuWasActive = true;
      mainMenu.render();
      Gdx.input.setInputProcessor(mainMenu.getStage());
      return;
    };

    if (mainMenuWasActive) {
      Gdx.input.setInputProcessor(input);
      mainMenuWasActive = false;
      if (mainMenu.isNewGame()) {
        startGame(mainMenu.getNextWorld());
      }
    }

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
    mainMenu.dispose();
  }


}
