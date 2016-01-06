package net.jppresents.space;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.brashmonkey.spriter.Animation;
import com.brashmonkey.spriter.Drawer;
import com.brashmonkey.spriter.Entity;
import com.brashmonkey.spriter.Player;


public class Guy extends AnimatedGameObject {

  private int idleAnimationCount = 0;
  private boolean wasWalking = true;
  private int shotCost = 2;
  private Player effectPlayer;
  private Drawer effectDrawer;
  private int gunLevel = 0;
  private boolean effectVisible;
  private int newGunLevel;

  public Guy(Entity entity, Drawer drawer, Entity effects, Drawer effectDrawer) {
    super(entity, drawer);
    this.effectDrawer = effectDrawer;
    this.effectPlayer = new Player(effects);
    effectPlayer.addListener(this);
    effectVisible = false;

    spriterPlayer.characterMaps = new Entity.CharacterMap[1];
    spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("gun_small");

    Light light = new Light(0, 0, 0, 40, 512, SpaceMain.lights);
    light.setColor(0.8f, 0.6f, 0.6f, 1);
    attachLight(light);
  }

  public int getGunLevel() {
    return this.gunLevel;
  }

  public void setGunLevel(int gunLevel) {
    this.gunLevel = gunLevel;
    if (gunLevel == 1) {
      spriterPlayer.characterMaps[0] = null;
      this.setDamage(2);
    }
  }

  @Override
  public void render(Batch batch) {
    super.render(batch);
    if (effectVisible) {
      effectPlayer.setPosition(spriterPlayer.getX(), spriterPlayer.getY());
      effectDrawer.draw(effectPlayer);
    }
  }

  @Override
  protected void updateAnimation() {
    if (effectVisible) {
      effectPlayer.update();
    }

    if (getMovement() != Movement.NONE) {
      wasWalking = true;
    }

    if (spriterPlayer.getAnimation().name.equals("front_item")) {
      return;
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

  public void showHealAnimation() {
    effectPlayer.setAnimation("heal");
    effectVisible = true;
  }

  @Override
  public void animationFinished(Animation animation) {


    if (animation.name.equals("newGun")) {
      setGunLevel(newGunLevel);
      effectVisible = false;
    }

    if (animation.name.equals("heal")) {
      effectVisible = false;
    }

    if (animation.name.equals("front_die")) {
      spriterPlayer.speed = 0;
      spriterPlayer.setTime(animation.length - 1);
    }

    if (animation.name.equals("front_idle_gun_flip") || animation.name.equals("front_item")) {
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

  public int getShotCost() {
    return shotCost;
  }


  public void pickupNewGun(int newGunLevel) {
    this.newGunLevel = newGunLevel;
    effectPlayer.setAnimation("newGun");
    spriterPlayer.setAnimation("front_item");
    effectVisible = true;
  }

}
