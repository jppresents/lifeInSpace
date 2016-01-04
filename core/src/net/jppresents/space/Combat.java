package net.jppresents.space;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class Combat {
  private class Shot {
    private Light light;
    private Sprite sprite;
    private Vector2 velocity = new Vector2();
    private boolean active = true;
    private int lifeTime = 50;
    private int startTick = 19;
    private int visibleTick = 23;
    private int damage = 0;

    public Shot() {
      sprite = new Sprite(SpaceMain.assets.getSprites().findRegion("shot"));
      sprite.setColor(1, 0.3f, 0.3f, 1);
      light = new Light(0, 0, (int) sprite.getWidth() / 2, (int) sprite.getHeight() / 2, 150, SpaceMain.lights);
      light.setColor(0.9f, 0.4f, 0.4f, 1.0f);
      light.setOn(false);
    }

    public int getTileX() {
      return (int) (sprite.getX() + sprite.getWidth() / 2) / SpaceMain.tileSize;
    }

    public int getTileY() {
      return (int) (sprite.getY() + sprite.getHeight() / 2) / SpaceMain.tileSize;
    }

    public void setPos(float x, float y) {
      sprite.setPosition(x - sprite.getWidth() / 2, y - sprite.getHeight() / 2);
    }


    private Enemy findTarget(List<Enemy> enemies, int x, int y) {
      for (Enemy enemy : enemies) {
        if (enemy.getTilePosition().x == x && enemy.getTilePosition().y == y && enemy.getSecondarySortAttrib() > 0) {
          return enemy;
        }
      }
      return null;
    }


    private void update(int tick, World world, List<Enemy> enemies) {
      sprite.setPosition(sprite.getX() + velocity.x, sprite.getY() + velocity.y);
      light.setPosition(sprite.getX(), sprite.getY());

      if (tick > visibleTick) {
        light.setOn(true);
      }

      if (tick > startTick + lifeTime) {
        active = false;
        light.setOn(false);
      }

      if (active) {
        Enemy enemy = findTarget(enemies, getTileX(), getTileY());
        if (enemy != null) {
          enemy.hit(damage);
          active = false;
          light.setOn(false);
        }
      }

      if (world.isTileBlocking(getTileX(), getTileY())) {
        active = false;
        light.setOn(false);
        SpaceMain.assets.playSound(Assets.SoundEffect.FIZZLE);
      }

    }
  }

  private List<Shot> shots = new ArrayList<Shot>(10);

  private boolean active = false;

  private int tick;

  public Combat() {
  }

  private Shot getShot() {
    for (Shot shot : shots) {
      if (!shot.active)
        return shot;
    }
    Shot shot = new Shot();
    shots.add(shot);
    return shot;
  }


  public void shoot(Vector3 tilePosFrom, Vector3 tilePosTo, int damage) {
    Shot shot = getShot();
    shot.damage = damage;
    shot.active = true;
    shot.setPos(tilePosFrom.x * SpaceMain.tileSize + SpaceMain.tileSize / 2, tilePosFrom.y * SpaceMain.tileSize + SpaceMain.tileSize / 2);
    shot.velocity.set(tilePosTo.x - tilePosFrom.x, tilePosTo.y - tilePosFrom.y);
    shot.velocity.nor().scl(22);
    shot.sprite.setRotation(MathUtils.atan2(shot.velocity.y, shot.velocity.x) * MathUtils.radDeg + 180);
    active = true;
    tick = 0;
  }

  public void update(World world, List<Enemy> enemies) {
    tick++;
    boolean any = false;
    for (Shot shot : shots) {
      if (shot.active) {
        if (tick == shot.startTick) {
          SpaceMain.assets.playSound(Assets.SoundEffect.BLASTER);
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
    for (Shot shot : shots) {
      if (shot.active && tick > shot.visibleTick) {
        shot.sprite.draw(batch);
      }
    }
  }

  public boolean isActive() {
    return active;
  }

}
