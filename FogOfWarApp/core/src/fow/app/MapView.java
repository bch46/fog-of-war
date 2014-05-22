package fow.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

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

    /*
     * Whether or not the character's avatar is currently being dragging around
     */
    private boolean dragging;

    private Vector2 dragStartPoint;
    private Vector2 dragEndPoint;

    private Vector2 camTranslation;
    private float zoomFactor;

    /**
     * Default constructor, initializes fields to default values
     */
    public MapView() {
        super();

        Viewport viewport = new MapViewport();
        setViewport(viewport);

        camera = (OrthographicCamera) getCamera();

        texture = new Texture(Gdx.files.internal("gnome.gif"));

        dragging = false;
        dragStartPoint = new Vector2();
        dragEndPoint = new Vector2();
        camTranslation = new Vector2();
        zoomFactor = 0f;
    }

    /**
     * Update the VisibilityLayer that represents this player's view
     * 
     * @param visibility
     */
    public void updateVisibility(VisibilityLayer visibility) {
        this.visibility = visibility;

        // This player's character should be first in the list
        PositionTuple pos = visibility.getPlayerPosition(0);

        // Try to center the camera on the player
        camera.position.set(pos.x, pos.y, 0);
    }

    public PositionTuple getCurrentPlayerPosition() {
        return visibility.getPlayerPosition(0);
    }

    @Override
    public void draw() {
        super.draw();

        // Translate camera if necessary
        OrthographicCamera cam = (OrthographicCamera) getCamera();
        cam.translate(camTranslation);

        // Zoom camera if necessary, don't let it zoom too far in or we get backwards world
        if (cam.zoom + zoomFactor > CAM_ZOOM_AMT) {
            cam.zoom += zoomFactor;
        }

        // Only attempt to draw the world if we have a visibility layer to work from
        if (visibility != null) {
            for (int i = 0; i < visibility.getNumPlayers(); i++) {
                PositionTuple currentPos = visibility.getPlayerPosition(i);

                int dx = currentPos.x - texture.getWidth() / 2;
                int dy = currentPos.y - texture.getHeight() / 2;

                if (dragging) {
                    dx += dragEndPoint.x - dragStartPoint.x;
                    dy += dragEndPoint.y - dragStartPoint.y;
                }

                Batch batch = getSpriteBatch();
                batch.begin();
                batch.draw(texture, dx, dy, texture.getHeight(), texture.getWidth());
                batch.end();
            }
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        int halfWidth = texture.getWidth() / 2;
        int halfHeight = texture.getHeight() / 2;

        Vector2 worldCoords = screenToStageCoordinates(new Vector2(screenX, screenY));
        PositionTuple curPos = visibility.getPlayerPosition(0);

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
        if (dragging) {
            dragEndPoint.set(screenToStageCoordinates(new Vector2(screenX, screenY)));
        }

        return super.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (dragging) {
            PositionTuple curPos = visibility.getPlayerPosition(0);
            int newX = curPos.x + (int) (dragEndPoint.x - dragStartPoint.x);
            int newY = curPos.y + (int) (dragEndPoint.y - dragStartPoint.y);
            visibility.setPlayerPosition(0, new PositionTuple(newX, newY));
            
            dragStartPoint.set(0, 0);
            dragEndPoint.set(0, 0);
            dragging = false;
        }
        
        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean keyDown(int keyCode) {
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
