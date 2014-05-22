package fow.common;

import java.io.Serializable;

/**
 * Serializable object to store all of the state necessary to display a single
 * visibility layer. This is the main object that gets transferred over the
 * network to update a PC app's view. Each player's visibility in the DM app is
 * also rendered using an instance of this class.
 * 
 * @author Ben
 * 
 */
public class VisibilityLayer implements Serializable {

	private static final long serialVersionUID = 8824713330677329032L;

	/*
	 * Width and height of the level. Should be global and consistent across all
	 * players.
	 */
	private int levelWidth;
	private int levelHeight;

	/*
	 * An implementation of a tree data structure, representing the relevant
	 * (i.e. visible) world geometry. Hidden geometry should be culled by the DM
	 * server and not included here.
	 */
	private SceneNode sceneGraph;

	/*
	 * The list of all players relevant to this player's visibility. This
	 * includes at minimum the player itself, any of its familiars, and any
	 * other characters who exist within visibility.
	 */
	private PlayerState[] players;

	/**
	 * Base constructor with all necessary information for a valid level.
	 * 
	 * @param width
	 *            width of the level
	 * @param height
	 *            height of the level
	 * @param sceneGraph
	 *            tree representing level's visible geometry (walls)
	 * @param players
	 *            the players in view of this visibility layer
	 */
	public VisibilityLayer(int width, int height, SceneNode sceneGraph,
			PlayerState[] players) {
		this.levelWidth = width;
		this.levelHeight = height;
		this.sceneGraph = sceneGraph;
		this.players = players;
	}

	public VisibilityLayer(int width, int height) {
		this(width, height, new SceneNode(null), new PlayerState[0]);
	}

	public VisibilityLayer() {
		this(0, 0, new SceneNode(null), new PlayerState[0]);
	}

	public int getLevelWidth() {
		return levelWidth;
	}

	public void setLevelWidth(int levelWidth) {
		this.levelWidth = levelWidth;
	}

	public int getLevelHeight() {
		return levelHeight;
	}

	public void setLevelHeight(int levelHeight) {
		this.levelHeight = levelHeight;
	}

	public SceneNode getSceneGraph() {
		return sceneGraph;
	}

	public void setSceneGraph(SceneNode sceneGraph) {
		this.sceneGraph = sceneGraph;
	}

	public PlayerState[] getPlayers() {
		return players;
	}

	public void setPlayers(PlayerState[] players) {
		this.players = players;
	}
	
	public int getNumPlayers() {
	    return players.length;
	}
}
