package net.jppresents.space;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import java.util.List;

public class World implements Disposable {

  private OrthogonalTiledMapRenderer mapRenderer;
  private TiledMap map;
  private TmxMapLoader mapLoader;
  private TiledMapTileLayer mainLayer;
  private MapLayer objLayer;
  private int[][] pathMap; //y,x


  public World() {
    mapLoader = new TmxMapLoader();
  }

  public void changeLevel(String filename) {
    load(filename);
  }

  private void load(String mapName) {
    if (map != null) {
      map.dispose();
    }
    if (mapRenderer != null) {
      mapRenderer.dispose();
    }
    map = mapLoader.load("world/" + mapName + ".tmx");
    mainLayer = (TiledMapTileLayer) (map.getLayers().get("world"));
    objLayer = map.getLayers().get("objects");
    mapRenderer = new OrthogonalTiledMapRenderer(map, 1);
    updateCollision();
  }

  public void applyPlayerData(Guy guy) {
    for (MapObject object : objLayer.getObjects()) {
      if (object.getName().equals("start")) {
        getTileCoords(((RectangleMapObject) object).getRectangle().getX(), ((RectangleMapObject) object).getRectangle().getY(), temp);
        guy.setPosition(temp.x * SpaceMain.TILE_SIZE, temp.y * SpaceMain.TILE_SIZE);
        guy.setMaxHealth(Integer.parseInt((String) object.getProperties().get("hp")));
        guy.setHealth(guy.getMaxHealth());
        guy.setMaxActionPoints(Integer.parseInt((String) object.getProperties().get("ap")));
        guy.setGunLevel(Integer.parseInt((String) object.getProperties().get("gun")));
      }
    }
  }

  public void getTileCoords(float x, float y, Vector3 result) {
    result.z = 0;
    result.x = (float) Math.floor(x / mainLayer.getTileWidth());
    result.y = (float) Math.floor(y / mainLayer.getTileHeight());
  }

  Vector3 temp = new Vector3();

  public boolean isTileBlocking(int x, int y) {
    TiledMapTileLayer.Cell cell = mainLayer.getCell(x, y);
    return cell == null || cell.getTile().getProperties().containsKey("b") && cell.getTile().getProperties().get("b").equals("1");
  }

  @Override
  public void dispose() {
    if (map != null)
      map.dispose();
    if (mapRenderer != null)
      mapRenderer.dispose();
  }

  public void render(OrthographicCamera camera) {
    mapRenderer.setView(camera);
    mapRenderer.render();
  }

  public void restrictCamera(OrthographicCamera camera) {
    if (camera.position.x < camera.viewportWidth / 2) {
      camera.position.x = camera.viewportWidth / 2;
    }
    if (camera.position.y < camera.viewportHeight / 2) {
      camera.position.y = camera.viewportHeight / 2;
    }
    if (camera.position.x > mainLayer.getWidth() * mainLayer.getTileWidth() - camera.viewportWidth / 2) {
      camera.position.x = mainLayer.getWidth() * mainLayer.getTileWidth() - camera.viewportWidth / 2;
    }
    if (camera.position.y > mainLayer.getHeight() * mainLayer.getTileHeight() - camera.viewportHeight / 2) {
      camera.position.y = mainLayer.getHeight() * mainLayer.getTileHeight() - camera.viewportHeight / 2;
    }
  }


  private int getPathMapValue(int x, int y) {
    if (x > 0 && y > 0 && pathMap.length > y && pathMap[y].length > x) {
      return pathMap[y][x];
    }
    return Integer.MAX_VALUE;
  }

  public boolean calcPath(SetPath guy, Vector3 start, Vector3 target, int stopDistance, List<Enemy> enemies) {
    int startX = (int) start.x;
    int startY = (int) start.y;
    int targetX = (int) target.x;
    int targetY = (int) target.y;

    clearPathMap(enemies);
    pathMap[startY][startX] = 0;

    if (pathMap[targetY][targetX] != Integer.MIN_VALUE) {
      pathMap[targetY][targetX] = 1;
      while (pathMap[startY][startX] == 0) {
        if (!floodPathMap()) {
          return false; //no path found
        }
      }
      int x = startX;
      int y = startY;
      int oldX = 0;
      int oldY = 0;

      int step = 0;

      int lastDir = -1; // 0 right, 1 left, 2 up, 3 down

      while (calcDistance(x, y, targetX, targetY) > stopDistance) {
        int curVal = getPathMapValue(x, y);
        oldX = x;
        oldY = y;

        boolean left = false;
        boolean right = false;
        boolean up = false;
        boolean down = false;

        if (getPathMapValue(x + 1, y) < curVal && getPathMapValue(x + 1, y) > 0) {
          right = true;
        }
        if (getPathMapValue(x - 1, y) < curVal && getPathMapValue(x - 1, y) > 0) {
          left = true;
        }
        if (getPathMapValue(x, y + 1) < curVal && getPathMapValue(x, y + 1) > 0) {
          up = true;
        }
        if (getPathMapValue(x, y - 1) < curVal && getPathMapValue(x, y - 1) > 0) {
          down = true;
        }

        if (right && ((!up && !down) || lastDir != 0)) {
          lastDir = 0;
          x++;
        } else if (left && ((!up && !down) || lastDir != 1)) {
          lastDir = 1;
          x--;
        } else if (up) {
          lastDir = 2;
          y++;
        } else if (down) {
          lastDir = 3;
          y--;
        }

        if (oldX == x && oldY == y) {
          return false;
        }
        addPathValue(guy, x, y, step);
        step++;
      }
      guy.setPathLength(step);
      return true;
    }
    return false;
  }

  private int calcDistance(int x, int y, int targetX, int targetY) {
    return Math.abs((x - targetX)) + Math.abs((y - targetY));
  }

  private void addPathValue(SetPath guy, int x, int y, int step) {
    while (guy.getPath().size() < step + 1) {
      guy.getPath().add(new Vector2());
    }
    guy.getPath().get(step).x = x;
    guy.getPath().get(step).y = y;
  }

  private void clearPathMap(List<Enemy> enemies) {
    for (int y = 0; y < pathMap.length; y++) {
      for (int x = 0; x < pathMap[y].length; x++) {
        if (pathMap[y][x] != Integer.MIN_VALUE) {
          pathMap[y][x] = 0;
        }
      }
    }
    for (Enemy enemy : enemies) {
      if (enemy.getHealth() > 0) {
        pathMap[(int) enemy.getTilePosition().y][(int) enemy.getTilePosition().x] = Integer.MIN_VALUE + 1;
      }
    }
  }

  private boolean floodPathMap() {
    boolean flooded = false;
    for (int y = 0; y < pathMap.length; y++) {
      for (int x = 0; x < pathMap[y].length; x++) {

        int value = getPathMapValue(x + 1, y);
        if (value > 0 && value != Integer.MAX_VALUE && (pathMap[y][x] == 0 || pathMap[y][x] > value + 1)) {
          pathMap[y][x] = -1 * (value + 1);
          flooded = true;
        }
        value = getPathMapValue(x - 1, y);
        if (value > 0 && value != Integer.MAX_VALUE && (pathMap[y][x] == 0 || pathMap[y][x] > value + 1)) {
          pathMap[y][x] = -1 * (value + 1);
          flooded = true;
        }
        value = getPathMapValue(x, y + 1);
        if (value > 0 && value != Integer.MAX_VALUE && (pathMap[y][x] == 0 || pathMap[y][x] > value + 1)) {
          pathMap[y][x] = -1 * (value + 1);
          flooded = true;
        }
        value = getPathMapValue(x, y - 1);
        if (value > 0 && value != Integer.MAX_VALUE && (pathMap[y][x] == 0 || pathMap[y][x] > value + 1)) {
          pathMap[y][x] = -1 * (value + 1);
          flooded = true;
        }
      }
    }
    for (int y = 0; y < pathMap.length; y++) {
      for (int x = 0; x < pathMap[y].length; x++) {
        if (pathMap[y][x] != Integer.MIN_VALUE && pathMap[y][x] != Integer.MIN_VALUE + 1) {
          pathMap[y][x] = Math.abs(pathMap[y][x]);
        }
      }
    }
    return flooded;
  }

  public void loadGameEvents(List<GameEvent> events) {
    events.clear();
    for (MapObject object : objLayer.getObjects()) {
      if (object.getName().equals("event")) {
        getTileCoords(((RectangleMapObject) object).getRectangle().getX(), ((RectangleMapObject) object).getRectangle().getY(), temp);
        int x = (int)temp.x;
        int y = (int)temp.y;
        getTileCoords(((RectangleMapObject) object).getRectangle().getWidth(), ((RectangleMapObject) object).getRectangle().getHeight(), temp);
        int width = (int)temp.x;
        int height = (int)temp.y;

        String key = (String) object.getProperties().get("key");
        String type = (String) object.getProperties().get("type");
        GameEvent.EventType eventType = GameEvent.EventType.NONE;
        if ("end".equals(type))
         eventType = GameEvent.EventType.ENDING;
        if ("text".equals(type))
          eventType = GameEvent.EventType.TEXT;
        if ("teleport".equals(type))
          eventType = GameEvent.EventType.TELEPORT;

        if (eventType != GameEvent.EventType.NONE) {
          GameEvent event = new GameEvent(x, y, width, height, eventType, key);
          events.add(event);
        } else {
          Gdx.app.log("ERROR", "Unknown Event Type in Map: " + type);
        }
      }
    }
  }

  public void loadEnemies(List<Enemy> enemies, SpriterDataManager spriterDataManager) {
    enemies.clear();
    for (MapObject object : objLayer.getObjects()) {
      if (object.getName().equals("enemy")) {
        getTileCoords(((RectangleMapObject) object).getRectangle().getX(), ((RectangleMapObject) object).getRectangle().getY(), temp);
        String type = (String) object.getProperties().get("type");
        Enemy enemy = new Enemy(spriterDataManager.getEntity("alien", type), spriterDataManager.getDrawer("alien"));
        enemy.setDamage( Integer.parseInt((String) object.getProperties().get("dmg")));
        enemy.setMaxHealth(Integer.parseInt((String) object.getProperties().get("hp")));
        enemy.setHealth(enemy.getMaxHealth());
        enemy.setMaxActionPoints( Integer.parseInt((String) object.getProperties().get("ap")));
        enemy.setLook( Integer.parseInt((String) object.getProperties().get("look")));
        String gunDamage = (String) object.getProperties().get("gun");
        int gunDamageInt = 0;
        if (gunDamage != null) {
          gunDamageInt = Integer.parseInt(gunDamage);
        }
        enemy.setGunDamage(gunDamageInt);
        enemy.setAggroRange( Integer.parseInt((String) object.getProperties().get("aggro")));
        enemy.setPosition(temp.x * SpaceMain.TILE_SIZE, temp.y * SpaceMain.TILE_SIZE);
        enemy.setTurret(type.equals("turret"));
        enemies.add(enemy);
      }
    }
  }

  public void loadGoodies(List<Goody> goodies) {
    goodies.clear();
    for (MapObject object : objLayer.getObjects()) {
      if (object.getName().equals("goody")) {
        getTileCoords(((RectangleMapObject) object).getRectangle().getX(), ((RectangleMapObject) object).getRectangle().getY(), temp);
        String type = (String) object.getProperties().get("type");
        String amount = (String) object.getProperties().get("amount");
        int amountInt = 0;
        if (amount != null) {
          amountInt = Integer.parseInt(amount);
        }
        Goody goody = new Goody(type, amountInt);
        goody.setPosition(temp.x, temp.y);
        goodies.add(goody);
      }
    }
  }


  public int getTileIndex(int x, int y) {
    TiledMapTileLayer.Cell cell = mainLayer.getCell(x, y);
    if (cell == null)
      return -1;
    return cell.getTile().getId() - 1;
  }

  private Vector2 ray = new Vector2();
  private Vector2 vel = new Vector2();
  private Vector3 checkCoords = new Vector3();

  private boolean hit(float targetx, float targety, Vector2 vec, int tolarance) {
    return Math.abs(targetx - vec.x) < tolarance && Math.abs(targety - vec.y) < tolarance;
  }


  public boolean hasLineOfSight(float x, float y, float x2, float y2) {
    return hasLineOfSight((int)x, (int)y, (int) x2, (int) y2);
  }

  public boolean hasLineOfSight(int x, int y, int x2, int y2) {
    ray.set(x * SpaceMain.TILE_SIZE + SpaceMain.TILE_SIZE /2, y * SpaceMain.TILE_SIZE + SpaceMain.TILE_SIZE /2);
    vel.set(x2 - x, y2 - y);
    vel.nor().scl(SpaceMain.TILE_SIZE /4);
    float tx = x2 * SpaceMain.TILE_SIZE + SpaceMain.TILE_SIZE /2;
    float ty = y2 * SpaceMain.TILE_SIZE + SpaceMain.TILE_SIZE /2;

    while (!hit(tx, ty, ray, SpaceMain.TILE_SIZE /4)) {
      ray.add(vel);
      getTileCoords(ray.x, ray.y, checkCoords);
      if (isTileBlocking((int)checkCoords.x, (int)checkCoords.y)) {
        return false;
      }
    }
    return true;
  }

  public boolean isLoaded() {
    return map != null;
  }

  public void setTileIndex(int x, int y, int index) {
    TiledMapTileLayer.Cell cell = mainLayer.getCell(x, y);
    if (cell != null) {
      cell.setTile(map.getTileSets().getTile(index + 1));
    }
  }

  public void updateCollision() {
    if (pathMap == null || pathMap.length != mainLayer.getHeight()) {
      pathMap = new int[mainLayer.getHeight()][];
    }
    for (int y = 0; y < mainLayer.getHeight(); y++) {
      if (pathMap[y] == null || pathMap[y].length != mainLayer.getWidth()) {
        pathMap[y] = new int[mainLayer.getWidth()];
      }
      for (int x = 0; x < pathMap[y].length; x++) {
        if (isTileBlocking(x, y)) {
          pathMap[y][x] = Integer.MIN_VALUE;
        } else {
          pathMap[y][x] = 0;
        }
      }
    }
  }
}
