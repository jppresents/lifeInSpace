package net.jppresents.space;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class Combat {
  public enum ShotType {GUY, TURRET, ALIEN}

  private static int GUY_START = 19;
  private static int GUY_VISIBLE = 23;

  private static int TURRET_START = 19;
  private static int TURRET_VISIBLE = 23;

  private static int ALIEN_START = 25;
  private static int ALIEN_VISIBLE = 27;

  private class Shot {
    private Light light;
    private Sprite sprite;
    private Vector2 velocity = new Vector2();
    private boolean active = true;
    private int lifeTime = 50;
    private int startTick = 19;
    private int visibleTick = 23;
    private int damage = 0;
    public AnimatedGameObject shooter;

    public Shot() {
      sprite = new Sprite(SpaceMain.assets.getSprites().findRegion("shot"));
      light = new Light(0, 0, (int) sprite.getWidth() / 2, (int) sprite.getHeight() / 2, 150, SpaceMain.lights);
      light.setOn(false);
    }

    public int getTileX() {
      return (int) (sprite.getX() + sprite.getWidth() / 2) / SpaceMain.TILE_SIZE;
    }

    public int getTileY() {
      return (int) (sprite.getY() + sprite.getHeight() / 2) / SpaceMain.TILE_SIZE;
    }

    public void setPos(float x, float y) {
      sprite.setPosition(x - sprite.getWidth() / 2, y - sprite.getHeight() / 2);
    }


    private Enemy findTarget(List<Enemy> enemies, int x, int y) {
      for (Enemy enemy : enemies) {
        if (enemy.getTilePosition().x == x && enemy.getTilePosition().y == y && enemy.getHealth() > 0) {
          return enemy;
        }
      }
      return null;
    }


    private void update(int tick, World world, List<Enemy> enemies, Guy guy) {
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
        if (guy != shooter && guy.getTilePosition().x == getTileX() && guy.getTilePosition().y == getTileY()) {
          guy.hit(damage);
          active = false;
          light.setOn(false);
        }

        Enemy enemy = findTarget(enemies, getTileX(), getTileY());
        if (enemy != null && enemy != shooter) {
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

    public void setShotType(ShotType type) {
      if (type == ShotType.GUY) {
        sprite.setColor(1, 0.3f, 0.3f, 1);
        light.setColor(0.9f, 0.4f, 0.4f, 1.0f);
        visibleTick = GUY_VISIBLE;
        startTick = GUY_START;
      } else {
        sprite.setColor(0.3f, 0.3f, 1, 1);
        light.setColor(0.4f, 0.4f, 0.9f, 1.0f);
        if (type == ShotType.TURRET) {
          visibleTick = TURRET_VISIBLE;
          startTick = TURRET_START;
        } else {
          visibleTick = ALIEN_VISIBLE;
          startTick = ALIEN_START;
        }
      }
    }
  }

  private List<Shot> shots = new ArrayList<Shot>(2);

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


  public void shoot(Vector3 tilePosFrom, Vector3 tilePosTo, int damage, AnimatedGameObject shooter, ShotType color) {
    Shot shot = getShot();
    shot.setShotType(color);
    shot.damage = damage;
    shot.shooter = shooter;
    shot.active = true;
    shot.setPos(tilePosFrom.x * SpaceMain.TILE_SIZE + SpaceMain.TILE_SIZE / 2, tilePosFrom.y * SpaceMain.TILE_SIZE + SpaceMain.TILE_SIZE / 2);
    shot.velocity.set(tilePosTo.x - tilePosFrom.x, tilePosTo.y - tilePosFrom.y);
    shot.velocity.nor().scl(22);
    shot.sprite.setRotation(MathUtils.atan2(shot.velocity.y, shot.velocity.x) * MathUtils.radDeg + 180);
    active = true;
    tick = 0;
  }

  public void update(World world, List<Enemy> enemies, Guy guy) {
    tick++;
    boolean any = false;
    for (Shot shot : shots) {
      if (shot.active) {
        if (tick == shot.startTick) {
          SpaceMain.assets.playSound(Assets.SoundEffect.BLASTER);
        }
        if (tick > shot.startTick) {
          shot.update(tick, world, enemies, guy);
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
