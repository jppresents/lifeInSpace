package net.jppresents.lifeInSpace;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brashmonkey.spriter.*;

public class LifeInSpaceMain extends ApplicationAdapter {
    SpriteBatch batch;
    Texture img;

    public static LibGdxAtlasLoader loader;
    public static LibGdxDrawer drawer;

    private SCMLReader reader;
    private FileHandle scmlHandle;
    private Player player;


    @Override
    public void create() {
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");

        scmlHandle = Gdx.files.internal("guy/guy.scml");
        reader = new SCMLReader(scmlHandle.read());
        Data data = reader.getData();

        loader = new LibGdxAtlasLoader(data, Gdx.files.internal("guy/guy.atlas"));
        loader.load(scmlHandle.file());
        drawer = new LibGdxDrawer(loader, batch, null);

        player = new Player(data.getEntity("guy"));
        player.setPosition(100, 100);
        player.setAnimation("front_idle_gun_flip");
    }



    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        player.update();

        batch.begin();
        batch.draw(img, 100, 100);
        drawer.draw(player);
        batch.end();
    }
}
