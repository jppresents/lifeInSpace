package net.jppresents.lifeInSpace;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.brashmonkey.spriter.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AnimatedGameObject implements SetPosition, Player.PlayerListener{


  protected enum Movement {NONE, LEFT, RIGHT, UP, DOWN}

  protected Player spriterPlayer;
  private Drawer drawer;
  private Vector3 position = new Vector3();
  private Vector3 target = new Vector3(-1, -1, 0);
  private List<Light> attachedLights = new ArrayList<Light>(1);
  private Movement movement = Movement.NONE;

  private int offsetY = 0;

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

  public AnimatedGameObject(Entity entity, Drawer drawer) {
    spriterPlayer = new Player(entity);
    spriterPlayer.addListener(this);
    this.drawer = drawer;
    spriterPlayer.setPosition(100, 100);
    spriterPlayer.setAnimation("front_idle");
    spriterPlayer.setTime(MathUtils.random(800)); //so not all idles are synchronized
  }

  protected void updateAnimation() {
    //override
  }

  public void update() {
    for (Light light: attachedLights) {
      light.setPosition(position.x, position.y);
    }
    if (target.x != -1 && target.y != -1) {
      if (Math.abs(position.x - target.x) < 5) {
        position.x = target.x;
      }
      if (Math.abs(position.y - target.y) < 5) {
        position.y = target.y;
      }
      if (position.x - target.x < 5) {
        position.x += 5;
        movement = Movement.RIGHT;
      } else if (position.x - target.x > 5) {
        position.x -= 5;
        movement = Movement.LEFT;
      } else if (position.y - target.y < 5) {
        position.y += 5;
        movement = Movement.UP;
      } else if (position.y - target.y > 5) {
        position.y -= 5;
        movement = Movement.DOWN;
      } else {
        movement = Movement.NONE;
      }
    }
    updateAnimation();
    spriterPlayer.setPosition(position.x, position.y + offsetY);
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
    position.set(x, y, 0);
  }

  public void setTarget(float x, float y) {
    target.set(x, y, 0);
  }

  public float getY() {
    return position.y;
  }

  public float getX() {
    return position.x;
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
