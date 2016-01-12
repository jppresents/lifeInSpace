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
  private boolean shootThisTurn = false;
  private boolean socialAggro = false; //is permanent, because they saw a hurt alien
  private boolean social = true;
  private int gunDamage = 0;
  private boolean turret;

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
    if (turret) {
      SpaceMain.assets.playSound(Assets.SoundEffect.TURRET_HIT);
    } else {
      SpaceMain.assets.playSound(Assets.SoundEffect.ALIEN_HURT);
    }

    spriterPlayer.setAnimation("front_hurt");
  }

  @Override
  protected void die() {
    if (turret) {
      SpaceMain.assets.playSound(Assets.SoundEffect.TURRET_DIE);
    } else {
      SpaceMain.assets.playSound(Assets.SoundEffect.ALIEN_DIE);
    }
    spriterPlayer.setAnimation("front_die");
    fadeAllLights(0.1f);
  }

  @Override
  public void animationFinished(Animation animation) {
    if (animation.name.equals("front_hurt") || animation.name.equals("side_attack") || animation.name.equals("front_attack")
        || animation.name.equals("back_attack") || animation.name.equals("front_shoot")|| animation.name.equals("side_shoot")
        || animation.name.equals("back_shoot")) {
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
      if (gunDamage > 0) {
        shootThisTurn = world.hasLineOfSight(guy.getTilePosition().x, guy.getTilePosition().y, getTilePosition().x, getTilePosition().y);
        attackedThisTurn = true;
      } else {
        attackedThisTurn = false;
      }
      if (!shootThisTurn) {
        //either has no gun, or no line of sight so this turn move
        if (getActionPoints() > 0) {
          world.calcPath(this, getTilePosition(), guy.getTilePosition(), 1, enemies);
        } else {
          setAggro(false);
        }
      }
    }
  }

  private int lastGuyPosX, lastGuyPosY;

  private void setAggro(boolean value) {
    aggro = value;
    if (aggro) {
      light.setColor(0.5f, 0.2f, 0.2f, 1);
    } else {
      light.setColor(0.2f, 0.5f, 0.5f, 1);
    }
  }

  public void updateEnemy(AnimatedGameObject guy, int tick, World world, List<Enemy> enemies, Combat combat) {
    if (aggro) {
      float distance = guy.calcDistance(this);

      if (!socialAggro && distance >= (aggroRange + 3) && getHealth() == getMaxHealth()) {
        setAggro(false);
      }

      if (shootThisTurn) {
        SpaceMain.assets.playSound(Assets.SoundEffect.ENEMY_BLASTER);


        if (turret) {
          combat.shoot(getTilePosition(), guy.getTilePosition(), gunDamage, this, Combat.ShotType.TURRET);
          if ( Math.abs(guy.getX() - getX()) > Math.abs(guy.getY() - getY()) ) {
            if (guy.getX() < getX()) {
              spriterPlayer.setAnimation("side_shoot");
            } else if (guy.getX() > getX()) {
              spriterPlayer.setAnimation("side_shoot");
              setFaceRight(true);
            }
          } else {
            if (guy.getY() > getY()) {
              spriterPlayer.setAnimation("back_shoot");
            } else {
              spriterPlayer.setAnimation("front_shoot");
            }
          }
        } else {
          combat.shoot(getTilePosition(), guy.getTilePosition(), gunDamage, this, Combat.ShotType.ALIEN);
          spriterPlayer.setAnimation("front_shoot");
        }
        shootThisTurn = false;
        setIdleIn(35, tick);
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
        setIdleIn(35, tick);
        attackedThisTurn = true;
      }

    } else {
      if (getHealth() < getMaxHealth() && getActionPoints() > 0) {
        setAggro(true);
        return;
      }
      if (social) {
        for (Enemy e : enemies) {
          if (e != this && e.getHealth() > 0 && e.getHealth() < e.getMaxHealth() && e.calcDistance(this) < 4) {
            socialAggro = true;
            setAggro(true);
            return;
          }
        }
      }
      if ((int) guy.getTilePosition().y != lastGuyPosY || (int) guy.getTilePosition().x != lastGuyPosX) {
        lastGuyPosX = (int) guy.getTilePosition().x;
        lastGuyPosY = (int) guy.getTilePosition().y;
        if ((guy.calcDistance(this) <= aggroRange
            && world.hasLineOfSight((int) getTilePosition().x, (int) getTilePosition().y, (int) guy.getTilePosition().x, (int) guy.getTilePosition().y))) {
          setAggro(true);
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

  public void setLook(int look) {
    if (look == 2) {
      spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("SpikeLeg");
    }
    if (look == 3) {
      spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("SpikeHead");
    }
    if (look == 4) {
      spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("SpikeHead");
      spriterPlayer.characterMaps[1] = spriterPlayer.getEntity().getCharacterMap("SpikeLeg");
    }
    if (look >= 5) {
      spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("EyeHead");
    }
    if (look == -1) {
      spriterPlayer.characterMaps[0] = spriterPlayer.getEntity().getCharacterMap("Blue");
    }
  }

  public void setAggroRange(int aggroRange) {
    this.aggroRange = aggroRange;
  }

  public void setGunDamage(int gunDamage) {
    this.gunDamage = gunDamage;
  }

  public void setTurret(boolean turret) {
    this.turret = turret;
    if (turret) {
      setMaxActionPoints(0); //turrets don't walk - I don't care what the level data says.
    }
  }
}
