package fow.app;

import com.badlogic.gdx.Game;

public class Main extends Game {
	public static final boolean DEV_MODE = true;
	public static final String LOG = Main.class.getSimpleName();

	public static final int port = 54321;

	private MainScreen mainScreen;

	@Override
	public void create() {
		mainScreen = new MainScreen(this);
		this.setScreen(mainScreen);
	}

	@Override
	public void render() {
		super.render();
	}
}
