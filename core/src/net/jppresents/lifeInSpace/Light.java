package net.jppresents.lifeInSpace;

import com.badlogic.gdx.graphics.Color;

public class Light {
  private float x, y;
  private Lights owner;
  private boolean on;
  private Color color;
  private float size;
  int offsetX;
  int offsetY;

  public Light(float x, float y, int offsetX, int offsetY, float size, Lights owner) {
    this.x = x;
    this.y = y;
    this.size = size;
    this.owner = owner;
    this.color = new Color(1, 1, 1 ,1);
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.setOn(true);
  }

  private void setOn(boolean value) {
    if (value != on) {
      on = value;
      if (on) {
        owner.addLight(this);
      } else {
        owner.removeLight(this);
      }
    }
  }

  public void setX(float worldX) {
    this.x = worldX;
  }

  public void setY(float worldY) {
    this.y = worldY;
  }

  public void setColor(float r, float g, float b, float a) {
    this.color.set(r, g, b, a);
  }

  public void setSize(float size) {
    this.size = size;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public boolean isOn() {
    return on;
  }

  public Color getColor() {
    return color;
  }

  public float getSize() {
    return size;
  }

  public void setPosition(float x, float y) {
    this.x = x + offsetX;
    this.y = y + offsetY;
  }
}
