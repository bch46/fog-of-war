package fow.app;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import fow.app.network.HandshakeListener;
import fow.app.network.ServerConnection;
import fow.common.NetworkEvent;
import fow.common.NetworkEvent.Type;
import fow.common.PositionTuple;
import fow.common.VisibilityLayer;

public class MasterScreen extends AbstractScreen {

    protected final MasterMenu menuView;
    protected final MapView mapView;

    private static final int MENU_HEIGHT = 80;
    
    private HashMap<Integer, VisibilityLayer> visibilities;
    private HashMap<Integer, PositionTuple> moveRequests;
    
    // The id of the player whose visibility we want to show
    private Integer selectedPlayer;

    /**
     * Creates the PlayerScreen, which basically just initializes the UI elements
     * 
     * @param game
     */
    public MasterScreen(Main game) {
        super(game);

        // Initialize the menu, add it to the stage (this stage is used for UI)
        menuView = new MasterMenu(this);
        stage.addActor(menuView);

        // Create another stage for the view of the game
        mapView = new MapView();

        // Add the second stage to the multiplexer (first one is added in superclass)
        multiplexer.addProcessor(mapView);

        visibilities = new HashMap<Integer, VisibilityLayer>();
        moveRequests = new HashMap<Integer, PositionTuple>();
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
        
        // Only draw the map view if there is a visibility for the currently selected player
        if (visibilities.containsKey(selectedPlayer)) {
            mapView.updateVisibility(selectedPlayer.intValue(), visibilities.get(selectedPlayer));
            
            // Set the viewport for the map view
            // Although they update the same viewport, this class allows us to set the x and y offsets
            MapViewport viewport = (MapViewport) mapView.getViewport();
            viewport.update(0, MENU_HEIGHT, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()
                    - MENU_HEIGHT);
            mapView.act(delta);
            mapView.draw(); 
        }
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
        game.serverConnection.sendEvent(evt);
    }

    private class NetworkEventListener extends HandshakeListener {
        @Override
        public void onReceiveNetworkEvent(final ServerConnection serverConnection,
                final NetworkEvent event) {
            super.onReceiveNetworkEvent(serverConnection, event);
            if (event.getType().equals(Type.REQUEST_MOVE)) {
                moveRequests = (HashMap<Integer, PositionTuple>) event.getData();
                menuView.updateNumPendingRequests(moveRequests.size());
                menuView.setButtonsVisible(moveRequests.containsKey(selectedPlayer));
            }
            if (event.getType().equals(Type.UPDATE_VISIBILITY)) {
                visibilities = (HashMap<Integer, VisibilityLayer>) event.getData();
                menuView.updateVisibilities(visibilities);
                if (selectedPlayer == null) {
                    System.out.println("SELECTING FIRST PLAYER");
                    menuView.selectFirstPlayer();
                }
            }
        }
    }
	
	public void onMoveRequestDecision(boolean approved) {
	    // TODO
	}

	public void setSelectedPlayer(Integer id) {
	    selectedPlayer = id;
	    menuView.setButtonsVisible(moveRequests.containsKey(id));
	}
}
