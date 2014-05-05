package fow.dmapp;

import fow.common.NetworkEvent;
import fow.common.NetworkEvent.Type;
import fow.dmapp.ServerConnection.OnReceiveNetworkEventListener;

public class PingListener implements OnReceiveNetworkEventListener {
    @Override
    public void onReceiveNetworkEvent(final ServerConnection connection,
            final NetworkEvent event) {
        if (event.getType().equals(Type.PING)) {
            connection.sendEvent(new NetworkEvent(Type.PING, null));
        } else if (event.getType().equals(Type.DISCONNECT)) {
            connection.kill();
        }
    }
}
