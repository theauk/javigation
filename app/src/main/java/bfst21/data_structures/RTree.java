package bfst21.data_structures;

import bfst21.Osm_Elements.NodeHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RTree {
    private int minimumChildren, maximumChildren, size;
    private RTreeNode root;

    public RTree(int minimumChildren, int maximumChildren) {
        this.minimumChildren = minimumChildren;
        this.maximumChildren = maximumChildren;
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
        RTreeNode selectedNode = chooseLeaf(nodeHolder, root);

        RTreeNode newEntry = new RTreeNode(nodeHolder.getCoordinates(), true, minimumChildren, maximumChildren, selectedNode.getParent());
        newEntry.addNodeHolderEntry(nodeHolder);

        selectedNode.addChild(newEntry);

        if (selectedNode.overflow()) {
            RTreeNode[] result = splitNodeShuffle(selectedNode);
            adjustTree(result[0], result[1]);
        } else {
            adjustTree(selectedNode, null);
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

    private void adjustTree(RTreeNode originalNode, RTreeNode newNode) {
        if (originalNode.getParent() == null) { // root
            if(originalNode.overflow()) {
                RTreeNode[] rootChildren = splitNodeShuffle(originalNode);
                RTreeNode newRoot = new RTreeNode(new float[originalNode.getCoordinates().length], false, minimumChildren, maximumChildren, null);
                newRoot.addChild(rootChildren[0]);
                newRoot.addChild(rootChildren[1]);
                updateNodeCoordinates(newRoot);
            }
        } else if (newNode == null) {
            updateNodeCoordinates(originalNode);
            adjustTree(originalNode.getParent(), null);
        } else {
            updateNodeCoordinates(originalNode);
            updateNodeCoordinates(newNode);
            if(originalNode.getParent().overflow()) {
                splitNodeShuffle(originalNode.getParent());
            }
        }
    }

    private void updateNodeCoordinates(RTreeNode node) {

        for (RTreeNode r: node.getChildren()) {
            for (int i = 0; i < node.getCoordinates().length; i++) {
                if(r.getCoordinates()[i] > node.getCoordinates()[i]) {
                    node.updateCoordinate(i, r.getCoordinates()[i]);
                }
            }
        }
    }

    private RTreeNode[] splitNodeShuffle(RTreeNode node) {

        ArrayList<RTreeNode> elementsToSplit = node.getChildren();
        Collections.shuffle(elementsToSplit);

        node.removeChildren();
        ArrayList<RTreeNode> childrenForNewNode = new ArrayList<>();

        for (int i = 0; i < elementsToSplit.size(); i += 2) {
            node.addChild(elementsToSplit.get(i));
            childrenForNewNode.add(elementsToSplit.get(i + 1));
        }
        RTreeNode newNode = new RTreeNode(node.getCoordinates(), node.isLeaf(), minimumChildren, maximumChildren, node.getParent());
        newNode.addChildren(childrenForNewNode);

        return new RTreeNode[]{node, newNode};
    }

    /*
    private RTreeNode[] splitNodeExhaustive(RTreeNode leaf, NodeHolder nodeHolderToInsert) {

    }

    private RTreeNode splitNodeQuadraticCost(RTreeNode leaf) {

    }

    private RTreeNode splitNodeLinearCost(RTreeNode leaf) {

    }*/

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
        return Math.abs(area);
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
