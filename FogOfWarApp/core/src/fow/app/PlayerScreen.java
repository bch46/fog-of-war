package fow.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import fow.app.network.HandshakeListener;
import fow.app.network.ServerConnection;
import fow.common.NetworkEvent;
import fow.common.NetworkEvent.Type;
import fow.common.VisibilityLayer;

public class PlayerScreen extends AbstractScreen {

    protected final PlayerMenu menuView;
    protected final MapView mapView;

    private static final int MENU_HEIGHT = 80;

    /**
     * Creates the PlayerScreen, which basically just initializes the UI elements
     * 
     * @param game
     */
    public PlayerScreen(Main game) {
        super(game);

        // Initialize the menu, add it to the stage (this stage is used for UI)
        menuView = new PlayerMenu(this);
        stage.addActor(menuView);

        // Create another stage for the view of the game
        mapView = new MapView();

        // Add the second stage to the multiplexer (first one is added in superclass)
        multiplexer.addProcessor(mapView);
    }

    @Override
    public void resize(int width, int height) {
        // Set the size of the menu and the map view
        menuView.setBounds(0, 0, width, MENU_HEIGHT);

        super.resize(width, height);
    }

    @Override
    public void show() {
        super.show();
        // We set the network event listener for this screen only when it is shown
        game.serverConnection.setOnReceiveNetworkEventListener(new NetworkEventListener());
    }

    @Override
    public void render(float delta) {
        // process the game logic

        // clear the screen with the given RGB color (black)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Set the viewport for the UI stage
        // This actually updates a single viewport that is shared by everyone
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        // update the actors
        stage.act(delta);

        // draw the actors
        stage.draw();

        // Set the viewport for the map view
        // Although they update the same viewport, this class allows us to set the x and y offsets
        MapViewport viewport = (MapViewport) mapView.getViewport();
        viewport.update(0, MENU_HEIGHT, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()
                - MENU_HEIGHT);
        mapView.act(delta);
        mapView.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        menuView.dispose();
        mapView.dispose();
    }

    /**
     * Send a request to the server for this player to move. The desired location is pulled from the
     * map view (which is specified by user input).
     */
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
                mapView.updateVisibility(serverConnection.getAccountId(),
                        (VisibilityLayer) event.getData());
            }
        }
    }
}
