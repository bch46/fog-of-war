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
    
    // The texture we use for any character on the screen. Future implementations would probably
    // expand this to different textures for different characters
    private final Texture texture;

    // This is essentially the state, as provided by the server
    protected VisibilityLayer visibility;

    /*
     * Represents the current position of your character in world coordinates. This can change if
     * the user drags their character's avatar around.
     */
    private int x;
    private int y;

    /*
     * Whether or not the character's avatar is currently being dragging around
     */
    private boolean dragging;

    private boolean untouched;
    
    private Vector2 translation;
    private float zoomFactor;

    /**
     * Default constructor, initializes fields to default values
     */
    public MapView() {
        super();

        Viewport viewport = new MapViewport();
        setViewport(viewport);

        texture = new Texture(Gdx.files.internal("gnome.gif"));

        x = 0;
        y = 0;

        dragging = false;
        untouched = false;
        translation = new Vector2();
        zoomFactor = 0f;
    }

    /**
     * Update the VisibilityLayer that represents this player's view
     * 
     * @param visibility
     */
    public void updateVisibility(VisibilityLayer visibility) {
        this.visibility = visibility;

        // The first player in the list should be this player's character
        x = visibility.getPlayers()[0].path[0].x;
        y = visibility.getPlayers()[0].path[0].y;

        // Try to center the camera on the player
        getCamera().position.set(x, y, 0);
    }

    public PositionTuple getCurrentPlayerPosition() {
        return new PositionTuple(x, y);
    }

    @Override
    public void draw() {
        super.draw();
        
        // Translate camera if necessary
        OrthographicCamera cam = (OrthographicCamera) getCamera();
        cam.translate(translation);
        
        // Zoom camera if necessary, don't let it zoom too far in or we get backwards world
        if (cam.zoom + zoomFactor > CAM_ZOOM_AMT) {
            cam.zoom += zoomFactor;
        }
        
        // Only attempt to draw the world if we have a visibility layer to work from
        if (visibility != null) {
            int dx = x - texture.getWidth() / 2;
            int dy = y - texture.getHeight() / 2;

            Batch batch = getSpriteBatch();
            batch.begin();
            batch.draw(texture, dx, dy, texture.getHeight(), texture.getWidth());
            batch.end();
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        int halfWidth = texture.getWidth() / 2;
        int halfHeight = texture.getHeight() / 2;

        Vector2 stageCoords = screenToStageCoordinates(new Vector2(screenX, screenY));

        if (stageCoords.x >= x - halfWidth && stageCoords.x <= x + halfWidth
                && stageCoords.y >= y - halfHeight && stageCoords.y <= y + halfHeight && untouched) {
            dragging = true;
        }
        untouched = false;

        System.out.println("x = " + screenX + ", y = " + screenY + ", dragging: " + dragging);
        
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (dragging) {
            Vector2 stageCoords = screenToStageCoordinates(new Vector2(screenX, screenY));
            x = (int) stageCoords.x;
            y = (int) stageCoords.y;
        }

        return super.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        dragging = false;
        untouched = true;
        return super.touchUp(screenX, screenY, pointer, button);
    }
    
    @Override
    public boolean keyDown (int keyCode) {
        if(keyCode == Input.Keys.A) {
            zoomFactor += CAM_ZOOM_AMT;
        }
        if(keyCode ==  Input.Keys.Q) {
            zoomFactor -= CAM_ZOOM_AMT;
        }
        if(keyCode ==  Input.Keys.LEFT) {
                translation.add(-CAM_PAN_AMT, 0);
        }
        if(keyCode ==  Input.Keys.RIGHT) {
                translation.add(CAM_PAN_AMT, 0);
        }
        if(keyCode ==  Input.Keys.DOWN) {
                translation.add(0, -CAM_PAN_AMT);
        }
        if(keyCode ==  Input.Keys.UP) {
                translation.add(0, CAM_PAN_AMT);
        }
        return super.keyDown(keyCode);
    }
    
    @Override
    public boolean keyUp (int keyCode) {
        if(keyCode == Input.Keys.A) {
            zoomFactor -= CAM_ZOOM_AMT;
        }
        if(keyCode ==  Input.Keys.Q) {
            zoomFactor += CAM_ZOOM_AMT;
        }
        if(keyCode ==  Input.Keys.LEFT) {
                translation.add(CAM_PAN_AMT, 0);
        }
        if(keyCode ==  Input.Keys.RIGHT) {
                translation.add(-CAM_PAN_AMT, 0);
        }
        if(keyCode ==  Input.Keys.DOWN) {
                translation.add(0, CAM_PAN_AMT);
        }
        if(keyCode ==  Input.Keys.UP) {
                translation.add(0, -CAM_PAN_AMT);
        }
        return super.keyUp(keyCode);
    }
}
