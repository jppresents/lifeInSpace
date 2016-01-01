package net.jppresents.space;

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

  private Guy guy;
  private List<Enemy> enemies = new ArrayList<Enemy>(20);

  public GameLogic(World world, List<AnimatedGameObject> gameObjects, SpriterDataManager spriterDataManager, UserInterface ui, Combat combat) {
    this.ui = ui;
    this.world = world;
    this.gameObjects = gameObjects;
    this.spriterDataManager = spriterDataManager;
    this.combat = combat;

    guy = new Guy(spriterDataManager.getEntity("guy"), spriterDataManager.getDrawer("guy"), SpaceMain.tileSize);
    gameObjects.add(guy);
    reset();
  }

  public void update(int tick) {
    for (Enemy enemy : enemies) {
      enemy.updateEnemy(guy, tick);
    }

    if (!guy.inCombat()) {
      //aggro
      for (Enemy enemy : enemies) {
        if (enemy.getHealth() > 0 && enemy.isAggro()) {
          guy.cancelMove(false);
          guy.setCombat(true);
          break;
        }
      }
    } else {
      boolean anyAggro = false;
      for (Enemy enemy : enemies) {
        if (enemy.getHealth() > 0 && enemy.isAggro()) {
          anyAggro = true;
          break;
        }
      }
      if (!anyAggro) {
        guy.setCombat(false);
      }
    }

    if (state == State.PLAYERMOVING) {
      if (guy.isIdle(tick)) {
        state = State.PLAYERINPUT;
      }
    }

    if (state == State.COMBAT) {
      if (!combat.isActive()) {
        state = State.PLAYERINPUT;
      }
    }

    if (state == State.PLAYERINPUT && guy.getActionPoints() <= 0) {
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
          activeEnemy.planTurn(world, guy, enemies);
          nextActiveEnemyIndex++;
        }
      } else {
        if (activeEnemy.isIdle(tick)) {
          activeEnemy = null; //next enemies turn
        }
      }
    }

    combat.update(world, enemies);
    ui.setActionPoints(guy.getActionPoints());
    ui.setMaxActionPoints(guy.getMaxActionPoints());
    ui.setHealthPoints(guy.getHealth());
    ui.setMaxHealthPoints(guy.getMaxHealth());
    ui.showActionBar(guy.inCombat());
  }

  public void reset() {
    gameObjects.clear();
    gameObjects.add(guy);
    world.applyPlayerPosition(guy, "Start");
    world.loadEnemies(enemies, spriterDataManager);
    gameObjects.addAll(enemies);
  }


  public void controlCamera(OrthographicCamera camera) {
    guy.centerCamera(camera);
    world.restrictCamera(camera);
  }

  public void touchDown(float x, float y) {
    mouseMoved(x, y);
    if (state == State.PLAYERINPUT) {

      if (guy.getHealth() <= 0) {
        return;
      }

      if (guy.getTilePosition().x == target.x && guy.getTilePosition().y == target.y) {
        guy.decActionPoints(guy.getActionPoints());
        ui.hideSelector();
        return;
      }

      AnimatedGameObject enemy = getActiveEnemy((int) target.x, (int) target.y);

      if (enemy != null) {
        guy.decActionPoints(1);
        combat.shoot(guy.getTilePosition(), enemy.getTilePosition(), guy.getDamage());
        state = State.COMBAT;
        ui.hideSelector();
        guy.activateShootAnimation(enemy.getX(), enemy.getY());
      } else if (!world.isTileBlocking((int) target.x, (int) target.y)) {
        world.calcPath(guy, guy.getTilePosition(), target, 0, enemies);
        state = State.PLAYERMOVING;
        ui.hideSelector();
      }
    } else if (state == State.PLAYERMOVING) {
      guy.cancelMove(false);
    }
  }

  public void mouseMoved(float x, float y) {
    if (state == State.PLAYERINPUT) {
      world.getTileCoords(x, y, temp);
      target.x = (int) temp.x;
      target.y = (int) temp.y;
      ui.setSelectorPos((int) temp.x * SpaceMain.tileSize + SpaceMain.tileSize / 2, (int) temp.y * SpaceMain.tileSize + SpaceMain.tileSize / 2);
      ui.setError(world.isTileBlocking((int) target.x, (int) target.y));
      ui.setTarget(getActiveEnemy((int) target.x, (int) target.y) != null);
      ui.setSkip(guy.inCombat() && guy.getTilePosition().x == target.x && guy.getTilePosition().y == target.y);
    }
  }

  private AnimatedGameObject getActiveEnemy(int x, int y) {
    for (AnimatedGameObject obj : enemies) {
      Vector3 pos = obj.getTilePosition();
      if (pos.x == x && pos.y == y && obj.getHealth() > 0) {
        return obj;
      }
    }
    return null;
  }
}
