package fow.dmserver;

import java.util.HashMap;

import fow.common.PlayerState;
import fow.common.SceneNode;
import fow.common.VisibilityLayer;

public class GameState {

	enum Status {
		UNINITIALIZED, RUNNING, PAUSED, ENDED
	}

	private Status currentStatus;
	private long startTime;

	/*
	 * Width and height of the level. Should be global and consistent across all
	 * players.
	 */
	private int levelWidth;
	private int levelHeight;

	/*
	 * A tree representing all of the geometry in the game.
	 */
	private SceneNode sceneGraph;

	/*
	 * The list of all players currently in the game
	 */
	private HashMap<Integer, PlayerState> players;
	
	/*
	 * The list of visibility layers for each player
	 */
	private HashMap<Integer, VisibilityLayer> visibilityLayers;

	/*
	 * Reference to the account ID of the DM. Temporarily just the first player
	 * who connects to the server.
	 */
	private int dmId;

	/**
	 * Base constructor with all necessary information to maintain a game.
	 * 
	 * @param width
	 *            width of the level
	 * @param height
	 *            height of the level
	 * @param sceneGraph
	 *            tree representing level's entire geometry (walls)
	 * @param players
	 *            the players in the game
	 */
    public GameState(int levelWidth, int levelHeight, SceneNode sceneGraph,
            HashMap<Integer, PlayerState> players,
            HashMap<Integer, VisibilityLayer> visibilityLayers) {
        this.levelWidth = levelWidth;
        this.levelHeight = levelHeight;
        this.sceneGraph = sceneGraph;
        this.players = players;
        this.visibilityLayers = visibilityLayers;

		this.currentStatus = Status.UNINITIALIZED;
		this.startTime = -1;
		this.dmId = -1;
	}

	/**
	 * Creates an empty level, without players or walls.
	 * 
	 * @param width
	 *            width of the level
	 * @param height
	 *            height of the level
	 */
    public GameState(int width, int height) {
        this(width, height, new SceneNode(null), new HashMap<Integer, PlayerState>(),
                new HashMap<Integer, VisibilityLayer>());
    }

    public GameState() {
        this(0, 0, new SceneNode(null), new HashMap<Integer, PlayerState>(),
                new HashMap<Integer, VisibilityLayer>());
	}

	public Status getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(Status currentStatus) {
		this.currentStatus = currentStatus;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		if (startTime <= 0) {
			throw new IllegalArgumentException();
		}
		this.startTime = startTime;
	}

	public int getLevelWidth() {
		return levelWidth;
	}

	public void setLevelWidth(int levelWidth) {
		if (levelWidth <= 0) {
			throw new IllegalArgumentException();
		}
		this.levelWidth = levelWidth;
	}

	public int getLevelHeight() {
		return levelHeight;
	}

	public void setLevelHeight(int levelHeight) {
		if (levelHeight <= 0) {
			throw new IllegalArgumentException();
		}
		this.levelHeight = levelHeight;
	}

	/**
	 * Populates the scene graph from a file.
	 */
	protected void loadMapFromFile() {
		// TODO implement
	}

	/**
	 * Get a copy of a player's state from its unique ID.
	 * 
	 * @param id
	 *            the player's unique ID
	 * @return the player's state
	 */
	public PlayerState getPlayer(int id) {
		PlayerState player = players.get(Integer.valueOf(id));
		return new PlayerState(player);
	}
	
	/**
	 * Check if a player with the given ID is in this game
	 * @param id the ID of the player to look for
	 * @return whether this player is in the game
	 */
	public boolean containsPlayer(int id) {
		return visibilityLayers.containsKey(id);
	}
	
	/** Add a new player to the game at the default spawn location
	 * 
	 * @param id the ID of the player to add
	 */
	public void addNewPlayer(int id) {
		// TODO figure out where to spawn new players, right now just goes to world center
		players.put(id, new PlayerState(id, levelWidth/2, levelHeight/2));
		PlayerState[] ps = players.values().toArray(new PlayerState[players.size()]);
		VisibilityLayer newVl = new VisibilityLayer(levelWidth, levelHeight, sceneGraph, ps);
		visibilityLayers.put(id, newVl);
	}
	
	/**
	 * Get a representation of a given player's visibility in the game. This is essentially a
	 * subset of the GameState with only the information relevant to what a player can see.
	 * 
	 * @param id the ID of the player
	 * @return the VisibilityLayer for this
	 */
	public VisibilityLayer getPlayerVisibility(int id) {
		return visibilityLayers.get(id);
	}

	public int getDmId() {
		return dmId;
	}

	public void setDmId(int dmId) {
		this.dmId = dmId;
	}
}
