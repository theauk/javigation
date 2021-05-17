package bfst21.data_structures;

import bfst21.Osm_Elements.Element;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class RTreeNode implements Serializable {
    @Serial private static final long serialVersionUID = 6595066254175712095L;

    private float[] coordinates;
    private final ArrayList<RTreeNode> children;
    private final boolean leaf;
    private final ArrayList<Element> entries;
    private final int minimumEntrySize;
    private final int maximumChildren;
    private RTreeNode parent;

    public RTreeNode(float[] coordinates, boolean leaf, int minimumChildren, int maximumChildren, RTreeNode parent) {
        this.coordinates = coordinates;
        this.leaf = leaf;
        this.maximumChildren = maximumChildren;
        this.minimumEntrySize = minimumChildren;
        this.entries = new ArrayList<>();
        this.parent = parent;
        children = new ArrayList<>();
    }

    public boolean isLeaf() {
        return leaf;
    }

    public float[] getCoordinates() {
        return coordinates;
    }

    public void updateCoordinate(float[] newCoordinates) {
        coordinates = newCoordinates;
    }

    public ArrayList<RTreeNode> getChildren() {
        return children;
    }

    public ArrayList<Element> getElementEntries() {
        return entries;
    }

    public boolean overflow() {
        return children.size() > maximumChildren;
    }

    public void addElementEntry(Element e) {
        entries.add(e);
    }

    public void addChild(RTreeNode r) {
        children.add(r);
        r.setParent(this);
    }

    public void addChildren(ArrayList<RTreeNode> children) {
        this.children.addAll(children);
    }

    public void removeChildren() {
        children.clear();
    }

    public RTreeNode getParent() {
        return parent;
    }

    public void setParent(RTreeNode rTreeNode) {
        parent = rTreeNode;
    }
}
