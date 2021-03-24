package bfst21.data_structures;

import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.NodeHolder;

import java.util.ArrayList;

public class RTreeNode {
    private float[] coordinates;
    private ArrayList<RTreeNode> children;
    private boolean leaf;
    private ArrayList<Element> entries;
    private int minimumEntrySize, maximumChildren;
    private RTreeNode parent;
    public int id;

    public RTreeNode(float[] coordinates, boolean leaf, int minimumChildren, int maximumChildren, RTreeNode parent, int id) { // TODO: 3/22/21 delete id
        this.coordinates = coordinates;
        this.leaf = leaf;
        this.maximumChildren = maximumChildren;
        this.minimumEntrySize = minimumChildren;
        this.entries = new ArrayList<>();
        this.parent = parent;
        children = new ArrayList<>();
        this.id = id;
        System.out.println("Created new node with id: " + id);
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setIsLeaf(boolean value) {
        leaf = value;
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
        System.out.println("Check overflow: Current number of children " + children.size() + " max children is: " + maximumChildren);
        return children.size() > maximumChildren;
    }

    public boolean underflow() {
        return children.size() < minimumEntrySize;
    }

    public void addElementEntry(Element e) {
        System.out.println("Added nodeHolderEntry to: " + id);
        entries.add(e);
    }

    public void addChild(RTreeNode r) {
        children.add(r);
        System.out.println("Added child: " + r.id + " to " + id);
        System.out.println("Currently " + children.size() + " added child(ren)");
        r.setParent(this); // TODO: 3/22/21 do i need this
    }

    public void addChildren(ArrayList<RTreeNode> children) {
        this.children.addAll(children);
    }

    public void removeChildren() {
        children.clear();
    }

    public void setParent(RTreeNode rTreeNode) {
        System.out.println(id + "s new parent is: " + rTreeNode.id);
        parent = rTreeNode;
    }

    public RTreeNode getParent() {
        return parent;
    }
}
