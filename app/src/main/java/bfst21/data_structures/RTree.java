package bfst21.data_structures;

import bfst21.Osm_Elements.NodeHolder;

import java.util.ArrayList;
import java.util.List;

public class RTree {
    private int maxEntries, size;
    private RTreeNode root;

    public RTree(int maxEntries) {
        this.maxEntries = maxEntries;
        this.root = null;
        this.size = 0;
    }

    public RTreeNode getRoot() {
        return root;
    }

    // TODO: 3/17/21 delete both root methods later
    public void setRoot(RTreeNode n) {
        root = n;
    }

    public List<NodeHolder> search(float xMin, float xMax, float yMin, float yMax) {
        float[] searchCoordinates = new float[]{xMin, xMax, yMin, yMax};
        ArrayList<NodeHolder> results = new ArrayList<>();
        search(searchCoordinates, root, results);
        System.out.println(root);
        return results;
    }

    private void search(float[] searchCoordinates, RTreeNode node, ArrayList<NodeHolder> results) {
        if (node.isLeaf()) {
            for (NodeHolder n : node.getNodeHolderEntries()) {
                if (intersects(searchCoordinates, n.getCoordinates())) {
                    results.add(n);
                }
            }
        } else {
            for (RTreeNode r : node.getChildren()) {
                if (intersects(searchCoordinates, r.getCoordinates())) {
                    search(searchCoordinates, r, results);
                }
            }
        }
    }

    public void insert(NodeHolder nodeHolder) {
        RTreeNode selectedLeaf = chooseLeaf(nodeHolder, root);

        if(selectedLeaf.isFull()) {
            //splitNodeExhaustive(selectedLeaf);
        } else {
            selectedLeaf.addNodeHolderEntry(nodeHolder);
        }
    }

    private RTreeNode chooseLeaf(NodeHolder nodeHolder, RTreeNode node) {
        if (node.isLeaf()) {
            return node;
        } else {
            ArrayList<RTreeNode> children = node.getChildren();
            RTreeNode smallestBoundingBoxNode = children.get(0);
            for (int i = 1; i < node.getChildren().size(); i++) {
                if (getNewBoundingBoxArea(nodeHolder, children.get(i)) < getNewBoundingBoxArea(nodeHolder, smallestBoundingBoxNode)) {
                    smallestBoundingBoxNode = children.get(i);
                }
            }
            return chooseLeaf(nodeHolder, smallestBoundingBoxNode);
        }
    }

    /*private RTreeNode splitNodeExhaustive(RTreeNode leaf) {

    }

    private RTreeNode splitNodeQuadraticCost(RTreeNode leaf) {

    }

    private RTreeNode splitNodeLinearCost(RTreeNode leaf) {

    }*/

    private void adjustTree() {

    }

    private float getNewBoundingBoxArea(NodeHolder nodeHolder, RTreeNode node) {
        float[] newCoordinates = new float[nodeHolder.getCoordinates().length];

        for (int i = 0; i < nodeHolder.getCoordinates().length; i++) {
            if (i % 2 == 0) {
                newCoordinates[i] = Math.min(nodeHolder.getCoordinates()[i], node.getCoordinates()[i]);
            } else {
                newCoordinates[i] = Math.max(nodeHolder.getCoordinates()[i], node.getCoordinates()[i]);
            }
        }
        float area = 1;
        for (int j = 0; j < newCoordinates.length - 1; j += 2) {
            area *= (newCoordinates[j + 1] - newCoordinates[j]);
        }
        return area;
    }

    public Boolean intersects(float[] coordinates1, float[] coordinates2) {
        for (int i = 0; i < coordinates1.length; i += 2) {
            if (doesNotIntersect(coordinates1[i], coordinates2[i + 1])) {
                return false;
            } else if (doesNotIntersect(coordinates2[i], coordinates1[i + 1])) {
                return false;
            }
        }
        return true;
    }

    private boolean doesNotIntersect(float minCoordinateFirstElement, float maxCoordinateSecondElement) {
        return minCoordinateFirstElement >= maxCoordinateSecondElement;
    }

    /*private Boolean contains(Element outerElement, Element innerElement) {
        if (outerElement.getxMin() > innerElement.getxMin()) {
            return false;
        } else if (outerElement.getxMax() < outerElement.getxMax()) {
            return false;
        } else if (outerElement.getyMin() > innerElement.getyMin()) {
            return false;
        } else if (outerElement.getyMax() < innerElement.getyMax()) {
            return false;
        }
        return true;
    }*/
}
