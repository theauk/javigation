package bfst21.data_structures;

import bfst21.osm_elements.NodeHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RTree {
    private int minimumChildren, maximumChildren, size, dimensions;
    private RTreeNode root;

    public RTree(int minimumChildren, int maximumChildren, int dimensions) {
        this.minimumChildren = minimumChildren;
        this.maximumChildren = maximumChildren;
        this.dimensions = dimensions;
        root = null;
        size = 0;
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
            if (originalNode.overflow()) {
                createNewRoot(originalNode);
            }
        } else if (newNode == null) {
            updateNodeCoordinates(originalNode);
            adjustTree(originalNode.getParent(), null);
        } else {
            updateNodeCoordinates(originalNode);
            updateNodeCoordinates(newNode);
            if (originalNode.getParent().overflow()) {
                createNewParents(originalNode.getParent());
            }
        }
    }

    private void createNewParents(RTreeNode oldParent) {
        RTreeNode[] newParents = splitNodeShuffle(oldParent);
        adjustTree(newParents[0], newParents[1]);
    }

    private void createNewRoot(RTreeNode oldRoot) {
        RTreeNode[] rootChildren = splitNodeShuffle(oldRoot);
        RTreeNode newRoot = new RTreeNode(new float[dimensions], false, minimumChildren, maximumChildren, null);
        newRoot.addChild(rootChildren[0]);
        newRoot.addChild(rootChildren[1]);
        updateNodeCoordinates(newRoot);
    }

    private void updateNodeCoordinates(RTreeNode node) {
        for (RTreeNode childNode : node.getChildren()) {
            for (int i = 0; i < dimensions; i += 2) {
                if (childNode.getCoordinates()[i] < node.getCoordinates()[i])
                    node.updateCoordinate(i, childNode.getCoordinates()[i]);
                if (childNode.getCoordinates()[i + 1] > node.getCoordinates()[i + 1])
                    node.updateCoordinate(i + 1, childNode.getCoordinates()[i]);
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
        float[] newCoordinates = new float[dimensions];

        for (int i = 0; i < dimensions; i += 2) {
            newCoordinates[i] = Math.min(nodeHolder.getCoordinates()[i], node.getCoordinates()[i]);
            newCoordinates[i + 1] = Math.max(nodeHolder.getCoordinates()[i + 1], node.getCoordinates()[i + 1]);
        }
        float area = 1;
        for (int j = 0; j < dimensions - 1; j += 2) {
            area *= (newCoordinates[j + 1] - newCoordinates[j]);
        }
        return Math.abs(area);
    }

    public Boolean intersects(float[] coordinates1, float[] coordinates2) {
        for (int i = 0; i < dimensions; i += 2) {
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
}
