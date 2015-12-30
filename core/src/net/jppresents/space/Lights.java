package net.jppresents.space;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

public class Lights implements Disposable{
  private SpriteBatch lightBatch;
  private Texture lightTexture;
  private OrthographicCamera lightBufferCamera;
  private FrameBuffer lightBuffer;
  private Batch lightBufferBatch;
  private List<Light> lights = new ArrayList<Light>(10);
  private Color ambientColor;


  public Lights() {
    lightBufferCamera = new OrthographicCamera(1280, 720);
    lightBufferBatch = new SpriteBatch();
    lightBatch = new SpriteBatch();
    lightTexture = new Texture("light.png");
    lightBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
    lightBatch.enableBlending();
    ambientColor = new Color(0.3f, 0.3f, 0.3f, 1);
  }

  @Override
  public void dispose() {
    lightBatch.dispose();
    lightTexture.dispose();
    lightBufferBatch.dispose();
    lightBuffer.dispose();
  }

  public void resize(int width, int height) {
    lightBufferCamera.viewportHeight = height;
    lightBufferCamera.viewportWidth = width;
    lightBufferCamera.position.set(width/2, height/2, 0);
    lightBufferCamera.update();
    if (lightBuffer != null)
      lightBuffer.dispose();
    lightBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    lightBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
  }

  public void render(Camera camera) {
    lightBuffer.begin();
    Gdx.gl.glClearColor(ambientColor.r, ambientColor.g, ambientColor.b, ambientColor.a);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    lightBatch.setProjectionMatrix(camera.combined);
    lightBatch.begin();
    for (int i = lights.size() - 1; i >= 0; i--) {
      Light light = lights.get(i);
      if (light.isOn()) {
        light.update();
        lightBatch.setColor(light.getColor());
        lightBatch.draw(lightTexture, light.getX() - light.getSize() / 2, light.getY() - light.getSize() / 2, light.getSize(), light.getSize(), 0, 0, 128, 128, false, false);
      } else {
        lights.remove(i);
      }
    }
    lightBatch.end();
    lightBuffer.end();
    lightBufferBatch.setProjectionMatrix(lightBufferCamera.combined);
    lightBufferBatch.setColor(1, 1, 1, 1);
    lightBufferBatch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
    lightBufferBatch.begin();
    lightBufferBatch.draw(lightBuffer.getColorBufferTexture(), 0, 0, lightBuffer.getWidth(), lightBuffer.getHeight(), 0, 0, lightBuffer.getWidth(), lightBuffer.getHeight(), false, true);
    lightBufferBatch.end();
  }

  public void addLight(Light light) {
    lights.add(light);
  }

  public void removeLight(Light light) {
    lights.remove(light);
  }

  public void setAmbientColor(float r, float g, float b, float a) {
    this.ambientColor.set(r, g, b, a);
  }
}
