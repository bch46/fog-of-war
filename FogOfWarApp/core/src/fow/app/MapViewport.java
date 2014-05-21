package fow.app;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MapViewport extends Viewport {
    private float unitsPerPixel = 1;

    /** Creates a new viewport using a new {@link OrthographicCamera}. */
    public MapViewport () {
        this(new OrthographicCamera());
    }

    public MapViewport (Camera camera) {
        this.camera = camera;
    }

    public void update (int xOffset, int yOffset, int screenWidth, int screenHeight) {
        viewportX = xOffset;
        viewportY = yOffset;
        viewportWidth = screenWidth;
        viewportHeight = screenHeight;
        worldWidth = screenWidth * unitsPerPixel;
        worldHeight = screenHeight * unitsPerPixel;
        super.update(screenWidth, screenHeight, false);
    }
    
    public float getUnitsPerPixel () {
        return unitsPerPixel;
    }

    /** Sets the number of pixels for each world unit. Eg, a scale of 2.5 means there are 2.5 world units for every 1 screen pixel.
     * Default is 1. */
    public void setUnitsPerPixel (float unitsPerPixel) {
        this.unitsPerPixel = unitsPerPixel;
    }
    
}
