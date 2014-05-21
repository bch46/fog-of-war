package fow.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import fow.common.PositionTuple;
import fow.common.VisibilityLayer;

public class MapView {

    private final Texture texture;

    // This is essentially the state, as provided by the server
    protected VisibilityLayer visibility;

    /*
     * Camera fields, storing the dimensions of the camera and its location in world coords.
     * Location maps to the origin of the camera (bottom left corner)
     */
    private Rectangle camViewport;
    private float camZoom;

    /*
     * Represents the current position of your character in world coordinates. This can change if
     * the user drags their character's avatar around.
     */
    private int x;
    private int y;

    /*
     * Whether or not the character's avatar is currently being dragged around
     */
    private boolean dragged;
    
    private boolean untouched;

    /**
     * Default constructor, initializes fields to default values
     */
    public MapView() {
        texture = new Texture(Gdx.files.internal("gnome.gif"));
        camViewport = new Rectangle();
        camZoom = 0f;

        x = 0;
        y = 0;

        dragged = false;
        untouched = false;
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
        setCameraTo(x, y);
    }

    /**
     * Set the width and height of the camera's viewport. This should be determined by the size of
     * the screen, minus the space taken up by the menu.
     * 
     * @param width
     * @param height
     */
    public void setCameraBounds(int width, int height) {
        this.camViewport.setWidth(width).setHeight(height);
    }

    /**
     * Tries to center the camera around the point x, y Takes world bounds into consideration
     * 
     * @param x coordinate
     * @param y coordinate
     */
    private void setCameraTo(int x, int y) {
        camViewport.x = x - (int) (camViewport.width / 2);
        camViewport.y = y - (int) (camViewport.height / 2);
        if (camViewport.x < 0) {
            camViewport.x = 0;
        } else if (camViewport.x + camViewport.width > visibility.getLevelWidth()) {
            camViewport.x = visibility.getLevelWidth() - camViewport.width;
        }
        if (camViewport.y < 0) {
            camViewport.y = 0;
        } else if (camViewport.y + camViewport.height > visibility.getLevelHeight()) {
            camViewport.y = visibility.getLevelHeight() - camViewport.height;
        }
    }

    /**
     * Sets the amount of zoom for the camera. Zoom is currently unimplemented.
     * 
     * @param sliderValue zoom value, 1 = full scale, 0 = fully zoomed out
     */
    public void setCameraZoom(float sliderValue) {
        this.camZoom = sliderValue;
    }

    public PositionTuple getCurrentPlayerPosition() {
        return new PositionTuple(x, y);
    }

    public void render(AbstractScreen screen) {
        if (visibility != null) {
            handleInput(screen);
            
            if (dragged) {
                int tempy = ((int) screen.stage.getHeight()) - Gdx.input.getY(0);
                if (tempy > 100) {
                    x = Gdx.input.getX(0) + (int) camViewport.x;
                    y = tempy + (int) camViewport.y;
                }
                System.out.println("x = " + x + ", y = " + y);
            }


            int dx = x - texture.getWidth() / 2 - (int) camViewport.x;
            int dy = y - texture.getHeight() / 2 - (int) camViewport.y;

            SpriteBatch batch = screen.getBatch();
            batch.begin();
            batch.draw(texture, dx, dy, texture.getHeight(), texture.getWidth());
            batch.end();
        }
    }

    private void handleInput(AbstractScreen screen) {
        if (Gdx.input.isTouched(0)) {
            
            int halfWidth = texture.getWidth() / 2;
            int halfHeight = texture.getHeight() / 2;

            int tempX = Gdx.input.getX(0);
            int tempY = ((int) screen.stage.getHeight()) - Gdx.input.getY(0);
            tempX += (int) camViewport.x;
            tempY += (int) camViewport.y;

            if (tempX >= x - halfWidth && tempX <= x + halfWidth && tempY >= y - halfHeight
                    && tempY <= y + halfHeight && untouched) {
                dragged = true;
            }
            
            untouched = false;
        } else {
            dragged = false;
            untouched = true;
        }
    }
}
