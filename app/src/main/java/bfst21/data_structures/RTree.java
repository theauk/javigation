package bfst21.data_structures;

import bfst21.Osm_Elements.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RTree {
    private int minimumChildren, maximumChildren, size, numberOfCoordinates;
    private RTreeNode root;
    private int idCount; // TODO: 3/22/21 delete

    public RTree(int minimumChildren, int maximumChildren, int numberOfCoordinates) {
        this.minimumChildren = minimumChildren;
        this.maximumChildren = maximumChildren;
        this.numberOfCoordinates = numberOfCoordinates;
        root = null;
        size = 0;
        idCount = 0;
    }

    public RTreeNode getRoot() {
        return root;
    }

    // TODO: 3/17/21 delete both root methods later
    public void setRoot(RTreeNode n) {
        root = n;
    }

    public float[] createNewCoordinateArray() {
        return new float[]{Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
    }

    private int getId() {
        idCount += 1;
        return idCount;
    }

    public ArrayList<Element> search(float xMin, float xMax, float yMin, float yMax) {
        float[] searchCoordinates = new float[]{xMin, xMax, yMin, yMax};
        ArrayList<Element> results = new ArrayList<>();
        search(searchCoordinates, root, results);
        return results;
    }

    private void search(float[] searchCoordinates, RTreeNode node, ArrayList<Element> results) {
        if (node.isLeaf()) {
            for (RTreeNode r : node.getChildren()) { // TODO: 3/22/21 fix search: seems like something here · like it only runs one on 51
                for (Element e : r.getElementEntries()) {
                    if (intersects(searchCoordinates, e.getCoordinates())) {
                        results.add(e);
                    }
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

    public void insert(Element element) {

        System.out.println("");
        if (root == null) { // if there are no roots we need to create a new one which points to its data
            System.out.println("creating first root with leaf true");
            root = new RTreeNode(element.getCoordinates(), true, minimumChildren, maximumChildren, null, getId());
            RTreeNode dataLeaf = new RTreeNode(element.getCoordinates(), false, minimumChildren, maximumChildren, root, getId());
            dataLeaf.addElementEntry(element);
            root.addChild(dataLeaf);
        } else {
            System.out.println("root is already created -> invoke selectNode");
            RTreeNode selectedNode = chooseLeaf(element, root); // select where to place the new node
            System.out.println("selected node id: " + selectedNode.id);
            System.out.println("selected node coordinates: " + Arrays.toString(selectedNode.getCoordinates()) + " with parent: " + selectedNode.getParent());

            // need to create new data entry node in the selected node. So we set leaf as false
            // we set its parent as the node that will hold it as a child
            RTreeNode newEntry = new RTreeNode(element.getCoordinates(), false, minimumChildren, maximumChildren, selectedNode, getId());
            newEntry.addElementEntry(element);
            selectedNode.addChild(newEntry);
            checkOverflow(selectedNode);
        }
    }

    private void checkOverflow(RTreeNode node) {
        if (node.overflow()) {
            System.out.println("The selected node overflows -> split the node");
            RTreeNode[] result = splitNodeShuffle(node);
            System.out.println("The new node(s): " + result[0].id + " " + result[1].id);

            System.out.println("");
            System.out.println("Children");
            for (RTreeNode r : result[0].getChildren()) {
                System.out.println("First: " + Arrays.toString(r.getCoordinates()) + " id: " + r.id + " parent " + r.getParent().id);
            }
            for (RTreeNode r : result[1].getChildren()) {
                System.out.println("Second : " + Arrays.toString(r.getCoordinates()) + " id: " + r.id + " parent " + r.getParent().id);
            }
            System.out.println("");
            // adjust the new nodes
            adjustTree(result[0], result[1]);
            System.out.println("HERE: " + result[0].getParent().id + " " + result[1].getParent().id + " " + node.getParent().id);
            checkOverflow(node.getParent());
        } else {
            System.out.println("The selected node does not overflow");
            adjustTree(node, null);
        }
    }

    private RTreeNode chooseLeaf(Element element, RTreeNode node) {
        if (node.isLeaf()) {
            System.out.println("Found a leaf: " + node.id);
            return node;
        } else {
            System.out.println("Search deeper for leaf");
            System.out.println("Number of children: " + node.getChildren().size());
            ArrayList<RTreeNode> children = node.getChildren();
            RTreeNode smallestBoundingBoxNode = children.get(0);
            for (int i = 1; i < node.getChildren().size(); i++) {
                System.out.println("Coordinates child: " + Arrays.toString(node.getChildren().get(i).getCoordinates()));
                if (getNewBoundingBoxArea(element, children.get(i)) < getNewBoundingBoxArea(element, smallestBoundingBoxNode)) {
                    smallestBoundingBoxNode = children.get(i);
                }
            }
            return chooseLeaf(element, smallestBoundingBoxNode);
        }
    }

    private void adjustTree(RTreeNode originalNode, RTreeNode newNode) {
        if (originalNode.getParent() == null && (newNode == null || originalNode == newNode)) { // only root
            System.out.println("Adjust the root");
            updateNodeCoordinates(originalNode);
        } else if (originalNode.getParent() == null && newNode != null) {// need to join them under one root
            System.out.println("Create new root");
            createNewRoot(originalNode, newNode);
            adjustTree(root, null);
        } else if (newNode == null) {
            System.out.println("Adjust: no split");
            updateNodeCoordinates(originalNode);
            adjustTree(originalNode.getParent(), null);
        } else { // not root but two new nodes need to climb the tree
            System.out.println("");
            System.out.println("Update node coordinates original node");
            updateNodeCoordinates(originalNode);
            System.out.println("");
            System.out.println("Update node coordinates new node");
            updateNodeCoordinates(newNode);
            adjustTree(originalNode.getParent(), newNode.getParent());
        }
    }

    private void createNewRoot(RTreeNode firstNode, RTreeNode secondNode) {
        // the new root is not a leaf. Use the coordinates from one of the nodes to avoid problems with 0
        RTreeNode newRoot = new RTreeNode(createNewCoordinateArray(), false, minimumChildren, maximumChildren, null, getId());
        newRoot.addChild(firstNode);
        newRoot.addChild(secondNode);
        root = newRoot;
        System.out.println("New parent of nodes below root: " + firstNode.getParent().id + " " + secondNode.getParent().id);
        System.out.println("Should be null as root has no parent: " + firstNode.getParent().getParent());
        System.out.println("Adjust the tree for the two new nodes below the root");
        adjustTree(firstNode, secondNode);
        System.out.println("");
        System.out.println("New coordinates of first node: " + Arrays.toString(firstNode.getCoordinates()));
        System.out.println("New coordinates of second node: " + Arrays.toString(secondNode.getCoordinates()));
    }

    private void updateNodeCoordinates(RTreeNode node) {
        float[] newCoordinates = createNewCoordinateArray();
        for (RTreeNode childNode : node.getChildren()) {
            System.out.println("");
            System.out.println("New child");
            for (int i = 0; i < numberOfCoordinates; i += 2) {
                System.out.println("i is: " + i);
                System.out.println("MIN child: " + childNode.getCoordinates()[i] + " node: " + newCoordinates[i]);
                if (childNode.getCoordinates()[i] < newCoordinates[i]) {
                    newCoordinates[i] = childNode.getCoordinates()[i];
                    System.out.println("update min coordinate to: " + childNode.getCoordinates()[i]);
                }
                System.out.println("MAX child: " + childNode.getCoordinates()[i + 1] + " node: " + newCoordinates[i + 1]);
                if (childNode.getCoordinates()[i + 1] > newCoordinates[i + 1]) {
                    newCoordinates[i + 1] = childNode.getCoordinates()[i + 1];
                    System.out.println("update max coordinate to: " + childNode.getCoordinates()[i + 1]);
                }
            }
        }
        node.updateCoordinate(newCoordinates);
    }

    private RTreeNode[] splitNodeShuffle(RTreeNode node) {

        System.out.println("Node to split: " + node.id);

        RTreeNode newNode = new RTreeNode(createNewCoordinateArray(), node.isLeaf(), minimumChildren, maximumChildren, node.getParent(), getId());

        ArrayList<RTreeNode> elementsToSplit = node.getChildren();
        //Collections.shuffle(elementsToSplit);

        ArrayList<RTreeNode> childrenForOldNode = new ArrayList<>();
        ArrayList<RTreeNode> childrenForNewNode = new ArrayList<>();

        for (int i = 0; i < elementsToSplit.size(); i++) {
            if (i % 2 == 0) {
                childrenForOldNode.add(elementsToSplit.get(i));
                elementsToSplit.get(i).setParent(node);
            } else {
                childrenForNewNode.add(elementsToSplit.get(i));
                elementsToSplit.get(i).setParent(newNode);
            }
        }

        node.removeChildren();
        node.addChildren(childrenForOldNode);
        newNode.addChildren(childrenForNewNode);

        System.out.println("");
        System.out.println("In split first children: " + node.getChildren().size());
        System.out.println("In split second children: " + newNode.getChildren().size());
        System.out.println("");

        if (node.getParent() != null) {
            node.getParent().addChild(newNode);
        }

        return new RTreeNode[]{node, newNode};
    }

    /*
    private RTreeNode[] splitNodeExhaustive(RTreeNode leaf, NodeHolder nodeHolderToInsert) {

    }

    private RTreeNode splitNodeQuadraticCost(RTreeNode leaf) {

    }

    private RTreeNode splitNodeLinearCost(RTreeNode leaf) {

    }*/

    private float getNewBoundingBoxArea(Element element, RTreeNode node) {
        float[] newCoordinates = new float[numberOfCoordinates];

        for (int i = 0; i < numberOfCoordinates; i += 2) {
            newCoordinates[i] = Math.min(element.getCoordinates()[i], node.getCoordinates()[i]);
            newCoordinates[i + 1] = Math.max(element.getCoordinates()[i + 1], node.getCoordinates()[i + 1]);
        }
        float area = 1;
        for (int j = 0; j < numberOfCoordinates - 1; j += 2) {
            area *= (newCoordinates[j + 1] - newCoordinates[j]);
        }
        return Math.abs(area);
    }

    private Boolean intersects(float[] coordinates1, float[] coordinates2) {
        for (int i = 0; i < numberOfCoordinates; i += 2) {
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

    public void printTree() {
        int level = 0;
        HashMap<Integer, ArrayList<RTreeNode>> result = new HashMap<>();
        result = getPrintTree(root, level, result);

        while (result.get(level) != null) {
            System.out.println("");
            System.out.println("Level: " + level);
            for (RTreeNode r : result.get(level)) {
                if (r.getParent() == null) {
                    System.out.println("Node: " + r.id + " Coordinates: " + Arrays.toString(r.getCoordinates()) + " Parent: " + null + " leaf: " + r.isLeaf());
                } else {
                    System.out.println("Node: " + r.id + " Coordinates: " + Arrays.toString(r.getCoordinates()) + " Parent: " + r.getParent().id + " leaf: " + r.isLeaf());
                }
            }
            level++;
        }
        System.out.println("");
    }

    public HashMap<Integer, ArrayList<RTreeNode>> getPrintTree(RTreeNode theRoot, int level, HashMap<Integer, ArrayList<RTreeNode>> result) {
        if (theRoot != null) {

            if (result.get(level) == null) {
                ArrayList<RTreeNode> newAL = new ArrayList<>();
                newAL.add(theRoot);
                result.put(level, newAL);
            } else {
                ArrayList<RTreeNode> current = result.get(level);
                current.add(theRoot);
                result.put(level, current);
            }
            level += 1;
            for (RTreeNode child : theRoot.getChildren()) {
                getPrintTree(child, level, result);
            }
        }
        return result;
    }
}
