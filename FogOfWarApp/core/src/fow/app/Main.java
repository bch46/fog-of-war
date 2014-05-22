package fow.app;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

import fow.app.network.ServerConnection;

public class Main extends Game {
	public static final boolean DEV_MODE = true;
	public static final String LOG = Main.class.getSimpleName();

	public static final int port = 54321;

	public ServerConnection serverConnection;

	private MainScreen mainScreen;

	@Override
	public void create() {
		mainScreen = new MainScreen(this);
		this.setScreen(mainScreen);
	}

	@Override
	public void render() {
		super.render();

		Screen current = this.getScreen();

		if (serverConnection != null && current.equals(mainScreen)) {
			if (serverConnection.isDm()) {
				System.out.println("Enting MasterScreen");
				this.setScreen(new MasterScreen(this));
			} else {
				System.out.println("Entering PlayerScreen");
				this.setScreen(new PlayerScreen(this));
			}
		} else if (serverConnection == null && !current.equals(mainScreen)) {
			this.setScreen(mainScreen);
		}
	}
}
