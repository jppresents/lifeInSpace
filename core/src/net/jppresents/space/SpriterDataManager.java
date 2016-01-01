package net.jppresents.space;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.brashmonkey.spriter.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpriterDataManager implements Disposable{

  private Map<String, Data> spriterData = new HashMap<String, Data>();
  private Map<String, LibGdxDrawer> spriterDrawer = new HashMap<String, LibGdxDrawer>();
  private List<LibGdxAtlasLoader> spriterLoader = new ArrayList<LibGdxAtlasLoader>();

  private SpriteBatch batch;

  public SpriterDataManager(SpriteBatch batch) {
    this.batch = batch;
  }

  public void load(String filename) {
    FileHandle scmlHandle = Gdx.files.internal(filename +"/" + filename + ".scml");
    SCMLReader reader = new SCMLReader(scmlHandle.read());
    Data data = reader.getData();
    LibGdxAtlasLoader loader = new LibGdxAtlasLoader(data, Gdx.files.internal(filename +"/" + filename + ".atlas"));
    loader.load(filename);
    LibGdxDrawer drawer = new LibGdxDrawer(loader, batch, null);
    spriterData.put(filename, data);
    spriterDrawer.put(filename, drawer);
    spriterLoader.add(loader);
  }

  Data getData(String filename) {
    return spriterData.get(filename);
  }

  Entity getEntity(String filename) {
    return spriterData.get(filename).getEntity(filename);
  }


  public Drawer getDrawer(String filename) {
    return spriterDrawer.get(filename);
  }

  @Override
  public void dispose() {
    for(Loader loader: spriterLoader) {
      loader.dispose();
    }
  }
}
