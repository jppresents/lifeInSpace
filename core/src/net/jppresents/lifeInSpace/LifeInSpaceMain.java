package net.jppresents.lifeInSpace;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
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
  private OrthographicCamera camera;
  private OrthographicCamera lightBufferCamera;
  private Vector3 touchPoint = new Vector3();


  float lightX, lightY;
  FrameBuffer lightBuffer;

  OrthogonalTiledMapRenderer mapRenderer;
  private Batch lightBufferBatch;

  @Override
  public void dispose() {
    map.dispose();
    light.dispose();
  }

  TiledMap map;
  TmxMapLoader mapLoader;

  @Override
  public void create() {
    camera = new OrthographicCamera();
    lightBufferCamera = new OrthographicCamera(1280, 720);
    viewport = new ExtendViewport(1280, 720, camera);
    camera.translate(1280/2, 720/2);
    batch = new SpriteBatch();
    lightBatch = new SpriteBatch();
    lightBufferBatch = new SpriteBatch();

    img = new Texture("badlogic.jpg");

    light = new Texture("light_org.png");

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


    mapLoader = new TmxMapLoader();
    map =  mapLoader.load("world/world.tmx");
    mapRenderer = new OrthogonalTiledMapRenderer(map, 1);

  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, false);
    if (lightBuffer != null)
      lightBuffer.dispose();
    camera.update();
    lightBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    lightBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    lightBufferCamera.viewportHeight = height;
    lightBufferCamera.viewportWidth = width;
    lightBufferCamera.position.set(width/2, height/2, 0);
    lightBufferCamera.update();
  }

  @Override
  public void render() {
//    Gdx.gl.glClearColor(1, 1, 1, 1);
//    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get(0);

    camera.update();

    mapRenderer.setView(camera);
    mapRenderer.render();

    player.update();


    batch.setProjectionMatrix(camera.combined);
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

    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

// start rendering the lights to our spriteBatch
    lightBatch.setProjectionMatrix(camera.combined);
    lightBatch.begin();

    //Scale
    float scale = lightBuffer.getWidth() / 1280f;
//    System.out.println(scale);


    lightBatch.setColor(1, 1, 1, 1);

    lightBatch.draw(light, touchPoint.x - 128/2, touchPoint.y - 128/2, 128, 128, 0, 0, 128, 128, false, false);


    lightBatch.setColor(1, 0, 0, 1);
    lightBatch.draw(light, 0, 0, 128, 128, 0, 0, 128, 128, false, false);

    lightBatch.setColor(0, 1, 0, 1);
    lightBatch.draw(light, 1280-128, 720-128, 128 , 128, 0, 0, 128, 128, false, false);
//
//    lightBatch.setColor(0, 0, 1, 1);
//    lightBatch.draw(light, lightBuffer.getWidth() - 128 * scale, 0, 128* scale, 128* scale, 0, 0, 128, 128, false, false);
//
//    lightBatch.setColor(0, 1, 1, 1);
//    lightBatch.draw(light, 0, lightBuffer.getHeight() - 128 * scale, 128* scale, 128* scale, 0, 0, 128, 128, false, false);


    lightBatch.end();
    lightBuffer.end();


// now we render the lightBuffer to the default "frame buffer"
// with the right blending !
    lightBufferBatch.setProjectionMatrix(lightBufferCamera.combined);
    lightBufferBatch.setColor(1, 1, 1, 1);
    lightBufferBatch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
    lightBufferBatch.begin();
    lightBufferBatch.draw(lightBuffer.getColorBufferTexture(), 0, 0, lightBuffer.getWidth(), lightBuffer.getHeight(), 0, 0, lightBuffer.getWidth(), lightBuffer.getHeight(), false, true);
    lightBufferBatch.end();

// post light-rendering
// you might want to render your statusbar stuff here

    if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
      camera.translate(-64, 0);
    }
    if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
      camera.translate(64, 0);
    }
    if(Gdx.input.isKeyPressed(Input.Keys.UP)){
      camera.translate(0, 64);
    }
    if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
      camera.translate(0, -64);
    }


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
    player.setPosition(touchPoint.x, touchPoint.y);
//    viewport.project(touchPoint);
//    lightX = touchPoint.x;
//    lightY = touchPoint.y;
    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    camera.unproject(touchPoint.set(screenX, screenY, 0));
    player.setPosition(touchPoint.x, touchPoint.y);
    lightX = touchPoint.x;
    lightY = touchPoint.y;
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
