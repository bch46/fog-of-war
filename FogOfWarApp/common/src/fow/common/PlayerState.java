package fow.common;

import java.io.Serializable;

/**
 * Object to represent anything about a player's persistent state in the game. It is sent over the
 * network, so any information not needed by the client should be marked transitive or otherwise
 * filtered out.
 * 
 * @author Ben
 * 
 */
public class PlayerState implements Serializable {

    private static final long serialVersionUID = 6946985915585070163L;

    /*
     * The ID associated with this player. No other player in the game should share this ID. The PC
     * app should know its own player's ID.
     */
    public int id;

    /*
     * The list of positions that this player has been, starting from oldest (index 0) to newest.
     */
    public PositionTuple[] path;

    /**
     * Creates an object to represent a single player's state.
     * 
     * @param id the player's unique ID
     * @param path the list of positions this player has been
     */
    public PlayerState(int id, PositionTuple[] path) {
        this.id = id;
        this.path = path;
    }

    /**
     * Create a player character at a given position in the game world.
     * 
     * @param id a unique identifier for this particular player character
     * @param x initial x-coordinate in the game world
     * @param y initial y-coordinate in the game world
     */
    public PlayerState(int id, int x, int y) {
        this(id, new PositionTuple[] {new PositionTuple(x, y)});
    }

    /**
     * Convenience duplication constructor
     * 
     * @param player the PlayerState to duplicate
     */
    public PlayerState(PlayerState player) {
        this(player.id, new PositionTuple[player.path.length]);
        for (int i = 0; i < player.path.length; i++) {
            this.path[i] = new PositionTuple(player.path[i]);
        }
    }

    /**
     * Change this player's current position, adding the new position it to the path history
     * 
     * @param newPosition
     */
    public void changePosition(PositionTuple newPosition) {
        // Create a new array to hold the old one, plus another element
        PositionTuple[] newPath = new PositionTuple[path.length + 1];

        // Add new element at front
        newPath[0] = newPosition;

        // Fill in the old path
        for (int j = 0; j < path.length; j++) {
            newPath[j + 1] = new PositionTuple(path[j]);
        }

        this.path = newPath;
    }

    /**
     * @return this player's current position
     */
    public PositionTuple getCurrentPosition() {
        return path[0];
    }

}
