package net.jppresents.space;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Goody implements GameObject{

  private int amount;
  private Sprite sprite;
  private boolean active = true;
  private Vector2 position = new Vector2();
  private String type;
  private Light light;
  private boolean blink;

  public Goody(String goodieType, int amount) {
    type = goodieType;
    this.amount = amount;
    String regionName = type;
    if (type.equals("weaponkit")) {
      regionName = type + amount;
    }
    TextureAtlas.AtlasRegion region = SpaceMain.assets.getSprites().findRegion(regionName);
    if (region == null) {
      region = SpaceMain.assets.getSprites().findRegion("medkit");
      this.type = "medkit";
      Gdx.app.log("Error", "Unkown Goody type " + type +" converted to medkit");
    }
    sprite = new Sprite(region);
    blink = !this.type.equals("medkit");
    light = new Light(0, 0, (int)sprite.getWidth()/2, (int)sprite.getHeight()/2, 200, SpaceMain.lights);
    light.setColor(0.3f, 0.2f, 1, 1);
  }


  @Override
  public void render(Batch batch) {
    if (active) {
      sprite.draw(batch);
    }
  }

  @Override
  public void update(int tick) {
    if (active && blink) {
      light.setColor(light.getColor().r, light.getColor().g, light.getColor().b, 0.5f + (1 + MathUtils.sinDeg(tick * 10))/4);
    }
  }

  @Override
  public float getX() {
    return sprite.getX();
  }

  @Override
  public float getY() {
    return sprite.getY();
  }

  @Override
  public void dispose() {

  }

  public int getAmount() {
    return amount;
  }

  public Vector2 getPosition() {
    return position;
  }

  public void setPosition(float x, float y) {
    position.set(x, y);
    sprite.setPosition(x * SpaceMain.tileSize + SpaceMain.tileSize/2 - sprite.getWidth()/2, y  * SpaceMain.tileSize + SpaceMain.tileSize/2 - sprite.getHeight()/2);
    light.setPosition(sprite.getX(), sprite.getY());
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
    light.setOn(active);
  }

  @Override
  public int getSecondarySortAttrib() {
    return -1;
  }

  public String getType() {
    return type;
  }
}
