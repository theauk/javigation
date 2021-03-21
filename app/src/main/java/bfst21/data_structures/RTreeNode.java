package bfst21.data_structures;

import bfst21.Osm_Elements.NodeHolder;

import java.util.ArrayList;

public class RTreeNode {
    private float[] coordinates;
    private ArrayList<RTreeNode> children;
    private boolean leaf;
    private ArrayList<NodeHolder> entries;
    private int minimumEntrySize, maximumChildren;
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

    public void updateCoordinate(int index, float newCoordinate) {
        coordinates[index] = newCoordinate;
    }

    public ArrayList<RTreeNode> getChildren() {
        return children;
    }

    public ArrayList<NodeHolder> getNodeHolderEntries() {
        return entries;
    }

    public boolean overflow() {
        return children.size() >= maximumChildren;
    }

    public boolean underflow() {
        return children.size() < minimumEntrySize;
    }

    public void addNodeHolderEntry(NodeHolder n) {
        entries.add(n);
    }

    public void addChild(RTreeNode r) {
        children.add(r);
        r.addParent(this);
    }

    public void addChildren(ArrayList<RTreeNode> children) {
        this.children.addAll(children);
    }

    public void removeChildren() {
        children.clear();
    }

    private void addParent(RTreeNode rTreeNode) {
        parent = rTreeNode;
    }

    public RTreeNode getParent() {
        return parent;
    }
}
