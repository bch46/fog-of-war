package fow.common;

import java.io.Serializable;

public class MoveRequest implements Serializable {

    private static final long serialVersionUID = 356920880358562529L;
    
    // The ID of the player we want to move
    private final int id;
    
    // The location we want to move this player to
    private final PositionTuple moveLocation;
    
    public MoveRequest(int id, PositionTuple moveLocation) {
        this.id = id;
        this.moveLocation = moveLocation;
    }

    public int getId() {
        return id;
    }

    public PositionTuple getMoveLocation() {
        return moveLocation;
    }

}
