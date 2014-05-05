package fow.common;

import java.io.Serializable;

/**
 * This is the superclass for any game entity that will be included in the scene
 * graph. Currently only planning to support walls, represented by line segments
 * and Bezier curves.
 * 
 * @author Ben
 * 
 */
public abstract class GeometryEntity implements Serializable {

	private static final long serialVersionUID = 4084155550251316650L;

	/*
	 * Position of the entity in the world. Should not exceed GameGeometry's
	 * levelWidth or levelHeight
	 */
	public int x;
	public int y;

	/*
	 * The width and height of the axis-aligned bounding box, used for placing
	 * into scene graph buckets. Neither width nor height should exceed 2 times
	 * the bucket's width or height.
	 */
	public int width;
	public int height;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setY(int y) {
		this.y = y;
	}

}
