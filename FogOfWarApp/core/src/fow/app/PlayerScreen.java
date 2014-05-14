package fow.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import fow.common.NetworkEvent;
import fow.common.NetworkEvent.Type;
import fow.common.PlayerState;
import fow.common.PositionTuple;
import fow.common.VisibilityLayer;

public class PlayerScreen extends AbstractScreen {

	private Texture texture;
	private VisibilityLayer visibility;
	
	public PlayerScreen(Main game) {
		super(game);
		NetworkEventListener listener = new NetworkEventListener();
		game.serverConnection.setOnReceiveNetworkEventListener(listener);

		texture = new Texture(Gdx.files.internal("gnome.gif"));
	}
	
	private class NetworkEventListener extends HandshakeListener {
		@Override
		public void onReceiveNetworkEvent (final ServerConnection serverConnection,
		             final NetworkEvent event) {
		    super.onReceiveNetworkEvent(serverConnection, event);
		    // Client should only ever receive this type of event
		    if (event.getType().equals(Type.UPDATE_VISIBILITY)) {
		        visibility = (VisibilityLayer) event.getData();
		    }
		}
	}

    @Override
    public void render(float delta) {
        super.render(delta);

        if (visibility != null) {
            SpriteBatch batch = getBatch();
            batch.begin();
            for (PlayerState player : visibility.getPlayers()) {
                PositionTuple curPos = player.path[player.path.length - 1];
                batch.draw(texture, curPos.x, curPos.y);
            }
            batch.end();
        }
    }
}
