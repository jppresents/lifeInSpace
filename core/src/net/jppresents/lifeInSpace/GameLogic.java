package net.jppresents.lifeInSpace;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {


  private enum State {PLAYERINPUT, PLAYERMOVING, ENEMYMOVING}

  private final UserInterface ui;
  private Lights lights;
  private World world;
  private List<AnimatedGameObject> gameObjects;
  private SpriterDataManager spriterDataManager;

  private Vector3 temp = new Vector3();
  private Vector3 target = new Vector3(-1, -1, 0);

  private State state = State.PLAYERINPUT;


  private Guy guy;
  private List<AnimatedGameObject> monsters = new ArrayList<AnimatedGameObject>(20);

  public GameLogic(Lights lights, World world, List<AnimatedGameObject> gameObjects, SpriterDataManager spriterDataManager, UserInterface ui) {
    this.ui = ui;
    this.lights = lights;
    this.world = world;
    this.gameObjects = gameObjects;
    this.spriterDataManager = spriterDataManager;

    guy = new Guy(spriterDataManager.getEntity("guy"), spriterDataManager.getDrawer("guy"), world.getTileSize());
    Light light = new Light(0, 0, 0, 40, 512, lights);
    light.setColor(0.8f, 0.6f, 0.6f, 1);
    guy.attachLight(light);
    gameObjects.add(guy);

    for (int i = 0; i < world.getCount("Monster", "1"); i++) {
      AnimatedGameObject monster = new AnimatedGameObject(spriterDataManager.getEntity("alien"), spriterDataManager.getDrawer("alien"), world.getTileSize());
      gameObjects.add(monster);
      monsters.add(monster);
      light = new Light(0, 0, 0, 40, 300, lights);
      light.setColor(0.1f, 0.3f, 0.3f, 1);
      monster.attachLight(light);
    }
    reset();
  }

  public void update() {
    if (state == State.PLAYERMOVING) {
      if (guy.isIdle()) {
        state = State.PLAYERINPUT;
      }
    }
  }

  public void reset() {
    world.resetPosition(guy, "Start");
    world.resetPositions(monsters, "Monster", "1");
  }


  public void controlCamera(OrthographicCamera camera) {
    guy.centerCamera(camera);
    world.restrictCamera(camera);
  }

  public void touchDown(float x, float y) {
    if (state == State.PLAYERINPUT) {
      if (!world.isTileBlocking((int) target.x, (int) target.y)) {
        world.calcPath(guy, guy.getTilePosition(), target);
        state = State.PLAYERMOVING;
        ui.hideSelector();
      }
    } else if (state == State.PLAYERMOVING){
      guy.cancelMove();
    }
  }

  public void mouseMoved(float x, float y) {
    if (state == State.PLAYERINPUT) {
      world.getTileCoords(x, y, temp);
      target.x = (int) temp.x;
      target.y = (int) temp.y;
      ui.setSelectorPos((int) temp.x * world.getTileSize() + world.getTileSize()/2, (int) temp.y * world.getTileSize() + world.getTileSize()/2);
      ui.setError(world.isTileBlocking((int)target.x, (int)target.y));
      ui.setTarget(getActiveEnemy((int) target.x, (int) target.y) != null);
    }
  }

  private AnimatedGameObject getActiveEnemy(int x, int y) {
    for (AnimatedGameObject obj: monsters) {
      Vector3 pos = obj.getTilePosition();
      if (pos.x == x && pos.y == y) {
        return obj;
      }
    }
    return null;
  }
}
