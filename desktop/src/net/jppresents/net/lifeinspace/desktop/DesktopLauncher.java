package net.jppresents.net.lifeinspace.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import net.jppresents.lifeInSpace.LifeInSpaceMain;

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
    config.resizable = false;
		new LwjglApplication(new LifeInSpaceMain(), config);
	}
}
