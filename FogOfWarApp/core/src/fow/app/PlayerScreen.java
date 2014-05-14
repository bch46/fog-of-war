package fow.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PlayerScreen extends AbstractScreen {

	private Texture texture;
	
	public PlayerScreen(Main game) {
		super(game);
		NetworkEventListener listener = new NetworkEventListener();
		game.serverConnection.setOnReceiveNetworkEventListener(listener);

		texture = new Texture(Gdx.files.internal("gnome.gif"));
	}
	
	private class NetworkEventListener extends HandshakeListener {
		
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		
		SpriteBatch batch = getBatch();
		batch.begin();
		batch.draw(texture, 50, 50);
		batch.end();
	}
}
