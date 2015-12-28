package net.jppresents.lifeInSpace;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class Combat {
  private class Shot {
    private Light light;
    private Sprite sprite;
    private Vector2 velocity = new Vector2();
    private boolean active = true;
    private int lifeTime = 50;
    private int startTick = 20;
    private int visibleTick = 22;
    private int damage = 50;

    public Shot(Lights lights) {
      sprite = new Sprite(LifeInSpaceMain.assets.getSprites().findRegion("shot"));
      sprite.setCenter(sprite.getWidth()/2, sprite.getHeight()/2);
      sprite.setColor(1, 0.3f, 0.3f, 1);
      light = new Light(0, 0, (int)sprite.getWidth()/2, (int)sprite.getHeight()/2, 150, lights);
      light.setColor(0.9f, 0.4f, 0.4f, 1.0f);
      light.setOn(false);
    }


    private Enemy findTarget(List<Enemy> enemies, int x, int y) {
      for (Enemy enemy: enemies) {
        if (enemy.getTilePosition().x == x && enemy.getTilePosition().y == y && enemy.getHealth() > 0) {
          return enemy;
        }
      }
      return null;
    }


    private void update(int tick, World world, List<Enemy> enemies) {
      sprite.setPosition(sprite.getX() + velocity.x, sprite.getY() + velocity.y);
      light.setPosition(sprite.getX(), sprite.getY());

      if (tick > startTick) {
        light.setOn(true);
      }

      if (tick > startTick + lifeTime) {
        active = false;
        light.setOn(false);
      }

      if (active) {
        Enemy enemy = findTarget(enemies, (int)sprite.getX()/world.getTileSize(), (int)sprite.getY()/world.getTileSize());
        if (enemy != null) {
          enemy.hit(damage);
          active = false;
          light.setOn(false);
        }
      }

      if (world.isWorldBlocking(sprite.getX(), sprite.getY())) {
        active = false;
        light.setOn(false);
        LifeInSpaceMain.assets.playSound(Assets.SoundEffect.FIZZLE);
      }

    }
  }
  private List<Shot> shots = new ArrayList<Shot>(10);

  private Lights lights;
  private boolean active = false;

  private int tick;
  public Combat(Lights lights) {
    this.lights = lights;
  }

  private Shot getShot() {
    for (Shot shot: shots) {
      if (!shot.active)
        return shot;
    }
    Shot shot = new Shot(lights);
    shots.add(shot);
    return shot;
  }


  public void shoot(float fromX, float fromY, float toX, float toY) {
    Shot shot = getShot();
    shot.active = true;
    shot.sprite.setPosition(fromX, fromY);
    shot.velocity.set(toX - fromX, toY - fromY);
    shot.velocity.nor().scl(22);
    shot.sprite.setRotation(MathUtils.atan2(shot.velocity.y, shot.velocity.x) * MathUtils.radDeg + 180);
    active = true;
    tick = 0;
  }

  public void update(World world, List<Enemy> enemies) {
    tick++;
    boolean any = false;
    for (Shot shot: shots){
      if (shot.active) {
        if (tick == shot.startTick) {
          LifeInSpaceMain.assets.playSound(Assets.SoundEffect.BLASTER);
        }
        if (tick > shot.startTick) {
          shot.update(tick, world, enemies);
        }
        any = true;
      }
    }
    if (!any) {
      active = false;
    }
  }

  public void render(Batch batch) {
    for (Shot shot: shots) {
      if (shot.active && tick > shot.visibleTick) {
          shot.sprite.draw(batch);
      }
    }
  }

  public boolean isActive() {
    return active;
  }

}
