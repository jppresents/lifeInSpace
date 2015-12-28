package net.jppresents.lifeInSpace;

import com.brashmonkey.spriter.Animation;
import com.brashmonkey.spriter.Drawer;
import com.brashmonkey.spriter.Entity;

public class Enemy extends AnimatedGameObject {
  public Enemy(Entity entity, Drawer drawer, int tileSize) {
    super(entity, drawer, tileSize);
  }

  @Override
  protected void hurt() {
    LifeInSpaceMain.assets.playSound(Assets.SoundEffect.ALIEN_HURT);
    spriterPlayer.setAnimation("front_hurt");
  }

  @Override
  protected void die() {
    LifeInSpaceMain.assets.playSound(Assets.SoundEffect.ALIEN_DIE);
    spriterPlayer.setAnimation("front_die");
    fadeAllLights(0.1f);
  }

  @Override
  protected void updateAnimation() {

  }

  @Override
  public void animationFinished(Animation animation) {
    if (animation.name.equals("front_hurt")) {
      spriterPlayer.setAnimation("front_idle");
    }

    if (animation.name.equals("front_die")) {
      spriterPlayer.speed = 0;
      spriterPlayer.setTime(animation.length - 1);
    }

  }
}
