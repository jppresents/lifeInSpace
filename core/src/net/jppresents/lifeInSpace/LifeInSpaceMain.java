package net.jppresents.lifeInSpace;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.brashmonkey.spriter.*;

public class LifeInSpaceMain extends ApplicationAdapter implements InputProcessor {
  SpriteBatch batch;
  SpriteBatch lightBatch;
  Texture img;
  Texture light;

  public static LibGdxAtlasLoader loader;
  public static LibGdxDrawer drawer;

  private Player player;


  private Viewport viewport;
  private Camera camera;
  private Vector3 touchPoint = new Vector3();


  float lightX, lightY;

  FrameBuffer lightBuffer;
  TextureRegion lightBufferRegion;


  @Override
  public void create() {
    camera = new OrthographicCamera();
    viewport = new ExtendViewport(1280, 720, camera);
    batch = new SpriteBatch();
    lightBatch = new SpriteBatch();

    img = new Texture("badlogic.jpg");

    light = new Texture("light.png");

    FileHandle scmlHandle = Gdx.files.internal("guy/guy.scml");
    SCMLReader reader = new SCMLReader(scmlHandle.read());
    Data data = reader.getData();

    loader = new LibGdxAtlasLoader(data, Gdx.files.internal("guy/guy.atlas"));
    loader.load(scmlHandle.file());
    drawer = new LibGdxDrawer(loader, batch, null);

    player = new Player(data.getEntity("guy"));
    player.setPosition(100, 100);
    player.setAnimation("front_idle_gun_flip");

    Gdx.input.setInputProcessor(this);

    lightBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, 1280, 720, false);

    lightBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    lightBufferRegion = new TextureRegion(lightBuffer.getColorBufferTexture(), 0, 0, 1280, 720);

    lightBufferRegion.flip(false, false);

  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, true);
    batch.setProjectionMatrix(camera.combined);
    lightBatch.setProjectionMatrix(camera.combined);
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(1, 1, 1, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    player.update();
    batch.begin();
    batch.draw(img, 200, 200);
    drawer.draw(player);
    batch.end();


// start rendering to the lightBuffer
    lightBuffer.begin();

// setup the right blending
    lightBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
    lightBatch.enableBlending();

// set the ambient color values, this is the "global" light of your scene
// imagine it being the sun.  Usually the alpha value is just 1, and you change the darkness/brightness with the Red, Green and Blue values for best effect

    Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

// start rendering the lights to our spriteBatch
    lightBatch.begin();


// set the color of your light (red,green,blue,alpha values)
    lightBatch.setColor(1, 0.5f, 0.5f, 1);

// tx and ty contain the center of the light source
    float tx = lightX;
    float ty = lightY;

// tw will be the size of the light source based on the "distance"
// (the light image is 128x128)
// and 96 is the "distance"
// Experiment with this value between based on your game resolution
// my lights are 8 up to 128 in distance
    float tw = (128 / 100f) * 256;

// make sure the center is still the center based on the "distance"
    tx -= (tw / 2);
    ty -= (tw / 2);

// and render the sprite
    lightBatch.draw(light, tx, ty, tw, tw, 0, 0, 128, 128, false, true);

    //static light
    lightBatch.draw(light, 100, 100, 256, 256, 0, 0, 128, 128, false, true);

    lightBatch.end();
    lightBuffer.end();


// now we render the lightBuffer to the default "frame buffer"
// with the right blending !

    lightBatch.setColor(1, 1, 1, 1);
    lightBatch.begin();
    lightBatch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
    lightBatch.draw(lightBufferRegion, 0, 0, 1280, 720);
    lightBatch.end();

// post light-rendering
// you might want to render your statusbar stuff here


  }


  @Override
  public boolean keyDown(int keycode) {
    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    return false;
  }

  @Override
  public boolean keyTyped(char character) {
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    camera.unproject(touchPoint.set(screenX, screenY, 0));
    lightX = touchPoint.x;
    lightY = 720 - touchPoint.y;
    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    camera.unproject(touchPoint.set(screenX, screenY, 0));
    lightX = touchPoint.x;
    lightY = 720 - touchPoint.y;
    return true;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    return false;
  }

  @Override
  public boolean scrolled(int amount) {
    return false;
  }
}
