package net.jppresents.space;

import com.brashmonkey.spriter.Animation;
import com.brashmonkey.spriter.Drawer;
import com.brashmonkey.spriter.Entity;


public class Guy extends AnimatedGameObject {

  private int idleAnimationCount = 0;
  private boolean wasWalking = true;

  public Guy(Entity entity, Drawer drawer, int tileSize) {
    super(entity, drawer, tileSize);
    spriterPlayer.characterMaps = new Entity.CharacterMap[1];
    spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("gun_small");

    Light light = new Light(0, 0, 0, 40, 512, SpaceMain.lights);
    light.setColor(0.8f, 0.6f, 0.6f, 1);
    attachLight(light);
  }

  public void setGunLevel(int lvl) {
    //todo -> charmap & gun laser color
  }

  @Override
  public void update() {
    super.update();
  }

  @Override
  protected void updateAnimation() {
    if (getMovement() != Movement.NONE) {
      wasWalking = true;
    }

    switch(getMovement()) {
      case NONE:
        if (wasWalking) {
          spriterPlayer.setAnimation("front_idle");
          wasWalking = false;
          setFaceRight(false);
        }
        break;
      case LEFT:
        spriterPlayer.setAnimation("side_walk");
        setFaceRight(false);
        break;
      case RIGHT:
        spriterPlayer.setAnimation("side_walk");
        setFaceRight(true);
        break;
      case UP:
        spriterPlayer.setAnimation("back_walk");
        setFaceRight(false);
        break;
      case DOWN:
        spriterPlayer.setAnimation("front_walk");
        setFaceRight(false);
        break;
    }
  }

  public void activateShootAnimation(float targetX, float targetY) {
    spriterPlayer.setAnimation("front_idle"); //forces the shooting animation to start over if one is currently running
    float distX = Math.abs(worldPosition.x - targetX);
    float distY = Math.abs(worldPosition.y - targetY);
    if (distX > distY) {
      setFaceRight(targetX > worldPosition.x);
      spriterPlayer.setAnimation("side_fire");
    } else {
      if (targetY > worldPosition.y) {
        spriterPlayer.setAnimation("back_fire");
        setFaceRight(false);
      } else {
        spriterPlayer.setAnimation("front_fire");
        setFaceRight(false);
      }
    }
  }

  @Override
  public void animationFinished(Animation animation) {
    if (animation.name.equals("front_die")) {
      spriterPlayer.speed = 0;
      spriterPlayer.setTime(animation.length - 1);
    }

    if (animation.name.equals("front_idle_gun_flip")) {
      spriterPlayer.setAnimation("front_idle");
      idleAnimationCount = 0;
    }

    if (animation.name.equals("front_fire") || animation.name.equals("back_fire") ||
        animation.name.equals("side_fire") || animation.name.equals("front_hurt") ) {
      spriterPlayer.setAnimation("front_idle");
      setFaceRight(false);
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

  @Override
  protected void die() {
    spriterPlayer.setAnimation("front_die");
    SpaceMain.assets.playSound(Assets.SoundEffect.GUY_HURT2);
  }

  @Override
  protected void hurt() {
    SpaceMain.assets.playSound(Assets.SoundEffect.GUY_HURT);
    spriterPlayer.setAnimation("front_hurt");
  }

  public void reset() {
    setCombat(false);
    setHealth(getMaxHealth());
    setGunLevel(0);
    spriterPlayer.setAnimation("front_idle");
    spriterPlayer.speed = 15;
  }
}
