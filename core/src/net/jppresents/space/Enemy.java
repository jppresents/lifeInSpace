package net.jppresents.space;

import com.brashmonkey.spriter.Animation;
import com.brashmonkey.spriter.Drawer;
import com.brashmonkey.spriter.Entity;

import java.util.List;

public class Enemy extends AnimatedGameObject {

  private Light light;
  private boolean aggro = false;
  private float aggroRange = 6;
  private boolean wasWalking;
  private boolean attackedThisTurn = true;
  private boolean social = true;

  public Enemy(Entity entity, Drawer drawer) {
    super(entity, drawer);
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
    if (animation.name.equals("front_hurt") || animation.name.equals("side_attack") || animation.name.equals("front_attack") || animation.name.equals("back_attack")) {
      setFaceRight(false);
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
    if (isAggro()) {
      attackedThisTurn = false;
      world.calcPath(this, this.getTilePosition(), guy.getTilePosition(), 1, enemies);
    }
  }

  private int lastGuyPosX, lastGuyPosY;

  public void updateEnemy(AnimatedGameObject guy, int tick, World world, List<Enemy> enemies) {
    if (aggro) {
      float distance = guy.calcDistance(this);

      if (distance >= (aggroRange + 3) && getHealth() == getMaxHealth()) {
        aggro = false;
        light.setColor(0.2f, 0.5f, 0.5f, 1);
      }

      if (getHealth() > 0 && getActionPoints() > 0 && isIdle(tick) && distance < 2 && !attackedThisTurn) {
        if (guy.getX() < getX()) {
          spriterPlayer.setAnimation("side_attack");
        } else if (guy.getX() > getX()) {
          spriterPlayer.setAnimation("side_attack");
          setFaceRight(true);
        } else if (guy.getY() > getY()) {
          spriterPlayer.setAnimation("back_attack");
        } else {
          spriterPlayer.setAnimation("front_attack");
        }
        guy.hit(getDamage());
        setIdleIn(600, tick);
        attackedThisTurn = true;
      }

    } else {
      if (getHealth() < getMaxHealth()) {
        aggro = true;
        light.setColor(0.5f, 0.2f, 0.2f, 1);
        return;
      }
      if (social) {
        for (Enemy e : enemies) {
          if (e.isAggro() && e.calcDistance(this) < 4) {
            aggro = true;
            light.setColor(0.5f, 0.2f, 0.2f, 1);
            return;
          }
        }
      }
      if ((int) guy.getTilePosition().y != lastGuyPosY || (int) guy.getTilePosition().x != lastGuyPosX) {
        lastGuyPosX = (int) guy.getTilePosition().x;
        lastGuyPosY = (int) guy.getTilePosition().y;
        if ((guy.calcDistance(this) <= aggroRange
            && world.hasLineOfSight((int) getTilePosition().x, (int) getTilePosition().y, (int) guy.getTilePosition().x, (int) guy.getTilePosition().y))
            || getSecondarySortAttrib() < getMaxHealth()) {
          aggro = true;
          light.setColor(0.5f, 0.2f, 0.2f, 1);
        }
      }
    }
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
    setInitialHealth(3);
    setDamage(1);
    setMaxActionPoints(3);

    if (level == 2) {
      spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("SpikeLeg");
      setDamage(2);
      setInitialHealth(4);
    }
    if (level == 3) {
      spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("SpikeHead");
      setMaxActionPoints(4);
      setDamage(2);
      setInitialHealth(5);
    }
    if (level == 4) {
      spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("SpikeHead");
      spriterPlayer.characterMaps[1] = spriterPlayer.getEntity().getCharacterMap("SpikeLeg");
      setMaxActionPoints(4);
      setDamage(3);
      setInitialHealth(6);
    }
    if (level == 5) {
      spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("EyeHead");
      setMaxActionPoints(5);
      setDamage(3);
      setInitialHealth(10);
      aggroRange = 9;
      social = false;
    }
  }

  private void setInitialHealth(int value) {
    setMaxHealth(value);
    setHealth(value);
  }

}
