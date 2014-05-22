package fow.common;

import java.io.Serializable;

public class PositionTuple implements Serializable {

	private static final long serialVersionUID = -8586611936509133031L;

	public final int x;
	public final int y;

	/**
	 * Create a tuple representing a point in 2-dimensional space.
	 * 
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 */
	public PositionTuple(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Convenience duplication constructor
	 * 
	 * @param pos
	 *            tuple to duplicate
	 */
	public PositionTuple(PositionTuple pos) {
		this(pos.x, pos.y);
	}
	
	@Override
	public String toString() {
	    return "{x: " + x + ", y: " + y + "}";
	}
}