package fow.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

import fow.common.PlayerState;
import fow.common.PositionTuple;
import fow.common.VisibilityLayer;

public class MapView extends Stage {

    // Camera constants for pan and zoom speed
    private static final int CAM_PAN_AMT = 3;
    private static final float CAM_ZOOM_AMT = 0.05f;

    // Reference to this stage's camera, but cast to an OrthographicCamera
    private OrthographicCamera camera;

    // The texture we use for any character on the screen. Future implementations would probably
    // expand this to different textures for different characters
    private final Texture texture;

    // This is essentially the state, as provided by the server
    protected VisibilityLayer visibility;

    // This player's index in the VisibilityLayer's players array, for convenience
    private int myIndex;

    /*
     * Whether or not the character's avatar is currently being dragged around
     */
    private boolean dragging;

    // Used for moving an avatar around the screen
    private Vector2 dragStartPoint;
    private Vector2 dragEndPoint;

    // Used for sliding and zooming the camera
    private Vector2 camTranslation;
    private float zoomFactor;
    
    private PositionTuple previewLocation;

    /**
     * Default constructor, initializes fields to default values
     */
    public MapView() {
        super();

        Viewport viewport = new MapViewport();
        setViewport(viewport);

        camera = (OrthographicCamera) getCamera();

        texture = new Texture(Gdx.files.internal("gnome.gif"));

        // So it will throw out of bounds exception if used improperly
        myIndex = -1;

        dragging = false;
        dragStartPoint = new Vector2();
        dragEndPoint = new Vector2();
        camTranslation = new Vector2();
        zoomFactor = 0f;
    }

    /**
     * Update the VisibilityLayer that represents this player's view.
     * 
     * @param accId the ID of the player who this VisibilityLayer belongs to
     * @param visibility
     */
    public void updateVisibility(int accId, VisibilityLayer visibility) {
        if (visibility.equals(this.visibility)) {
            return;
        }
        this.visibility = visibility;
        for (int i = 0; i < visibility.getPlayers().length; i++) {
            PlayerState p = visibility.getPlayers()[i];
            if (p.id == accId) {
                myIndex = i;
            }
        }

        // This player's character should be first in the list
        PositionTuple pos = visibility.getPlayerPosition(myIndex);

        // Try to center the camera on the player
        camera.position.set(pos.x, pos.y, 0);
    }
    
    public void updatePreviewLocation(PositionTuple previewLocation) {
        this.previewLocation = previewLocation;
    }

    public PositionTuple getCurrentPlayerPosition() {
        return visibility.getPlayerPosition(myIndex);
    }

    @Override
    public void draw() {
        super.draw();

        // Translate camera if necessary
        camera.translate(camTranslation);

        // Zoom camera if necessary, don't let it zoom too far in or we get backwards world
        if (camera.zoom + zoomFactor > CAM_ZOOM_AMT) {
            camera.zoom += zoomFactor;
        }

        // Only attempt to draw the world if we have a visibility layer to work from
        if (visibility != null) {
            Batch batch = getSpriteBatch();
            batch.begin();
            
            for (int i = 0; i < visibility.getNumPlayers(); i++) {
                PositionTuple currentPos = visibility.getPlayerPosition(i);

                int dx = currentPos.x - texture.getWidth() / 2;
                int dy = currentPos.y - texture.getHeight() / 2;

                if (i == myIndex && dragging) {
                    dx += dragEndPoint.x - dragStartPoint.x;
                    dy += dragEndPoint.y - dragStartPoint.y;
                }

                batch.draw(texture, dx, dy, texture.getHeight(), texture.getWidth());
            }
            
            if (previewLocation != null) {
                int dx = previewLocation.x - texture.getWidth() / 2;
                int dy = previewLocation.y - texture.getHeight() / 2;
                
                batch.setColor(Color.RED);
                batch.draw(texture, dx, dy, texture.getHeight(), texture.getWidth());
                batch.setColor(Color.WHITE);
            }

            batch.end();
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (visibility == null) {
            return false;
        }

        int halfWidth = texture.getWidth() / 2;
        int halfHeight = texture.getHeight() / 2;

        Vector2 worldCoords = screenToStageCoordinates(new Vector2(screenX, screenY));
        PositionTuple curPos = visibility.getPlayerPosition(myIndex);

        if (worldCoords.x >= curPos.x - halfWidth && worldCoords.x <= curPos.x + halfWidth
                && worldCoords.y >= curPos.y - halfHeight && worldCoords.y <= curPos.y + halfHeight) {
            dragging = true;
            dragStartPoint.set(worldCoords);
            dragEndPoint.set(worldCoords);
        }

        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (visibility == null) {
            return false;
        }

        if (dragging) {
            dragEndPoint.set(screenToStageCoordinates(new Vector2(screenX, screenY)));
        }

        return super.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (visibility == null) {
            return false;
        }

        if (dragging) {
            PositionTuple curPos = visibility.getPlayerPosition(myIndex);
            int newX = curPos.x + (int) (dragEndPoint.x - dragStartPoint.x);
            int newY = curPos.y + (int) (dragEndPoint.y - dragStartPoint.y);
            visibility.setPlayerPosition(myIndex, new PositionTuple(newX, newY));

            dragStartPoint.set(0, 0);
            dragEndPoint.set(0, 0);
            dragging = false;
        }

        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean keyDown(int keyCode) {
        if (visibility == null) {
            return false;
        }

        if (keyCode == Input.Keys.A) {
            zoomFactor += CAM_ZOOM_AMT;
        }
        if (keyCode == Input.Keys.Q) {
            zoomFactor -= CAM_ZOOM_AMT;
        }
        if (keyCode == Input.Keys.LEFT) {
            camTranslation.add(-CAM_PAN_AMT, 0);
        }
        if (keyCode == Input.Keys.RIGHT) {
            camTranslation.add(CAM_PAN_AMT, 0);
        }
        if (keyCode == Input.Keys.DOWN) {
            camTranslation.add(0, -CAM_PAN_AMT);
        }
        if (keyCode == Input.Keys.UP) {
            camTranslation.add(0, CAM_PAN_AMT);
        }
        return super.keyDown(keyCode);
    }

    @Override
    public boolean keyUp(int keyCode) {
        if (visibility == null) {
            return false;
        }

        if (keyCode == Input.Keys.A) {
            zoomFactor -= CAM_ZOOM_AMT;
        }
        if (keyCode == Input.Keys.Q) {
            zoomFactor += CAM_ZOOM_AMT;
        }
        if (keyCode == Input.Keys.LEFT) {
            camTranslation.add(CAM_PAN_AMT, 0);
        }
        if (keyCode == Input.Keys.RIGHT) {
            camTranslation.add(-CAM_PAN_AMT, 0);
        }
        if (keyCode == Input.Keys.DOWN) {
            camTranslation.add(0, CAM_PAN_AMT);
        }
        if (keyCode == Input.Keys.UP) {
            camTranslation.add(0, -CAM_PAN_AMT);
        }
        return super.keyUp(keyCode);
    }
}
