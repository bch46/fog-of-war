package fow.app;

public class PlayerScreen extends AbstractScreen {

	public PlayerScreen(Main game) {
		super(game);
		NetworkEventListener listener = new NetworkEventListener();
		game.serverConnection.setOnReceiveNetworkEventListener(listener);
	}
	
	private class NetworkEventListener extends HandshakeListener {
		
	}
}
