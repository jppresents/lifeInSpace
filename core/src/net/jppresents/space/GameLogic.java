package net.jppresents.space;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {
  private final Combat combat;

  private Vector2 lastMouse = new Vector2(0, 0);
  private Vector2 dragFrom = new Vector2(0, 0);
  private Vector2 moveCam = new Vector2(0, 0);
  private boolean resetCam;

  private enum State {PLAYERINPUT, PLAYERMOVING, ENEMYTURN, COMBAT}

  private final UserInterface ui;
  private World world;
  private List<GameObject> gameObjects;
  private SpriterDataManager spriterDataManager;

  private Vector3 temp = new Vector3();
  private Vector3 target = new Vector3(-1, -1, 0);

  private State state = State.PLAYERINPUT;
  private int nextActiveEnemyIndex;
  private Enemy activeEnemy;

  private Guy guy;
  private List<Enemy> enemies = new ArrayList<Enemy>(20);
  private List<Goody> goodies = new ArrayList<Goody>(20);


  private int gameOverTime = 0;
  private int lastTick = 0;

  public GameLogic(World world, List<GameObject> gameObjects, SpriterDataManager spriterDataManager, UserInterface ui, Combat combat) {
    this.ui = ui;
    this.world = world;
    this.gameObjects = gameObjects;
    this.spriterDataManager = spriterDataManager;
    this.combat = combat;

    guy = new Guy(spriterDataManager.getEntity("guy"), spriterDataManager.getDrawer("guy"), spriterDataManager.getEntity("effects"), spriterDataManager.getDrawer("effects"));
    gameObjects.add(guy);
    reset();
    //todo maybe read texts from file
    if (SpaceMain.touchMode) {
      this.ui.getTextBox().setText("\"life in space and how to get rid of it\"\n\nControls:\ndrag to move the camera\ntouch to see action, touch again to execute the action");
    } else {
      this.ui.getTextBox().setText("\"life in space and how to get rid of it\"\n\nControls:\nhold mouse right to move the camera\nclick mouse left to move and shoot");
    }
  }

  private float lastPosX, lastPosY;

  private void handleWorldInteraction() {
    if (guy.getHealth() > 0 && (guy.getTilePosition().x != lastPosX || guy.getTilePosition().y != lastPosY)) {
      lastPosX = guy.getTilePosition().x;
      lastPosY = guy.getTilePosition().y;

      //Goodie pickups
      Goody goody = findActiveGoody((int)lastPosX, (int)lastPosY);
      if (goody != null) {
        if ("weaponkit".equals(goody.getType())) {
          guy.cancelMove(false);
          goody.setActive(false);
          guy.pickupNewGun(guy.getGunLevel() + 1);
        }
        if ("medkit".equals(goody.getType()) && guy.getHealth() < guy.getMaxHealth()) {
          goody.setActive(false);
          guy.setHealth(guy.getHealth() + goody.getAmount());
          guy.showHealAnimation();
        }
        if ("hpkit".equals(goody.getType())) {
          goody.setActive(false);
          guy.setMaxHealth(guy.getMaxHealth() + goody.getAmount());
          guy.setHealth(guy.getHealth() + goody.getAmount());
          guy.showMaxHealthUpAnimation();
        }
        if ("apkit".equals(goody.getType())) {
          goody.setActive(false);
          guy.setMaxActionPoints(guy.getMaxActionPoints() + goody.getAmount());
          guy.showMaxApUpAnimation();
        }
      }

      //Light changes
      if (world.getTileIndex((int)lastPosX, (int)lastPosY) == 38) {
        SpaceMain.lights.fadeTo(SpaceMain.insideColor);
      } else if (world.getTileIndex((int)lastPosX, (int)lastPosY) == 0) {
        SpaceMain.lights.fadeTo(SpaceMain.outsideColor);
      }
    }
  }

  public void update(int tick) {

    handleWorldInteraction();

    lastTick = tick;
    if (gameOverTime == 0 && guy.getHealth() <= 0) {
      SpaceMain.lights.fadeTo(0.2f, 0, 0, 0, 0);
      gameOverTime = tick;
    }

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
        refreshUI();
      }
    }

    if (state == State.COMBAT) {
      if (!combat.isActive()) {
        state = State.PLAYERINPUT;
        refreshUI();
      }
    }

    if (state == State.PLAYERINPUT && guy.getActionPoints() <= 0) {
      state = State.ENEMYTURN;
      ui.hideSelector();
      nextActiveEnemyIndex = 0;
    }

    if (state == State.ENEMYTURN) {
      if (activeEnemy == null) {
        if (nextActiveEnemyIndex >= enemies.size()) {
          state = State.PLAYERINPUT;
          guy.resetActionPoints();
          refreshUI();
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
    for (GameObject obj : gameObjects) {
      if (obj != guy)
        obj.dispose();
    }
    gameObjects.clear();
    gameObjects.add(guy);
    guy.reset();
    world.applyPlayerPosition(guy, "Start");
    world.loadEnemies(enemies, spriterDataManager);
    gameObjects.addAll(enemies);
    world.loadGoodies(goodies);
    gameObjects.addAll(goodies);
    SpaceMain.lights.resetColor();
    gameOverTime = 0;
    resetCam = true;
  }


  public void controlCamera(OrthographicCamera camera, int tick) {
    if (resetCam) {
      guy.centerCamera(camera);
      resetCam = false;
    }
    if (ui.getTextBox().isActive()) {
      moveCam.set(0, 0);
      return;
    }
    camera.translate(moveCam.x, moveCam.y);
    moveCam.set(0, 0);

    guy.restrictCamera(camera);

    if (!guy.isIdle(tick)) {
      guy.moveCamera(camera);
    }
    world.restrictCamera(camera);
  }

  public void startCameraDrag(float x, float y) {
    dragFrom.set(x, y);
  }

  public void cameraDragged(float x, float y) {
    if (dragFrom.x != -1 || dragFrom.y != -1) {
      moveCam.add(dragFrom.x - x, -(dragFrom.y - y));
      dragFrom.set(x, y);
    }
  }

  public void executeAction() {
    if (ui.getTextBox().isActive()) {
      if (ui.getTextBox().isDone()) {
        ui.getTextBox().hide();
        dragFrom.set(-1, -1);
        moveCam.set(0, 0);
      }
      return;
    }

    dragFrom.set(-1, -1);
    if (state == State.PLAYERINPUT) {
      if (guy.getHealth() <= 0) {
        if (gameOverTime < lastTick - 120) {
          reset();
        }
        return;
      }

      if (guy.getTilePosition().x == target.x && guy.getTilePosition().y == target.y) {
        guy.decActionPoints(guy.getActionPoints());
        ui.hideSelector();
        return;
      }

      AnimatedGameObject enemy = findActiveEnemy((int) target.x, (int) target.y);

      if (enemy != null) {
        if (guy.getActionPoints() < guy.getShotCost()) {
          return;
        }
        guy.decActionPoints(guy.getShotCost());
        guy.setCurrentMovecostsActinPoints(true);
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

  public void setAndDisplayAction(float x, float y) {

    if (ui.getTextBox().isActive()) {
      if (SpaceMain.touchMode) {
        if (ui.getTextBox().isDone()) {
          ui.getTextBox().hide();
        }
      }
      return; //no actions until textbox is done
    }


    if (SpaceMain.touchMode) {
      if (gameOverTime < lastTick - 120 && guy.getHealth() <= 0) {
        reset();
      }
      if (state == State.PLAYERMOVING) {
        guy.cancelMove(false);
      }
    }

    if (state == State.PLAYERINPUT) {
      lastMouse.x = x;
      lastMouse.y = y;
      world.getTileCoords(x, y, temp);
      target.x = (int) temp.x;
      target.y = (int) temp.y;
      ui.setSelectorPos((int) temp.x * SpaceMain.tileSize + SpaceMain.tileSize / 2, (int) temp.y * SpaceMain.tileSize + SpaceMain.tileSize / 2);
      ui.setError(world.isTileBlocking((int) target.x, (int) target.y));
      Enemy activeEnemy = findActiveEnemy((int) target.x, (int) target.y);
      ui.setTarget(activeEnemy);
      if (activeEnemy != null) {
        ui.setActionCost(guy.getShotCost());
      } else {
        ui.setActionCost(0);
      }
      ui.setSkip(guy.inCombat() && guy.getTilePosition().x == target.x && guy.getTilePosition().y == target.y);
    }
  }

  private void refreshUI() {
    if (!SpaceMain.touchMode) {
      setAndDisplayAction(lastMouse.x, lastMouse.y);
    }
  }

  private Goody findActiveGoody(int x, int y) {
    for (Goody obj : goodies) {
      Vector2 pos = obj.getPosition();
      if (pos.x == x && pos.y == y && obj.isActive()) {
        return obj;
      }
    }
    return null;
  }

  private Enemy findActiveEnemy(int x, int y) {
    for (Enemy obj : enemies) {
      Vector3 pos = obj.getTilePosition();
      if (pos.x == x && pos.y == y && obj.getHealth() > 0) {
        return obj;
      }
    }
    return null;
  }
}
