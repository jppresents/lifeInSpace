package net.jppresents.space.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import net.jppresents.space.SpaceMain;

public class DesktopLauncher {
	private static boolean argsContain(String[] arg, String search) {
    for (String anArg : arg) {
      if (anArg.equalsIgnoreCase(search))
        return true;
    }
    return false;
  }

  public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		if (argsContain(arg, "small")) {
      config.width = 1280 / 2;
      config.height = 720 / 2;
    } else {
      config.width = 1280;
      config.height = 720;
    }
    config.title = "Life in space - and how to get rid of it";
    new LwjglApplication(new SpaceMain(false, argsContain(arg, "fast")), config);
	}
}
