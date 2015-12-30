package net.jppresents.space;

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

import java.text.NumberFormat;
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
    load("world");
  }

  private void load(String mapName) {
    map = mapLoader.load("world/" + mapName + ".tmx");
    mainLayer = (TiledMapTileLayer) (map.getLayers().get("world"));
    objLayer = map.getLayers().get("objects");
    mapRenderer = new OrthogonalTiledMapRenderer(map, 1);
    pathMap = new int[mainLayer.getHeight()][];
    for (int y = 0; y < mainLayer.getHeight(); y++) {
      pathMap[y] = new int[mainLayer.getWidth()];
      for (int x = 0; x < pathMap[y].length; x++) {
        if (isTileBlocking(x, y)) {
          pathMap[y][x] = Integer.MIN_VALUE;
        } else {
          pathMap[y][x] = 0;
        }
      }
    }
  }

  public void resetPosition(SetPosition guy, String name) {
    for (MapObject object : objLayer.getObjects()) {
      if (object.getName().equals(name)) {
        getTileCoords(((RectangleMapObject) object).getRectangle().getX(), ((RectangleMapObject) object).getRectangle().getY(), temp);
        guy.setPosition(temp.x * SpaceMain.tileSize, temp.y * SpaceMain.tileSize);
      }
    }
  }

  public int getCount(String name, String typ) {
    int i = 0;
    for (MapObject object : objLayer.getObjects()) {
      if (object.getName().equals(name) && object.getProperties().get("type").equals(typ)) {
        i++;
      }
    }
    return i;
  }

  public void resetPositions(List guys, String name, String typ) {
    int i = 0;
    for (MapObject object : objLayer.getObjects()) {
      if (object.getName().equals(name) && object.getProperties().get("type").equals(typ)) {
        SetPosition guy = (SetPosition) guys.get(i);
        getTileCoords(((RectangleMapObject) object).getRectangle().getX(), ((RectangleMapObject) object).getRectangle().getY(), temp);
        guy.setPosition(temp.x * SpaceMain.tileSize, temp.y * SpaceMain.tileSize);
        i++;
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
    return cell == null || cell.getTile().getProperties().containsKey("b");
  }

  public int getTileSize() {
    return (int) mainLayer.getTileWidth();
  }

  @Override
  public void dispose() {
    map.dispose();
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


  private void printMap() {
    System.out.println("-----");
    for (int y = pathMap.length - 1; y >= 0; y--) {
      String str = "";
      for (int x = 0; x < pathMap[y].length; x++) {
        if (pathMap[y][x] == Integer.MIN_VALUE) {
          str = str + "   ";
        } else if (pathMap[y][x] == Integer.MIN_VALUE + 1) {
          str = str + " x ";
        } else {
          NumberFormat nf = NumberFormat.getIntegerInstance();
          nf.setMinimumIntegerDigits(2);
          nf.setGroupingUsed(false);
          str = str + nf.format(pathMap[y][x]) + " ";
        }
      }
      System.out.println(str);
    }

  }

}
