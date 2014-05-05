package fow.common;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Original code courtesy of
 * https://code.google.com/p/yet-another-tree-structure/
 */
public class SceneNode implements Iterable<SceneNode>, Serializable {

	private static final long serialVersionUID = -8970813132810776520L;
	
	public GeometryEntity data;
	public SceneNode parent;
	public List<SceneNode> children;

	public boolean isRoot() {
		return parent == null;
	}

	public boolean isLeaf() {
		return children.size() == 0;
	}

	private List<SceneNode> elementsIndex;

	public SceneNode(GeometryEntity data) {
		this.data = data;
		this.children = new LinkedList<SceneNode>();
		this.elementsIndex = new LinkedList<SceneNode>();
		this.elementsIndex.add(this);
	}

	public SceneNode addChild(GeometryEntity child) {
		SceneNode childNode = new SceneNode(child);
		childNode.parent = this;
		this.children.add(childNode);
		this.registerChildForSearch(childNode);
		return childNode;
	}

	public int getLevel() {
		if (this.isRoot())
			return 0;
		else
			return parent.getLevel() + 1;
	}

	private void registerChildForSearch(SceneNode node) {
		elementsIndex.add(node);
		if (parent != null)
			parent.registerChildForSearch(node);
	}

	public SceneNode findTreeNode(Comparable<Serializable> cmp) {
		for (SceneNode element : this.elementsIndex) {
			Serializable elData = element.data;
			if (cmp.compareTo(elData) == 0)
				return element;
		}

		return null;
	}

	@Override
	public String toString() {
		return data != null ? data.toString() : "[data null]";
	}

	@Override
	public Iterator<SceneNode> iterator() {
		SceneNodeIter iter = new SceneNodeIter(this);
		return iter;
	}

}