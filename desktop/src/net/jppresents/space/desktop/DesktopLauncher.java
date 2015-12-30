package net.jppresents.space.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import net.jppresents.space.SpaceMain;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		if (arg.length  > 0 && arg[0].equals("small")) {
      config.width = 1280 / 2;
      config.height = 720 / 2;
    } else {
      config.width = 1280;
      config.height = 720;
    }
    config.title = "Life in space - and how to get rid of it";
		new LwjglApplication(new SpaceMain(), config);
	}
}
