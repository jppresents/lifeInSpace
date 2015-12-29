package net.jppresents.lifeInSpace;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {


  private final Combat combat;

  private enum State {PLAYERINPUT, PLAYERMOVING, ENEMYTURN, COMBAT}

  private final UserInterface ui;
  private World world;
  private List<AnimatedGameObject> gameObjects;
  private SpriterDataManager spriterDataManager;

  private Vector3 temp = new Vector3();
  private Vector3 target = new Vector3(-1, -1, 0);

  private State state = State.PLAYERINPUT;
  private int nextActiveEnemyIndex;
  private Enemy activeEnemy;
  private boolean playerInCombat = false;


  private Guy guy;
  private List<Enemy> enemies = new ArrayList<Enemy>(20);

  public GameLogic(World world, List<AnimatedGameObject> gameObjects, SpriterDataManager spriterDataManager, UserInterface ui, Combat combat) {
    this.ui = ui;
    this.world = world;
    this.gameObjects = gameObjects;
    this.spriterDataManager = spriterDataManager;
    this.combat = combat;

    guy = new Guy(spriterDataManager.getEntity("guy"), spriterDataManager.getDrawer("guy"), LifeInSpaceMain.tileSize);
    gameObjects.add(guy);

    for (int i = 0; i < world.getCount("Monster", "1"); i++) {
      Enemy enemy = new Enemy(spriterDataManager.getEntity("alien"), spriterDataManager.getDrawer("alien"), LifeInSpaceMain.tileSize);
      gameObjects.add(enemy);
      enemies.add(enemy);
    }
    reset();
  }

  public void update() {
    for (Enemy enemy: enemies) {
      enemy.updateAggro(guy);
    }

    if (!playerInCombat) {
      guy.resetActionPoints();
      //aggro
      for (Enemy enemy: enemies) {
        if (enemy.getHealth() > 0 && enemy.isAggro()) {
          guy.cancelMove();
          playerInCombat = true;
          break;
        }
      }
    } else {
      boolean anyAggro = false;
      for (Enemy enemy: enemies) {
        if (enemy.getHealth() > 0 && enemy.isAggro()) {
          anyAggro = true;
          break;
        }
      }
      if (!anyAggro) {
        playerInCombat = false;
      }
    }

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

    if (state == State.PLAYERINPUT && guy.getActionPoints() == 0) {
      state = State.ENEMYTURN;
      nextActiveEnemyIndex = 0;
    }

    if (state == State.ENEMYTURN) {
      if (activeEnemy == null) {
        if (nextActiveEnemyIndex >= enemies.size()) {
          state = State.PLAYERINPUT;
          guy.resetActionPoints();
        } else {
          activeEnemy = enemies.get(nextActiveEnemyIndex);
          activeEnemy.resetActionPoints();
          activeEnemy.planTurn(world, guy);
          nextActiveEnemyIndex++;
        }
      } else {
        if (activeEnemy.isIdle()) {
          activeEnemy = null; //next enemies turn
        }
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
        guy.decActionPoints(1);
        combat.shoot(guy.getTilePosition(), enemy.getTilePosition());
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
      ui.setSelectorPos((int) temp.x * LifeInSpaceMain.tileSize + LifeInSpaceMain.tileSize/2, (int) temp.y * LifeInSpaceMain.tileSize + LifeInSpaceMain.tileSize/2);
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
