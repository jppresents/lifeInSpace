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
  private String nextEnding;


  private enum State {PLAYERINPUT, PLAYERMOVING, ENEMYTURN, COMBAT}

  private final UserInterface ui;
  private World world;
  private int triggerReset = 0;
  private String nextWorld;
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
  private List<GameEvent> events = new ArrayList<GameEvent>(20);


  private int gameOverTime = 0;
  private int lastTick = 0;

  public GameLogic(World world, List<GameObject> gameObjects, SpriterDataManager spriterDataManager, UserInterface ui, Combat combat) {
    this.ui = ui;
    this.world = world;
    this.gameObjects = gameObjects;
    this.spriterDataManager = spriterDataManager;
    this.combat = combat;

    guy = new Guy(spriterDataManager.getEntity("guy", "guy"), spriterDataManager.getDrawer("guy"), spriterDataManager.getEntity("effects", "effects"), spriterDataManager.getDrawer("effects"));
    gameObjects.add(guy);
  }

  public void teleportIn() {
    SpaceMain.assets.playSound(Assets.SoundEffect.TELEPORT);
    guy.spriterPlayer.setAnimation("front_teleport_in");
    guy.setVisible(true);
  }

  private int doorTimer = 0;

  private void handleDoor(int x, int y, boolean fullyOpen) {
    if (!fullyOpen) {
      if (world.getTileIndex(x, y) == 61 || world.getTileIndex(x, y) == 63) {
        if (!guy.hasKeyCard()) {
          SpaceMain.assets.playSound(Assets.SoundEffect.ERROR);
          return;
        }
        SpaceMain.assets.playSound(Assets.SoundEffect.DOOR);
        if (world.getTileIndex(x, y) == 61) {
          world.setTileIndex(x, y, 62);
        } else if (world.getTileIndex(x, y) == 63) {
          world.setTileIndex(x, y, 64);
        }
        doorTimer = 30;
        guy.setKeyCardCount(guy.getKeyCardCount() - 1);
      }
    } else {
      if (world.getTileIndex(x, y) == 62) {
        world.setTileIndex(x, y, 0);
        world.updateCollision();
      }
      if (world.getTileIndex(x, y) == 64) {
        world.setTileIndex(x, y, 38);
        world.updateCollision();
      }
    }
  }

  private float lastPosX, lastPosY;

  private void handleWorldInteraction() {
    if (triggerReset != 0)
      return;

    if (doorTimer > 0) {
      doorTimer--;
      if (doorTimer == 0) {
        handleDoor((int)lastPosX, (int)lastPosY + 1, true);
        handleDoor((int)lastPosX, (int)lastPosY - 1, true);
      }
    }

    if (guy.getHealth() > 0 && (guy.getTilePosition().x != lastPosX || guy.getTilePosition().y != lastPosY)) {

      lastPosX = guy.getTilePosition().x;
      lastPosY = guy.getTilePosition().y;

      handleDoor((int)lastPosX, (int)lastPosY + 1, false);
      handleDoor((int)lastPosX, (int)lastPosY - 1, false);

      //Light changes
      if (world.getTileIndex((int)lastPosX, (int)lastPosY) == 38) {
        SpaceMain.lights.fadeTo(SpaceMain.insideColor);
      } else if (world.getTileIndex((int)lastPosX, (int)lastPosY) == 0 || world.getTileIndex((int)lastPosX, (int)lastPosY) == 1) {
        SpaceMain.lights.fadeTo(SpaceMain.outsideColor);
      }

      handleEvents();

      //Goodie pickups
      Goody goody = findActiveGoody((int)lastPosX, (int)lastPosY);
      if (goody != null) {
        if ("weaponkit".equals(goody.getType())) {
          if (goody.getAmount() > guy.getGunLevel()) {
            SpaceMain.assets.playSound(Assets.SoundEffect.POWERUP);
            guy.cancelMove(false);
            guy.pickupNewGun(goody.getAmount());
          }
          goody.setActive(false);
        }
        if ("medkit".equals(goody.getType()) && guy.getHealth() < guy.getMaxHealth()) {
          SpaceMain.assets.playSound(Assets.SoundEffect.HEAL);
          goody.setActive(false);
          guy.setHealth(guy.getHealth() + goody.getAmount());
          guy.showHealAnimation();
        }
        if ("hpkit".equals(goody.getType())) {
          goody.setActive(false);
          SpaceMain.assets.playSound(Assets.SoundEffect.POWERUP);
          guy.setMaxHealth(guy.getMaxHealth() + goody.getAmount());
          guy.setHealth(guy.getHealth() + goody.getAmount());
          guy.showMaxHealthUpAnimation();
        }
        if ("apkit".equals(goody.getType())) {
          SpaceMain.assets.playSound(Assets.SoundEffect.POWERUP);
          goody.setActive(false);
          guy.setMaxActionPoints(guy.getMaxActionPoints() + goody.getAmount());
          guy.showMaxApUpAnimation();
        }
        if ("keycard".equals(goody.getType())) {
          SpaceMain.assets.playSound(Assets.SoundEffect.PICKUP);
          goody.setActive(false);
          guy.setKeyCardCount(guy.getKeyCardCount() + 1);
        }
      }
    }
  }



  public void handleEvents() {
    Vector3 pos = guy.getTilePosition();
    for (GameEvent event: events) {
      if (event.active && pos.x >= event.locX && pos.x <= event.locX + event.width && pos.y >= event.locY && pos.y <= event.locY + event.height) {
        event.active = false;
        switch(event.type) {
          case TEXT:
            ui.getTextBox().setText(SpaceMain.assets.getText(event.key), false);
            SpaceMain.assets.playRadioIfAvailable(event.key);
            break;
          case ENDING:
            SpaceMain.prefs.putBoolean(SpaceMain.Pref.WIN, true);
            SpaceMain.prefs.flush();
            triggerReset = 120;
            guy.cancelMove(true);
            nextEnding = event.key;
            guy.spriterPlayer.setAnimation("front_teleport_away");
            SpaceMain.lights.fadeOut();
            SpaceMain.assets.playSound(Assets.SoundEffect.TELEPORT);
            break;
          case TELEPORT:
            nextWorld = event.key;
            SpaceMain.prefs.putBoolean(SpaceMain.Pref.BEAT_UP_TO + event.key, true);
            SpaceMain.prefs.flush();
            triggerReset = 120;
            guy.cancelMove(true);
            guy.spriterPlayer.setAnimation("front_teleport_away");
            SpaceMain.lights.fadeOut();
            SpaceMain.assets.playSound(Assets.SoundEffect.TELEPORT);
            break;
        }
      }
    }
  }


  public void update(int tick) {
    if (triggerReset != 0) {
      triggerReset--;
      if (triggerReset == 0) {
        if (nextWorld != null) {
          world.changeLevel(nextWorld);
          reset();
        }
        if (nextEnding!= null) {
          SpaceMain.mainMenu.showEnding(nextEnding);
        }
        return;
      }
    }
    handleWorldInteraction();

    lastTick = tick;
    if (gameOverTime == 0 && guy.getHealth() <= 0) {
      SpaceMain.lights.fadeTo(0.2f, 0, 0, 0, 0);
      gameOverTime = tick;
    }

    for (Enemy enemy : enemies) {
      enemy.updateEnemy(guy, tick, world, enemies, combat);
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
          do {
            activeEnemy = enemies.get(nextActiveEnemyIndex);
            nextActiveEnemyIndex++;
          } while (nextActiveEnemyIndex < enemies.size() && !activeEnemy.isAggro());
          activeEnemy.resetActionPoints();
          activeEnemy.planTurn(world, guy, enemies);
        }
      } else {
        if (activeEnemy.isIdle(tick)) {
          activeEnemy = null; //next enemies turn
        }
      }
    }

    combat.update(world, enemies, guy);
    ui.setActionPoints(guy.getActionPoints());
    ui.setMaxActionPoints(guy.getMaxActionPoints());
    ui.setHealthPoints(guy.getHealth());
    ui.setKeyCardCount(guy.getKeyCardCount());
    ui.setMaxHealthPoints(guy.getMaxHealth());
    ui.showActionBar(guy.inCombat());
  }

  public void reset() {
    for (GameObject obj : gameObjects) {
      if (obj != guy)
        obj.dispose();
    }
    triggerReset = 0;
    nextWorld = null;
    gameObjects.clear();
    gameObjects.add(guy);
    guy.reset();
    world.applyPlayerData(guy);
    world.loadEnemies(enemies, spriterDataManager);
    world.loadGameEvents(events);
    gameObjects.addAll(enemies);
    world.loadGoodies(goodies);
    gameObjects.addAll(goodies);
    SpaceMain.lights.resetColor();
    gameOverTime = 0;
    resetCam = true;
    state = State.PLAYERINPUT;
    activeEnemy = null;
    guy.cancelMove(true);
    teleportIn();
    SpaceMain.lights.fadeIn();
  }


  public void controlCamera(OrthographicCamera camera, int tick) {
    if (resetCam) {
      guy.centerCamera(camera);
      world.restrictCamera(camera);
      moveCam.set(0, 0);
      resetCam = false;
    }

    if (ui.getTextBox().isActive()) {
      moveCam.set(0, 0);
      return;
    }

    camera.translate(moveCam.x * camera.viewportWidth/SpaceMain.viewport.getScreenWidth(), moveCam.y * camera.viewportHeight/SpaceMain.viewport.getScreenHeight());
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
      moveCam.add((dragFrom.x - x), -(dragFrom.y - y));
      dragFrom.set(x, y);
    }
  }

  public void executeAction() {
    if (!world.isLoaded())
      return;

    if (ui.getTextBox().isActive()) {
      if (ui.getTextBox().isDone()) {
        ui.getTextBox().hide();
        SpaceMain.assets.fadeOutCurrentRadio();
        dragFrom.set(-1, -1);
        moveCam.set(0, 0);
      } else {
        ui.getTextBox().setQuick(true);
      }
      return;
    }

    dragFrom.set(-1, -1);
    if (state == State.PLAYERINPUT) {
      if (guy.getHealth() <= 0) {
        if (gameOverTime < lastTick - 120) {
          SpaceMain.returnToMenu = true;
        }
        return;
      }

      if (!guy.isIdle(lastTick)) {
        return;
      }

      if (guy.getTilePosition().x == target.x && guy.getTilePosition().y == target.y) {
        guy.decActionPoints(guy.getActionPoints());
        ui.hideSelector();
        return;
      }

      Enemy enemy = findActiveEnemy((int) target.x, (int) target.y);

      if (enemy != null) {
        if (guy.getActionPoints() < guy.getShotCost()) {
          SpaceMain.assets.playSound(Assets.SoundEffect.ERROR);
          return;
        }
        guy.decActionPoints(guy.getShotCost());
        guy.setCurrentMovecostsActinPoints(true);
        combat.shoot(guy.getTilePosition(), enemy.getTilePosition(), guy.getDamage(), guy, Combat.ShotType.GUY);
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

  public void touchUpNoScroll() {
    if (state == State.PLAYERMOVING ) {
      guy.cancelMove(false);
    }
  }

  public void setAndDisplayAction(float x, float y) {
    if (!world.isLoaded())
      return;
    if (ui.getTextBox().isActive()) {
      if (SpaceMain.touchMode) {
        if (ui.getTextBox().isDone()) {
          ui.getTextBox().hide();
        } else {
          ui.getTextBox().setQuick(true);
        }
      }
      return; //no actions until textbox is done
    }


    if (SpaceMain.touchMode) {
      if (gameOverTime < lastTick - 120 && guy.getHealth() <= 0) {
        reset();
      }
    }

    if (state == State.PLAYERINPUT) {
      lastMouse.x = x;
      lastMouse.y = y;
      world.getTileCoords(x, y, temp);
      target.x = (int) temp.x;
      target.y = (int) temp.y;
      ui.setSelectorPos((int) temp.x * SpaceMain.TILE_SIZE + SpaceMain.TILE_SIZE / 2, (int) temp.y * SpaceMain.TILE_SIZE + SpaceMain.TILE_SIZE / 2);
      ui.setError(world.isTileBlocking((int) target.x, (int) target.y));
      Enemy activeEnemy = findActiveEnemy((int) target.x, (int) target.y);
      ui.setTarget(activeEnemy, guy.getActionPoints() >= guy.getShotCost());
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

  public boolean canResume() {
    return guy.getHealth() > 0;
  }

}
