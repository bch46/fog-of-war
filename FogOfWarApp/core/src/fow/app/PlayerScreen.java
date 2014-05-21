package fow.app;

import fow.common.NetworkEvent;
import fow.common.NetworkEvent.Type;
import fow.common.VisibilityLayer;

public class PlayerScreen extends AbstractScreen {

    protected final PlayerMenu menuView;
    protected final MapView mapView;

    private static final int MENU_HEIGHT = 80;

    public PlayerScreen(Main game) {
        super(game);

        menuView = new PlayerMenu(this);
        mapView = new MapView();

        stage.addActor(menuView);
    }
    
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        menuView.setBounds(0, 0, width, MENU_HEIGHT);
        mapView.setCameraBounds(width, height - MENU_HEIGHT);
    }

    @Override
    public void show() {
        super.show();
        game.serverConnection.setOnReceiveNetworkEventListener(new NetworkEventListener());
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        mapView.render(this);
    }

    @Override
    public void dispose() {
        super.dispose();
        menuView.dispose();
    }

    public void sendMoveRequest() {
        NetworkEvent evt = new NetworkEvent(Type.REQUEST_MOVE, mapView.getCurrentPlayerPosition());
        game.serverConnection.sendEvent(new NetworkEvent(Type.REQUEST_MOVE, evt));
    }

    private class NetworkEventListener extends HandshakeListener {
        @Override
        public void onReceiveNetworkEvent(final ServerConnection serverConnection,
                final NetworkEvent event) {
            super.onReceiveNetworkEvent(serverConnection, event);
            // Client should only ever receive this type of event
            if (event.getType().equals(Type.UPDATE_VISIBILITY)) {
                mapView.updateVisibility((VisibilityLayer) event.getData());
            }
        }
    }
}
