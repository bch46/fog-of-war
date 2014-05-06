package fow.app;

public class MainScreen extends AbstractScreen {

	protected final MainView view;

	/**
	 * Initialize the elements of the main screen, namely the view
	 * 
	 * @param game
	 */
	public MainScreen(Main game) {
		super(game);

		view = new MainView(this);
		stage.addActor(view);
	}

	/**
	 * Called when the view's connect button is pressed, attempts to connect to
	 * a server with the information provided by the user.
	 */
	public void onConnectPressed() {
		// Check that IP text field isn't empty
		String ipAddress = view.getIPText().trim();
		if (ipAddress.equals("")) {
			System.out.println("Can't connect due to empty IP");
			return;
		}

		// Check that ID text fied isn't empty
		String idText = view.getIdText().trim();
		if (idText.equals("")) {
			System.out.println("Can't connect due to no ID");
			return;
		}

		// Use regex to validate the IP address format
		if (Utils.validateIP(ipAddress)) {
			int id = Integer.valueOf(idText).intValue();
			// Create a local instance of the server connection and attempt to connect
			// If connection fails this object will be garbage collected
			ServerConnection sc = new ServerConnection(ipAddress, Main.port, id, true);
			if (!sc.connect(new NetworkEventListener())) {
				// TODO provide user feedback
				System.out.println("Failed to connect to server");
			}
		} else {
			System.out.println("Can't connect due to invalid IP");
		}
	}

	private class NetworkEventListener extends HandshakeListener {
		public NetworkEventListener() {
			super(new HandshakeListener.OnHandshakeResultListener() {
				@Override
				public void onHandshakeResult(ServerConnection connection,
						boolean success, boolean isDm) {
					connection.setOnReceiveNetworkEventListener(null);
					if (success) {
						connection.setDm(isDm);
						game.serverConnection = connection;
					} else {
						// TODO provide user feedback
						System.out.println("Failed handshake with server");
					}
				}
			});
		}
	}
}
