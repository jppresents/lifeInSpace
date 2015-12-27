package net.jppresents.lifeInSpace;

import com.brashmonkey.spriter.Animation;
import com.brashmonkey.spriter.Drawer;
import com.brashmonkey.spriter.Entity;

public class Guy extends AnimatedGameObject {

  private int idleAnimationCount = 0;

  public Guy(Entity entity, Drawer drawer) {
    super(entity, drawer);
  }

  @Override
  protected void updateAnimation() {
    if (spriterPlayer.flippedX() == -1) {
      spriterPlayer.flipX();
    }

    switch(getMovement()) {
      case NONE:
        if (!spriterPlayer.getAnimation().name.equals("front_idle_gun_flip")) {
          spriterPlayer.setAnimation("front_idle");
        }
        break;
      case LEFT:
        spriterPlayer.setAnimation("side_walk");
        break;
      case RIGHT:
        spriterPlayer.setAnimation("side_walk");
        if (spriterPlayer.flippedX() != -1) {
          spriterPlayer.flipX();
        }
        break;
      case UP:
        spriterPlayer.setAnimation("back_walk");
        break;
      case DOWN:
        spriterPlayer.setAnimation("front_walk");
        break;
    }
  }

  @Override
  public void animationFinished(Animation animation) {

    if (animation.name.equals("front_idle_gun_flip")) {
      spriterPlayer.setAnimation("front_idle");
      idleAnimationCount = 0;
    }

    if (animation.name.equals("front_idle")) {
      idleAnimationCount++;
    } else {
      idleAnimationCount = 0;
    }
    if (idleAnimationCount > 5) {
      spriterPlayer.setAnimation("front_idle_gun_flip");
    }
  }
}
