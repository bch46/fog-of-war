package fow.dmserver;

import com.badlogic.gdx.net.Socket;

import fow.common.NetworkEvent;
import fow.common.NetworkEvent.Type;

/**
 * Helper class to keep event processing code separate from the server code.
 * This class and Server are tightly coupled.
 * 
 * @author Ben
 * 
 */
public class NetworkEventHandler {

	private final Server server;

	private final GameState state;

	private boolean debug;

	public NetworkEventHandler(final Server server, final boolean debug) {
		this.server = server;
		this.debug = debug;

		// TODO how to initialize
		state = new GameState(Constants.DEFAULT_LEVEL_WIDTH, Constants.DEFAULT_LEVEL_HEIGHT);
	}

	public NetworkEventHandler(final Server server) {
		this(server, false);
	}

	/**
	 * Parses each event and handles it appropriately.
	 * 
	 * @param e
	 *            The event to be handled
	 */
	protected void handleEvent(final NetworkEvent e) {
		if (e.getType().equals(Type.NEW_CONNECTION)) {
			handleNewConnection(e);
		} else if (e.getType().equals(Type.IDENTIFY)) {
			handleIdentify(e);
		} else if (e.getType().equals(Type.IDLE)) {
			handleIdle(e);
		} else if (e.getType().equals(Type.DISCONNECT)) {
			handleDisconnect(e);
		} else if (e.getType().equals(Type.REQUEST_MOVE)) {
			handleRequestMove(e);
		} else {
			// TODO print error? crash?
		}
	}

	/**
	 * Handles a new connection. Adds a ClientConnection to the pool of
	 * attempted client connections.
	 * 
	 * @param newConnection
	 *            The event holding the identifying information of the client
	 *            trying to connect
	 */
	private void handleNewConnection(final NetworkEvent newConnectionEvent) {
		final Socket clientSocket = (Socket) newConnectionEvent.getData();
		final int tempClientId = getTemporaryClientId();
		if (tempClientId >= 0) {
			final ClientConnection clientConnection = new ClientConnection(
					server, clientSocket, tempClientId, debug);
			server.unconfirmedClientConnections.put(tempClientId,
					clientConnection);
			clientConnection.sendEvent(new NetworkEvent(Type.IDENTIFY,
					tempClientId));
		} // this should never happen, have to more than Integer.MAX_VAL clients
	}

	/**
	 * Gets a temporary id used for identifying unconfirmed connections.
	 * 
	 * @return The temporary id or -1 if there are too many clients trying to
	 *         connect
	 */
	private int getTemporaryClientId() {
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			if (!server.confirmedClientConnections.containsKey(i)) {
				return i;
			}
		}
		return -1;
	}

    /**
     * Handles the addition of a client to the game after they complete the handshake. Graduates
     * ClientConnections from unconfirmed to confirmed.
     * 
     * @param identifyEvent The event from the client used for identifying themselves
     */
    private void handleIdentify(final NetworkEvent identifyEvent) {
        final Object[] ids = (Object[]) identifyEvent.getData();
        final Integer tempClientId = ((Integer) ids[0]);
        final Integer accountId = ((Integer) ids[1]);
        
        if (!server.isFull()) {
            if (server.confirmedClientConnections.containsKey(accountId)) {
                final ClientConnection clientConnection =
                        server.unconfirmedClientConnections.get(tempClientId);
                if (clientConnection != null) {
                    clientConnection.sendEvent(new NetworkEvent(Type.DUPLICATE_ACCOUNT, null));
                    clientConnection.kill();
                }
            } else {
                final ClientConnection clientConnection =
                        server.unconfirmedClientConnections.remove(tempClientId);
                if (clientConnection != null) {
                    // TODO this is a temporary hack
                    if (state.getDmId() < 0 || state.getDmId() == accountId.intValue()) {
                        System.out.println("DM joined with account id " + accountId);
                        state.setDmId(accountId);
                        server.confirmClient(clientConnection, accountId, true);
                    } else {
                        System.out.println("PC joined with account id " + accountId);
                        server.confirmClient(clientConnection, accountId, false);
                        
                        // If this is a new player, add it to the game state
                        if (!state.containsPlayer(accountId)) {
                            state.addNewPlayer(accountId);
                        }
                        // Send the client its visibility layer
                        clientConnection.sendEvent(new NetworkEvent(Type.UPDATE_VISIBILITY, state
                                .getPlayerVisibility(accountId)));
                    }
                } // else some client is doing something weird
            }
        } else {
            // Max number of clients reached for this server
            final ClientConnection clientConnection =
                    server.unconfirmedClientConnections.get(tempClientId);
            if (clientConnection != null) {
                clientConnection.sendEvent(new NetworkEvent(Type.SERVER_FULL, null));
                clientConnection.kill();
            }
        }
    }

	/**
	 * Handles an idle ClientConnection by pinging it.
	 * 
	 * @param idleEvent
	 *            The event holding the identifying information of the client
	 *            that needs to be pinged
	 */
	private void handleIdle(final NetworkEvent idleEvent) {
		final Object[] identity = (Object[]) idleEvent.getData();
		final boolean confirmed = (Boolean) identity[0];
		final Integer id = (Integer) identity[1];

		if (confirmed) {
			final ClientConnection clientConnection = server.confirmedClientConnections
					.get(id);
			if (clientConnection != null) {
				clientConnection.sendEvent(new NetworkEvent(Type.PING, null));
			}
		}
	}

	/**
	 * Handles a disconnection from a ClientConnection. Removes the connection
	 * from the lists of connections and kills it.
	 * 
	 * @param disconnectEvent
	 *            The event holding the identifying information of the client
	 *            that needs to be disconnected
	 */
	private void handleDisconnect(final NetworkEvent disconnectEvent) {
		final Object[] identity = (Object[]) disconnectEvent.getData();
		final boolean confirmed = (Boolean) identity[0];
		final Integer id = (Integer) identity[1];
		final ClientConnection clientConnection = confirmed ? server.confirmedClientConnections
				.remove(id) : server.unconfirmedClientConnections.remove(id);
		if (clientConnection != null) {
			clientConnection.kill();
		}

	}

	/**
	 * Handles a PC app's request to move. The move request may be to move any
	 * character to any position.
	 * 
	 * @param e
	 *            The event holding a request to move one character to a given
	 *            location
	 */
	private void handleRequestMove(final NetworkEvent e) {
		// TODO implement!
	}

}
