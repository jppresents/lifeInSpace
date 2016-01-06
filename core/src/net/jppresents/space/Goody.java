package net.jppresents.space;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class Goody implements GameObject{

  private int amount;
  private Sprite sprite;
  private boolean active = true;
  private Vector2 position = new Vector2();
  private String type;

  public Goody(String type) {
    this.type = type;
    sprite = new Sprite(SpaceMain.assets.getSprites().findRegion(type));
  }

  @Override
  public void render(Batch batch) {
    if (active) {
      sprite.draw(batch);
    }
  }

  @Override
  public void update() {
    //
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

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public void setPosition(float x, float y) {
    this.position.set(x, y);
    this.sprite.setPosition(x * SpaceMain.tileSize + SpaceMain.tileSize/2 - sprite.getWidth()/2, y  * SpaceMain.tileSize + SpaceMain.tileSize/2 - sprite.getHeight()/2);
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public int getSecondarySortAttrib() {
    return 0;
  }

  public String getType() {
    return type;
  }
}
