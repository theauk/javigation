package bfst21.data_structures;

import bfst21.Osm_Elements.Element;

import java.util.ArrayList;

public class RTreeNode {
    private float[] coordinates;
    private RTreeNode parent;
    private ArrayList<RTreeNode> children;
    private boolean leaf;
    private Element[] elements;
    private int elementsSize;
    private int numberOfElements;

    public RTreeNode(float xMin, float xMax, float yMin, float yMax, boolean leaf, int elementsSize) {
        this.coordinates = new float[]{xMin, xMax, yMin, yMax};
        this.leaf = leaf;
        this.elementsSize = elementsSize;
        this.elements = new Element[elementsSize];
        children = new ArrayList<>();
        numberOfElements = 0;
    }

    public float[] getCoordinates() {
        return coordinates;
    }

    public RTreeNode getParent() {
        return parent;
    }

    public ArrayList<RTreeNode> getChildren() {
        return children;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public Element[] getElements() {
        return elements;
    }

    public boolean isFull() {
        return numberOfElements == elementsSize;
    }

    public void addElement(Element e) {
        elements[numberOfElements] = e;
        numberOfElements++;
    }
}
