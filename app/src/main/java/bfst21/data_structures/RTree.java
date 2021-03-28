package bfst21.data_structures;

import bfst21.Osm_Elements.Element;

import java.util.*;

public class RTree {
    private int minimumChildren, maximumChildren, numberOfCoordinates;
    private RTreeNode root;
    private int idCount; // TODO: 3/22/21 delete

    public RTree(int minimumChildren, int maximumChildren, int numberOfCoordinates) {
        this.minimumChildren = minimumChildren;
        this.maximumChildren = maximumChildren;
        this.numberOfCoordinates = numberOfCoordinates;
        root = null;
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
        if(root != null) {
            float[] searchCoordinates = new float[]{xMin, xMax, yMin, yMax};
            ArrayList<Element> results = new ArrayList<>();
            search(searchCoordinates, root, results);
            return results;
        } else {
            //throw new RuntimeException("No elements in the RTree");
            System.out.println("No elements in the rtree");
            return new ArrayList<>();
        }
    }

    private void search(float[] searchCoordinates, RTreeNode node, ArrayList<Element> results) {
        if (node.isLeaf()) {
            for (RTreeNode r : node.getChildren()) {
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

    public void insertAll(List<Element> elements) {
        for (Element e: elements) {
            insert(e);
        }
    }

    public void insert(Element element) {

        if (root == null) {
            root = new RTreeNode(element.getCoordinates(), true, minimumChildren, maximumChildren, null, getId());
            RTreeNode dataLeaf = new RTreeNode(element.getCoordinates(), false, minimumChildren, maximumChildren, root, getId());
            dataLeaf.addElementEntry(element);
            root.addChild(dataLeaf);
        } else {
            RTreeNode selectedNode = chooseLeaf(element, root); // select where to place the new node
            RTreeNode newEntry = new RTreeNode(element.getCoordinates(), false, minimumChildren, maximumChildren, selectedNode, getId());
            newEntry.addElementEntry(element);
            selectedNode.addChild(newEntry);
            checkOverflow(selectedNode);
        }
    }

    private void checkOverflow(RTreeNode node) {
        if (node.overflow()) {
            //RTreeNode[] result = splitNodeShuffle(node);
            RTreeNode[] result = splitNodeQuadraticCost(node);
            adjustTree(result[0], result[1]);
            checkOverflow(node.getParent());
        } else {
            adjustTree(node, null);
        }
    }

    private RTreeNode chooseLeaf(Element element, RTreeNode node) {
        if (node.isLeaf()) {
            return node;
        } else {
            ArrayList<RTreeNode> children = node.getChildren();
            RTreeNode smallestBoundingBoxNode = children.get(0);
            for (int i = 1; i < node.getChildren().size(); i++) {
                if (getNewBoundingBoxArea(element.getCoordinates(), children.get(i).getCoordinates()) < getNewBoundingBoxArea(element.getCoordinates(), smallestBoundingBoxNode.getCoordinates())) {
                    smallestBoundingBoxNode = children.get(i);
                }
            }
            return chooseLeaf(element, smallestBoundingBoxNode);
        }
    }

    private void adjustTree(RTreeNode originalNode, RTreeNode newNode) {
        if (originalNode.getParent() == null && (newNode == null || originalNode == newNode)) { // only root
            updateNodeCoordinates(originalNode);
        } else if (originalNode.getParent() == null && newNode != null) {// need to join them under one root
            createNewRoot(originalNode, newNode);
            adjustTree(root, null);
        } else if (newNode == null) {
            updateNodeCoordinates(originalNode);
            adjustTree(originalNode.getParent(), null);
        } else { // not root but two new nodes need to climb the tree
            updateNodeCoordinates(originalNode);
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
        adjustTree(firstNode, secondNode);
    }

    private void updateNodeCoordinates(RTreeNode node) {
        float[] newCoordinates = createNewCoordinateArray();
        for (RTreeNode childNode : node.getChildren()) {
            for (int i = 0; i < numberOfCoordinates; i += 2) {
                if (childNode.getCoordinates()[i] < newCoordinates[i]) {
                    newCoordinates[i] = childNode.getCoordinates()[i];
                }
                if (childNode.getCoordinates()[i + 1] > newCoordinates[i + 1]) {
                    newCoordinates[i + 1] = childNode.getCoordinates()[i + 1];
                }
            }
        }
        node.updateCoordinate(newCoordinates);
    }

    private RTreeNode[] splitNodeShuffle(RTreeNode node) {

        RTreeNode newNode = new RTreeNode(createNewCoordinateArray(), node.isLeaf(), minimumChildren, maximumChildren, node.getParent(), getId());

        ArrayList<RTreeNode> elementsToSplit = node.getChildren();
        Collections.shuffle(elementsToSplit);

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

        if (node.getParent() != null) {
            node.getParent().addChild(newNode);
        }

        return new RTreeNode[]{node, newNode};
    }

    private RTreeNode[] splitNodeQuadraticCost(RTreeNode node) {
        ArrayList<RTreeNode> elementsToSplit = new ArrayList<>(node.getChildren());
        int[] seeds = pickSeeds(elementsToSplit);

        RTreeNode elementForNode = elementsToSplit.get(seeds[0]);
        RTreeNode elementForNewNode = elementsToSplit.get(seeds[1]);

        if (seeds[0] < seeds[1]) {
            elementsToSplit.remove(seeds[1]);
            elementsToSplit.remove(seeds[0]);
        } else {
            elementsToSplit.remove(seeds[0]);
            elementsToSplit.remove(seeds[1]);
        }

        node.removeChildren();
        node.addChild(elementForNode);
        node.updateCoordinate(createNewCoordinateArray());

        RTreeNode newNode = new RTreeNode(createNewCoordinateArray(), node.isLeaf(), minimumChildren, maximumChildren, node.getParent(), getId());
        newNode.addChild(elementForNewNode);

        if (elementsToSplit.size() == 0) {
            return new RTreeNode[]{node, newNode};
        } else if (tooFewEntries(node.getChildren().size(), elementsToSplit.size())) {
            node.addChildren(elementsToSplit);
        } else if (tooFewEntries(newNode.getChildren().size(), elementsToSplit.size())) {
            newNode.addChildren(elementsToSplit);
        } else {
            while (elementsToSplit.size() > 0) {
                RTreeNode[] nextAssignment = pickNext(elementsToSplit, node, newNode);
                nextAssignment[1].addChild(nextAssignment[0]);
            }
        }

        if (node.getParent() != null) {
            node.getParent().addChild(newNode);
        }

        return new RTreeNode[]{node, newNode};
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

    private boolean tooFewEntries(int numberOfChildren, int elementsLeft) {
        return numberOfChildren < minimumChildren && elementsLeft <= minimumChildren - numberOfChildren;
    }

    /*private RTreeNode splitNodeLinearCost(ArrayList<RTreeNode> elementsToSplit) {
        RTreeNode[] bestPair = new RTreeNode[2];
        float bestSeparation = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < numberOfCoordinates/2; i++) {
            RTreeNode nodeMaxLowerBound = null;
            RTreeNode nodeMinLowerBound = null;
            for (RTreeNode n: elementsToSplit) {
                
            }
        }
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
