package net.jppresents.lifeInSpace;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.brashmonkey.spriter.Drawer;
import com.brashmonkey.spriter.Player;

public class AnimatedGameObject {
  private Player spriterPlayer;
  private Drawer drawer;
  private Vector3 position = new Vector3();
  private Vector3 target = new Vector3(-1, -1, 0);

  public AnimatedGameObject(SpriterDataManager data, String entityName) {
    spriterPlayer = new Player(data.getData(entityName).getEntity(entityName));
    drawer = data.getDrawer(entityName);
    spriterPlayer.setPosition(100, 100);
    spriterPlayer.setAnimation("front_idle");
    spriterPlayer.setTime(MathUtils.random(800)); //so not all idles are synchronized
  }

  public void update() {
    if (target.x != -1 && target.y != -1) {

      if (Math.abs(position.x - target.x) < 5) {
        position.x = target.x;
      }

      if (Math.abs(position.y - target.y) < 5) {
        position.y = target.y;
      }

      if (position.x - target.x < 5) {
        position.x += 5;
        spriterPlayer.setAnimation("side_walk");
        if (spriterPlayer.flippedX() != -1) {
          spriterPlayer.flipX();
        }
      } else if (position.x - target.x > 5) {
        position.x -= 5;
        spriterPlayer.setAnimation("side_walk");
        if (spriterPlayer.flippedX() == -1) {
          spriterPlayer.flipX();
        }
      } else if (position.y - target.y < 5) {
        position.y += 5;
        spriterPlayer.setAnimation("back_walk");
      } else if (position.y - target.y > 5) {
        position.y -= 5;
        spriterPlayer.setAnimation("front_walk");
      } else {
        spriterPlayer.setAnimation("front_idle");
      }
    }

    spriterPlayer.setPosition(position.x, position.y - 20);
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
}
