package net.jppresents.space;

import com.brashmonkey.spriter.Animation;
import com.brashmonkey.spriter.Drawer;
import com.brashmonkey.spriter.Entity;

import java.util.List;

public class Enemy extends AnimatedGameObject {

  private Light light;
  private boolean aggro = false;
  private float aggroRange = 6;
  private float deAggroRange = 9;
  private boolean wasWalking;
  boolean attackedThisTurn = false;
  private int level;

  public Enemy(Entity entity, Drawer drawer, int tileSize) {
    super(entity, drawer, tileSize);
    light = new Light(0, 0, 0, 40, 300, SpaceMain.lights);
    light.setColor(0.2f, 0.5f, 0.5f, 1);
    attachLight(light);
    setCombat(true);
    spriterPlayer.characterMaps = new Entity.CharacterMap[2];
  }

  @Override
  protected void hurt() {
    SpaceMain.assets.playSound(Assets.SoundEffect.ALIEN_HURT);
    spriterPlayer.setAnimation("front_hurt");
  }

  @Override
  protected void die() {
    SpaceMain.assets.playSound(Assets.SoundEffect.ALIEN_DIE);
    spriterPlayer.setAnimation("front_die");
    fadeAllLights(0.1f);
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

  public void planTurn(World world, Guy guy, List<Enemy> enemies) {
    if (getHealth() <= 0)
      return;
    attackedThisTurn = false;
    if (isAggro()) {
      world.calcPath(this, this.getTilePosition(), guy.getTilePosition(), 1, enemies);
    }
  }

  public void updateEnemy(AnimatedGameObject guy, int tick) {
    if (aggro) {
      float distance = guy.calcDistance(this);

      if (distance >= deAggroRange && getHealth() == getMaxHealth()) {
        aggro = false;
        light.setColor(0.2f, 0.5f, 0.5f, 1);
      }

      if (getHealth() > 0 && getActionPoints() > 0 && isIdle(tick) && distance < 2 && !attackedThisTurn) {
        guy.hit(getDamage());
        setIdleIn(600, tick);
        attackedThisTurn = true;
      }

    } else {
      if (guy.calcDistance(this) <= aggroRange || getHealth() < getMaxHealth()) {
        aggro = true;
        light.setColor(0.5f, 0.2f, 0.2f, 1);
      }
    }
    return;
  }


  @Override
  protected void updateAnimation() {
    if (getHealth() > 0) {

      if (getMovement() != Movement.NONE) {
        wasWalking = true;
      }

      switch (getMovement()) {
        case NONE:
          if (wasWalking) {
            spriterPlayer.setAnimation("front_idle");
            wasWalking = false;
          }
          break;
        default:
          spriterPlayer.setAnimation("front_walk");
          break;
      }
    }

  }

  public boolean isAggro() {
    return aggro;
  }

  public void setLevel(int level) {
    this.level = level;
    setMaxHealth(level * 2);
    setHealth(level * 2);
    setDamage(level * 2);
    if (level == 2) {
      spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("SpikeLeg");
    }
    if (level == 3) {
      spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("SpikeHead");
    }
    if (level == 4) {
      spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("SpikeHead");
      spriterPlayer.characterMaps[1] = spriterPlayer.getEntity().getCharacterMap("SpikeLeg");
    }
    if (level == 5) {
      spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("EyeHead");
    }
  }

}
