package nbody.desktop;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import nbody.NBody;

public class DesktopLauncher {

	public static void main(String[] arg) {

		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "MyGame";
		cfg.width = 1280;
		cfg.height = 720;

		new LwjglApplication(new NBody(), cfg);
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
	}
}
