package net.jppresents.lifeInSpace;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.brashmonkey.spriter.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AnimatedGameObject implements SetPosition, Player.PlayerListener, SetPath{


  @Override
  public List<Vector2> getPath() {
    return path;
  }

  @Override
  public void setPathLength(int pathLength) {
    this.pathLength = pathLength;
    currentPathTarget = 0;
  }

  public boolean isIdle() {
    return pathLength == 0 || currentPathTarget == pathLength;
  }

  public void cancelMove() {
    if (currentPathTarget < pathLength - 1) {
      pathLength = currentPathTarget + 1;
    }
  }

  protected enum Movement {NONE, LEFT, RIGHT, UP, DOWN}

  protected Player spriterPlayer;
  private Drawer drawer;
  private Vector3 worldPosition = new Vector3();
  private Vector3 tilePosition = new Vector3();
  private List<Vector2> path = new ArrayList<Vector2>(20);
  private int pathLength = 0;
  private int currentPathTarget = 0;
  private List<Light> attachedLights = new ArrayList<Light>(1);
  private Movement movement = Movement.NONE;

  private int tileSize = 0;

  private static YSortComparator ySortComparator = new YSortComparator();

  public static YSortComparator getYSortComparator() {
    return ySortComparator;
  }

  public void attachLight(Light light) {
    attachedLights.add(light);
  }

  private static class YSortComparator implements Comparator<AnimatedGameObject> {
    @Override
    public int compare(AnimatedGameObject o1, AnimatedGameObject o2) {
      return Math.round(o2.getY() - o1.getY());
    }
  }

  public AnimatedGameObject(Entity entity, Drawer drawer, int tileSize) {
    spriterPlayer = new Player(entity);
    spriterPlayer.addListener(this);
    this.drawer = drawer;
    this.tileSize = tileSize;
    spriterPlayer.setPosition(100, 100);
    spriterPlayer.setAnimation("front_idle");
    spriterPlayer.setTime(MathUtils.random(800)); //so not all idles are synchronized
  }

  protected void updateAnimation() {
    //override
  }

  private float toWorld(float pos) {
    return pos * tileSize;
  }

  public void update() {
    for (Light light: attachedLights) {
      light.setPosition(worldPosition.x + tileSize/2, worldPosition.y);
    }

    Vector2 target = null;
    if (pathLength > 0 && currentPathTarget < pathLength) {
      target = path.get(currentPathTarget);
    }

    if (target != null) {
      if (Math.abs(worldPosition.x - toWorld(target.x)) <= 10 && Math.abs(worldPosition.y - toWorld(target.y)) <= 10) {
        worldPosition.x = toWorld(target.x);
        worldPosition.y = toWorld(target.y);
        currentPathTarget++;
        if (currentPathTarget == pathLength) {
          this.movement = Movement.NONE;
        }
      } else {
        if (worldPosition.x - toWorld(target.x) < -5) {
          worldPosition.x += 5;
          movement = Movement.RIGHT;
        } else if (worldPosition.x - toWorld(target.x) > 5) {
          worldPosition.x -= 5;
          movement = Movement.LEFT;
        } else if (worldPosition.y - toWorld(target.y) < -5) {
          worldPosition.y += 5;
          movement = Movement.UP;
        } else if (worldPosition.y - toWorld(target.y) > 5) {
          worldPosition.y -= 5;
          movement = Movement.DOWN;
        }
      }
    }
    updateAnimation();
    spriterPlayer.setPosition(worldPosition.x + tileSize/2, worldPosition.y);
    tilePosition.x = Math.round(worldPosition.x / tileSize);
    tilePosition.y = Math.round(worldPosition.y / tileSize);
    spriterPlayer.update();
  }

  public void centerCamera(OrthographicCamera camera) {
    camera.position.x = spriterPlayer.getX();
    camera.position.y = spriterPlayer.getY();
  }

  public void draw() {
    drawer.draw(spriterPlayer);
  }

  public void setPosition(float x, float y) {
    worldPosition.set(x, y, 0);
  }

  public float getY() {
    return worldPosition.y;
  }

  public float getX() {
    return worldPosition.x;
  }

  public Vector3 getTilePosition() {
    return this.tilePosition;
  }

  protected Movement getMovement() {
    return movement;
  }

  @Override
  public void animationFinished(Animation animation) {
  }

  @Override
  public void animationChanged(Animation oldAnim, Animation newAnim) {
  }

  @Override
  public void preProcess(Player player) {
  }

  @Override
  public void postProcess(Player player) {
  }

  @Override
  public void mainlineKeyChanged(Mainline.Key prevKey, Mainline.Key newKey) {
  }
}
