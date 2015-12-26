package net.jppresents.lifeInSpace;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
  Texture img;

  public static LibGdxAtlasLoader loader;
  public static LibGdxDrawer drawer;

  private Player player;

  private Viewport viewport;
  private OrthographicCamera camera;

  private Vector3 touchPoint = new Vector3();

  private OrthogonalTiledMapRenderer mapRenderer;
  private Lights lights;
  private Light testLight;

  @Override
  public void dispose() {
    map.dispose();
    mapRenderer.dispose();
    loader.dispose();
    lights.dispose();
  }

  TiledMap map;
  TmxMapLoader mapLoader;

  @Override
  public void create() {
    camera = new OrthographicCamera();

    viewport = new ExtendViewport(1280, 720, camera);
    camera.translate(1280/2, 720/2);
    batch = new SpriteBatch();


    img = new Texture("badlogic.jpg");

    lights = new Lights();
    lights.setAmbientColor(0.5f, 0.3f, 0.3f, 1);

    testLight = new Light(100, 100, 600, lights);
    testLight.setColor(1, 1, 1, 1);

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
    camera.update();
    lights.resize(width, height);
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(1, 1, 1, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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


    lights.render(camera);

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
    testLight.setPosition(touchPoint.x, touchPoint.y);
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
    testLight.setPosition(touchPoint.x, touchPoint.y);
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
