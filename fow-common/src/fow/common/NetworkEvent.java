package fow.common;

import java.io.Serializable;

public class NetworkEvent implements Serializable {

	private static final long serialVersionUID = -6292051594155761005L;

	/**
     * Document new NetworkEvents by declaring direction, type of data, and purpose.
     * 
     * PING - server<->client - null - Check for connection
     * 
     * IDENTIFY - server->client - Short tempId - Ask client to send it's accountId along with
     * tempId
     * 
     * IDENTIFY - client->server - Object[] {Short tempId, Integer accountId} - Send client identity
     * in response to request
     * 
     * CONFIRMED - server->client - Short gameId - Tell client handshake is confirmed and gives it
     * it's gameId
     * 
     * DISCONNECT - server->server - Object[] {Boolean confirmed, Short id} - A ClientConnection
     * informs the server that it is disconnected from the Client, gives it identifying information
     * to remove that connection
     * 
     * DISCONNECT - server->client - null - Tell client they've been disconnected
     * 
     * DISCONNECT - client->client - null - Client lets itself know it's been disconnected
     * 
     * IDLE - server->server - Object[] {Boolean confirmed, Short id} - A ClientConnection informs
     * the server that it client has been idle, gives it identifying information to handle pinging
     * that client
     * 
     * NEW_CONNETION - server->server - Socket clientSocket - The server's ServerSocket informs the
     * server it has received a new connection and gives it the socket so it can initiate the
     * handshake
     * 
     * DUPLICATE_ACCOUNT - server->client - null - Tell client someone with same account is already
     * part of this game
     * 
     * SERVER_FULL - server->client - null - Tell client the server is full and cannot accept
     * additional players
     * 
     * FAILED_CONNECTION - client->client - null - Informs the client that connection has failed.
     * 
     * REQUEST_MOVE - PCapp->server->DMapp - MoveRequest - Request the DM to move a particular
     * character to a given position
     * 
     * REQUEST_MOVE - DMapp->server - MoveRequest - Notify the server of a DM-approved move that
     * needs to be carried out. Not necessarily in response to a previous REQUEST_MOVE
     * 
     * UPDATE_VISIBILITY - server->DM/PCapp - VisibilityLater - Notify the DM or PC app of a
     * change in visibility and provide the necessary data to redraw the view.
     */
	public enum Type {
		PING, IDENTIFY, CONFIRMED, DISCONNECT, IDLE, NEW_CONNECTION, DUPLICATE_ACCOUNT,
		SERVER_FULL, FAILED_CONNECTION, REQUEST_MOVE, UPDATE_VISIBILITY
	}

    private Type type;
    private Object data;

    /** id associated with ClientConnection */
    private short id;

    /**
     * Data can be an Object[], but numbers will be converted to doubles over the network.
     * 
     * @param type The type of the event
     * @param data The data for the event
     */
    public NetworkEvent(final Type type, final Object data) {
        this.type = type;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public short getGameId() {
        return id;
    }

    @Override
    public String toString() {
        return new StringBuilder("[").append("type:")
                .append(type == null ? "null" : type.toString()).append(", data:")
                .append(data == null ? "null" : data.toString()).append("]").toString();
    }
    
	/**
	 * Throw this exception when the data object cannot be cast to a valid class
	 * (after deserialization).
	 * 
	 * @author Ben
	 * 
	 */
    public class BadDataClassException extends Exception {
		private static final long serialVersionUID = -4001876126029464870L;

		public BadDataClassException(String message) {
    		super(message);
    	}
    }
}
