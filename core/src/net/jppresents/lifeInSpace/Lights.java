package net.jppresents.lifeInSpace;

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


  public Lights() {
    lightBufferCamera = new OrthographicCamera(1280, 720);
    lightBufferBatch = new SpriteBatch();
    lightBatch = new SpriteBatch();
    lightTexture = new Texture("light_org.png");

// setup the right blending
    lightBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
    lightBatch.enableBlending();

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
// start rendering to the lightBuffer
    lightBuffer.begin();

// set the ambient color values, this is the "global" lightTexture of your scene
// imagine it being the sun.  Usually the alpha value is just 1, and you change the darkness/brightness with the Red, Green and Blue values for best effect

    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

// start rendering the lights to our spriteBatch
    lightBatch.setProjectionMatrix(camera.combined);
    lightBatch.begin();

    //Scale
    float scale = lightBuffer.getWidth() / 1280f;
//    System.out.println(scale);

    for (Light light: lights) {
      lightBatch.setColor(light.getColor());
      lightBatch.draw(lightTexture, light.getX() - light.getSize()/2, light.getY() - light.getSize()/2, light.getSize(), light.getSize(), 0, 0, 128, 128, false, false);
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
}
