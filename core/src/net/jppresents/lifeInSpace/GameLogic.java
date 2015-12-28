package net.jppresents.lifeInSpace;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {


  private final Combat combat;

  private enum State {PLAYERINPUT, PLAYERMOVING, ENEMYMOVING, COMBAT}

  private final UserInterface ui;
  private Lights lights;
  private World world;
  private List<AnimatedGameObject> gameObjects;
  private SpriterDataManager spriterDataManager;

  private Vector3 temp = new Vector3();
  private Vector3 target = new Vector3(-1, -1, 0);

  private State state = State.PLAYERINPUT;


  private Guy guy;
  private List<Enemy> enemies = new ArrayList<Enemy>(20);

  public GameLogic(Lights lights, World world, List<AnimatedGameObject> gameObjects, SpriterDataManager spriterDataManager, UserInterface ui, Combat combat) {
    this.ui = ui;
    this.lights = lights;
    this.world = world;
    this.gameObjects = gameObjects;
    this.spriterDataManager = spriterDataManager;
    this.combat = combat;

    guy = new Guy(spriterDataManager.getEntity("guy"), spriterDataManager.getDrawer("guy"), world.getTileSize());
    Light light = new Light(0, 0, 0, 40, 512, lights);
    light.setColor(0.8f, 0.6f, 0.6f, 1);
    guy.attachLight(light);
    gameObjects.add(guy);

    for (int i = 0; i < world.getCount("Monster", "1"); i++) {
      Enemy enemy = new Enemy(spriterDataManager.getEntity("alien"), spriterDataManager.getDrawer("alien"), world.getTileSize());
      gameObjects.add(enemy);
      enemies.add(enemy);
      light = new Light(0, 0, 0, 40, 300, lights);
      light.setColor(0.2f, 0.5f, 0.5f, 1);
      enemy.attachLight(light);
    }
    reset();
  }

  public void update() {
    if (state == State.PLAYERMOVING) {
      if (guy.isIdle()) {
        state = State.PLAYERINPUT;
      }
    }

    if (state == State.COMBAT) {
      if (!combat.isActive()) {
        state = State.PLAYERINPUT;
      }
    }

    combat.update(world, enemies);
  }

  public void reset() {
    world.resetPosition(guy, "Start");
    world.resetPositions(enemies, "Monster", "1");
  }


  public void controlCamera(OrthographicCamera camera) {
    guy.centerCamera(camera);
    world.restrictCamera(camera);
  }

  public void touchDown(float x, float y) {
    mouseMoved(x, y);
    if (state == State.PLAYERINPUT) {
      AnimatedGameObject enemy = getActiveEnemy((int) target.x, (int) target.y);

      if (enemy != null) {
        combat.shoot(guy.getGunX(), guy.getGunY(), enemy.getX(), enemy.getY() + world.getTileSize()/2);
        state = State.COMBAT;
        ui.hideSelector();
        guy.activateShootAnimation(enemy.getX(), enemy.getY());
      } else if (!world.isTileBlocking((int) target.x, (int) target.y)) {
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
    for (AnimatedGameObject obj: enemies) {
      Vector3 pos = obj.getTilePosition();
      if (pos.x == x && pos.y == y && obj.getHealth() > 0) {
        return obj;
      }
    }
    return null;
  }
}
