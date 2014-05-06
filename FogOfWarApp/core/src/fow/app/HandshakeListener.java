package fow.app;

import fow.common.NetworkEvent;
import fow.common.NetworkEvent.Type;

public class HandshakeListener extends PingListener {

    private OnHandshakeResultListener onHandshakeResultListener;

    /**
     * Create an instance of the HandshakeListener to connect to our server. Upon successfully
     * opening a socket connection, this instance will attempt the necessary handshake for entry. It
     * will not provide the user with feedback on whether the handshake was successful or not.
     */
    public HandshakeListener() {}

    /**
     * Create an instance of the HandshakeListener to connect to our server. Upon successfully
     * opening a socket connection, this instance will attempt the necessary handshake for entry.
     * The user is later notified via the OnHandshakeResultListener whether the handshake was
     * successful or not.
     * 
     * @param onConnectionConfirmedListener
     */
    public HandshakeListener(OnHandshakeResultListener onHandshakeResultListener) {
        this.onHandshakeResultListener = onHandshakeResultListener;
    }

    @Override
    public void onReceiveNetworkEvent(final ServerConnection serverConnection,
            final NetworkEvent event) {
        super.onReceiveNetworkEvent(serverConnection, event);
        Type eventType = event.getType();
        if (eventType.equals(Type.IDENTIFY)) {
            serverConnection.sendEvent(new NetworkEvent(Type.IDENTIFY, new Object[] {
                    event.getData(), serverConnection.getAccountId()}));
        } else if (eventType.equals(Type.CONFIRMED)) {
        	final Object[] data = (Object[]) event.getData();
        	final short gameId = (Short) data[0];
        	final boolean isDm = (Boolean) data[1];
            serverConnection.setGameId(gameId);
            if (onHandshakeResultListener != null)
                onHandshakeResultListener.onHandshakeResult(serverConnection, true, isDm);
        } else if (eventType.equals(Type.DUPLICATE_ACCOUNT)
                || eventType.equals(Type.SERVER_FULL)) {
            // TODO (Ben) probably handle these differently to provide better feedback
            if (onHandshakeResultListener != null)
                onHandshakeResultListener.onHandshakeResult(serverConnection, false, false);
        }
    }

    public interface OnHandshakeResultListener {
        public void onHandshakeResult(ServerConnection connection, boolean success, boolean isDm);
    }
}
