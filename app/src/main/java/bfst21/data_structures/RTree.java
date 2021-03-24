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
            //RTreeNode[] result = splitNodeShuffle(node);
            RTreeNode[] result = splitNodeQuadraticCost(node);
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
                if (getNewBoundingBoxArea(element.getCoordinates(), children.get(i).getCoordinates()) < getNewBoundingBoxArea(element.getCoordinates(), smallestBoundingBoxNode.getCoordinates())) {
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

    private RTreeNode[] splitNodeQuadraticCost(RTreeNode node) {
        ArrayList<RTreeNode> elementsToSplit = node.getChildren();
        int[] seeds = pickSeeds(elementsToSplit);

        RTreeNode elementForNewNode1 = elementsToSplit.get(seeds[0]);
        RTreeNode elementForNewNode2 = elementsToSplit.get(seeds[1]);

        RTreeNode newNode1 = new RTreeNode(createNewCoordinateArray(), node.isLeaf(), minimumChildren, maximumChildren, node.getParent(), getId());
        newNode1.addChild(elementForNewNode1);

        RTreeNode newNode2 = new RTreeNode(createNewCoordinateArray(), node.isLeaf(), minimumChildren, maximumChildren, node.getParent(), getId());
        newNode2.addChild(elementForNewNode2);

        if (seeds[0] < seeds[1]) {
            elementsToSplit.remove(seeds[1]);
            elementsToSplit.remove(seeds[0]);
        } else {
            elementsToSplit.remove(seeds[0]);
            elementsToSplit.remove(seeds[1]);
        }

        if (elementsToSplit.size() == 0) {
            return new RTreeNode[]{newNode1, newNode2};
        } else if (tooFewEntries(newNode1.getChildren().size(), elementsToSplit.size())) {
            newNode1.addChildren(elementsToSplit);
        } else if (tooFewEntries(newNode2.getChildren().size(), elementsToSplit.size())) {
            newNode2.addChildren(elementsToSplit);
        } else {
            while (elementsToSplit.size() > 0) {
                RTreeNode[] nextAssignment = pickNext(elementsToSplit, newNode1, newNode2);
                nextAssignment[1].addChild(nextAssignment[0]);
            }
        }

        if (node.getParent() != null) {
            node.getParent().addChild(newNode1);
            node.getParent().addChild(newNode2); // TODO: 3/24/21 Need to delete node from its parent 
        }

        return new RTreeNode[]{newNode1, newNode2};
    }

    private RTreeNode[] pickNext(ArrayList<RTreeNode> elementsToSplit, RTreeNode node1, RTreeNode node2) {
        float greatestAreaDif = Float.NEGATIVE_INFINITY;
        int nextElementIndex = 0;
        RTreeNode[] nextAssignment = new RTreeNode[2];

        for (int i = 0; i < elementsToSplit.size(); i++) {
            RTreeNode n = elementsToSplit.get(i);
            float newArea1 = getNewBoundingBoxArea(node1.getCoordinates(), n.getCoordinates());
            float newArea2 = getNewBoundingBoxArea(node2.getCoordinates(), n.getCoordinates());
            float areaDif = Math.abs(newArea1 - newArea2);
            if (areaDif > greatestAreaDif) {
                greatestAreaDif = areaDif;
                nextElementIndex = i;
                nextAssignment[0] = n;
                nextAssignment[1] = newArea1 < newArea2 ? node1 : node2;
            }
        }

        if (greatestAreaDif == 0) {
            float area1 = getArea(node1.getCoordinates());
            float area2 = getArea(node2.getCoordinates());

            if (area1 == area2) {
                int entries1 = node1.getChildren().size();
                int entries2 = node2.getChildren().size();

                if (entries1 == entries2) {
                    nextAssignment[1] = node1;
                } else {
                    nextAssignment[1] = entries1 < entries2 ? node1 : node2;
                }
            } else {
                nextAssignment[1] = area1 < area2 ? node1 : node2;
            }
        }
        elementsToSplit.remove(nextElementIndex);
        return nextAssignment;
    }

    /*private boolean checkTies(RTreeNode newNode1, RTreeNode newNode2) {

    }*/

    private boolean tooFewEntries(int numberOfChildren, int elementsLeft) {
        return numberOfChildren < minimumChildren && elementsLeft <= minimumChildren - numberOfChildren;
    }

    /*
    private RTreeNode[] splitNodeExhaustive(RTreeNode leaf, NodeHolder nodeHolderToInsert) {

    }

    private RTreeNode splitNodeLinearCost(RTreeNode leaf) {

    }*/

    private float getArea(float[] coordinates) {
        float area = 1;
        for (int i = 0; i < numberOfCoordinates - 1; i += 2) {
            area *= (coordinates[i + 1] - coordinates[i]);
        }
        return Math.abs(area);
    }

    private float getNewBoundingBoxArea(float[] coordinatesFirstNode, float[] coordinateSecondNode) {
        float[] newCoordinates = new float[numberOfCoordinates];

        for (int i = 0; i < numberOfCoordinates; i += 2) {
            newCoordinates[i] = Math.min(coordinatesFirstNode[i], coordinateSecondNode[i]);
            newCoordinates[i + 1] = Math.max(coordinatesFirstNode[i + 1], coordinateSecondNode[i + 1]);
        }
        return getArea(newCoordinates);
    }

    private int[] pickSeeds(ArrayList<RTreeNode> elementsToSplit) {
        int[] mostWastefulNodesIndices = new int[2];
        float largestArea = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < elementsToSplit.size() - 1; i++) {
            for (int j = i + 1; j < elementsToSplit.size(); j++) {
                float currentArea = findAreaDifference(elementsToSplit.get(i).getCoordinates(), elementsToSplit.get(j).getCoordinates());
                if (currentArea > largestArea) {
                    mostWastefulNodesIndices[0] = i;
                    mostWastefulNodesIndices[1] = j;
                    largestArea = currentArea;
                }
            }
        }
        return mostWastefulNodesIndices;
    }

    private float findAreaDifference(float[] firstCoordinates, float[] secondCoordinates) {
        float areaWithBoth = getNewBoundingBoxArea(firstCoordinates, secondCoordinates);
        float areaElement1 = getArea(firstCoordinates);
        float areaElement2 = getArea(secondCoordinates);
        return areaWithBoth - areaElement1 - areaElement2;
    }

    private Boolean intersects(float[] coordinates1, float[] coordinates2) {
        for (int i = 0; i < numberOfCoordinates; i += 2) {
            if (specificCoordinatesDoesNotIntersect(coordinates1[i], coordinates2[i + 1])) {
                return false;
            } else if (specificCoordinatesDoesNotIntersect(coordinates2[i], coordinates1[i + 1])) {
                return false;
            }
        }
        return true;
    }

    private boolean specificCoordinatesDoesNotIntersect(float minCoordinateFirstElement, float maxCoordinateSecondElement) {
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
