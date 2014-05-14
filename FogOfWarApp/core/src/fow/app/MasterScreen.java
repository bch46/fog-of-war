package fow.app;

public class MasterScreen extends AbstractScreen {
	
	public MasterScreen(Main game) {
		super(game);
		NetworkEventListener listener = new NetworkEventListener();
		game.serverConnection.setOnReceiveNetworkEventListener(listener);
	}
	
	private class NetworkEventListener extends HandshakeListener {
		
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		
	}

}
