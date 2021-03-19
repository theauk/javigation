package bfst21.data_structures;

import bfst21.Osm_Elements.NodeHolder;

import java.util.ArrayList;

public class RTreeNode {
    private float[] coordinates;
    private RTreeNode parent;
    private ArrayList<RTreeNode> children;
    private boolean leaf;
    private NodeHolder[] entries;
    private int minimumEntrySize;
    private int entriesSize;
    private int numberOfEntries;

    public RTreeNode(float xMin, float xMax, float yMin, float yMax, boolean leaf, int minimumEntrySize, int entriesSize) {
        this.coordinates = new float[]{xMin, xMax, yMin, yMax};
        this.leaf = leaf;
        this.entriesSize = entriesSize;
        this.minimumEntrySize = minimumEntrySize;
        this.entries = new NodeHolder[entriesSize];
        children = new ArrayList<>();
        numberOfEntries = 0;
    }

    public float[] getCoordinates() {
        return coordinates;
    }

    // TODO: 3/18/21 should not be necessary? 
    public RTreeNode getParent() {
        return parent;
    }

    public void setParent(RTreeNode r) {
        parent = r;
    }

    public ArrayList<RTreeNode> getChildren() {
        return children;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public NodeHolder[] getNodeHolderEntries() {
        return entries;
    }

    public boolean isFull() {
        return numberOfEntries >= entriesSize;
    }

    public boolean underflow() {
        return numberOfEntries < minimumEntrySize;
    }

    public void addNodeHolderEntry(NodeHolder n) {
        entries[numberOfEntries] = n;
        numberOfEntries++;
    }

    public void addChild(RTreeNode r) {
        children.add(r);
        r.setParent(this);
    }
}
