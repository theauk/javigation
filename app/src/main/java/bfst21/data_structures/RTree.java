package bfst21.data_structures;

import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;

import java.util.*;

public class RTree {
    private final int minimumChildren, maximumChildren, numberOfCoordinates;
    private RTreeNode root;

    public RTree(int minimumChildren, int maximumChildren, int numberOfCoordinates) {
        this.minimumChildren = minimumChildren;
        this.maximumChildren = maximumChildren;
        this.numberOfCoordinates = numberOfCoordinates;
        root = null;
    }

    public float[] createNewCoordinateArray() {
        return new float[]{Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
    }

    public ArrayList<Element> search(float xMin, float xMax, float yMin, float yMax, boolean debug) {
        if (root != null) {
            float[] searchCoordinates = new float[]{xMin, xMax, yMin, yMax};
            ArrayList<Element> results = new ArrayList<>();
            if (debug) { // TODO: 3/31/21 Delete when getting rid of debug mode
                searchCoordinates = new float[]{xMin + 0.01f, xMax + (-0.01f), yMin + 0.01f, yMax + (-0.01f)};
                //searchCoordinates = new float[]{xMin*0.99f, xMax*0.99f, yMin*0.99f, yMax*0.99f};
                results.add(createDebugBoundsRectangle(searchCoordinates));
                searchDebug(searchCoordinates, root, results);
            } else {
                search(searchCoordinates, root, results);
            }
            return results;
        } else {
            //throw new RuntimeException("No elements in the RTree"); 
            System.out.println("No elements in the rtree"); // TODO: 3/26/21 fix this in view so that the search is not called before file has been loaded 
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

    private void searchDebug(float[] searchCoordinates, RTreeNode node, ArrayList<Element> results) { // TODO: 3/31/21 Delete when getting rid of debug mode
        if (node.isLeaf()) {
            for (RTreeNode r : node.getChildren()) {
                for (Element e : r.getElementEntries()) {
                    if (intersects(searchCoordinates, e.getCoordinates())) {
                        results.add(createDebugRectangleRelation(e.getCoordinates()));
                        results.add(e);
                    }
                }
            }
        } else {
            for (RTreeNode r : node.getChildren()) {
                if (intersects(searchCoordinates, r.getCoordinates())) {
                    //results.add(createDebugRelation(r.getCoordinates()));
                    searchDebug(searchCoordinates, r, results);
                }
            }
        }
    }

    private Way createDebugWay(float firstCoordinate, float secondCoordinate, float thirdCoordinate, float fourthCoordinate) { // TODO: 3/31/21 Delete when getting rid of debug mode
        Way w = new Way(0);
        w.addNode(new Node(firstCoordinate, secondCoordinate));
        w.addNode(new Node(thirdCoordinate, fourthCoordinate));
        return w;
    }

    private Relation createDebugBoundsRectangle(float[] searchCoordinates) { // TODO: 3/31/21 Delete when getting rid of debug mode
        Relation r = new Relation(0);
        r.addWay(createDebugWay(searchCoordinates[0], searchCoordinates[2], searchCoordinates[0], searchCoordinates[3]));
        r.addWay(createDebugWay(searchCoordinates[0], searchCoordinates[2], searchCoordinates[1], searchCoordinates[2]));
        r.addWay(createDebugWay(searchCoordinates[1], searchCoordinates[2], searchCoordinates[1], searchCoordinates[3]));
        r.addWay(createDebugWay(searchCoordinates[0], searchCoordinates[3], searchCoordinates[1], searchCoordinates[3]));
        return r;
    }

    private Relation createDebugRectangleRelation(float[] coordinates) { // TODO: 3/31/21 Delete when getting rid of debug mode
        Relation r = new Relation(0);
        r.addWay(createDebugWay(coordinates[0], coordinates[2], coordinates[1], coordinates[2]));
        r.addWay(createDebugWay(coordinates[0], coordinates[2], coordinates[0], coordinates[3]));
        r.addWay(createDebugWay(coordinates[1], coordinates[2], coordinates[1], coordinates[3]));
        r.addWay(createDebugWay(coordinates[0], coordinates[3], coordinates[1], coordinates[3]));
        return r;
    }

    public void insertAll(List<Element> elements) {
        for (Element e : elements) {
            insert(e);
        }
    }

    public void insert(Element element) {
        if (root == null) {
            root = new RTreeNode(element.getCoordinates(), true, minimumChildren, maximumChildren, null);
            RTreeNode dataLeaf = new RTreeNode(element.getCoordinates(), false, minimumChildren, maximumChildren, root);
            dataLeaf.addElementEntry(element);
            root.addChild(dataLeaf);
        } else {
            RTreeNode selectedNode = chooseLeaf(element, root);
            RTreeNode newEntry = new RTreeNode(element.getCoordinates(), false, minimumChildren, maximumChildren, selectedNode);
            newEntry.addElementEntry(element);
            selectedNode.addChild(newEntry);
            checkOverflow(selectedNode);
        }
    }

    private void checkOverflow(RTreeNode node) {
        if (node.overflow()) { // TODO: 3/31/21 Decide on split method 
            //RTreeNode[] result = splitNodeShuffle(node);
            RTreeNode[] result = splitNodeQuadraticCost(node);
            //RTreeNode[] result = splitNodeLinearCost(node);
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
        } else if (originalNode.getParent() == null && newNode != null) { // need to join two nodes under one root
            createNewRoot(originalNode, newNode);
            adjustTree(root, null);
        } else if (newNode == null) { // only one node
            updateNodeCoordinates(originalNode);
            adjustTree(originalNode.getParent(), null);
        } else { // two nodes
            updateNodeCoordinates(originalNode);
            updateNodeCoordinates(newNode);
            adjustTree(originalNode.getParent(), newNode.getParent());
        }
    }

    private void createNewRoot(RTreeNode firstNode, RTreeNode secondNode) {
        RTreeNode newRoot = new RTreeNode(createNewCoordinateArray(), false, minimumChildren, maximumChildren, null);
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

        RTreeNode newNode = new RTreeNode(createNewCoordinateArray(), node.isLeaf(), minimumChildren, maximumChildren, node.getParent());

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
        int[] seeds = pickSeedsQuadratic(elementsToSplit);
        RTreeNode newNode = new RTreeNode(createNewCoordinateArray(), node.isLeaf(), minimumChildren, maximumChildren, node.getParent());

        RTreeNode elementForNode = elementsToSplit.get(seeds[0]);
        RTreeNode elementForNewNode = elementsToSplit.get(seeds[1]);

        removeElementsFromElementsToSplit(elementsToSplit, seeds[0], seeds[1]);
        updateNodes(node, newNode, elementForNode, elementForNewNode);
        distributeNodesQuadraticCost(elementsToSplit, node, newNode);

        return new RTreeNode[]{node, newNode};
    }

    private void updateNodes(RTreeNode node, RTreeNode newNode, RTreeNode elementForNode, RTreeNode elementForNewNode) {
        node.removeChildren();
        node.addChild(elementForNode);
        node.updateCoordinate(createNewCoordinateArray());
        newNode.addChild(elementForNewNode);
        if (node.getParent() != null) node.getParent().addChild(newNode);
    }

    private void removeElementsFromElementsToSplit(ArrayList<RTreeNode> elementsToSplit, int index1, int index2) {
        if (index1 < index2) {
            elementsToSplit.remove(index2);
            elementsToSplit.remove(index1);
        } else {
            elementsToSplit.remove(index1);
            elementsToSplit.remove(index2);
        }
    }

    private void distributeNodesQuadraticCost(ArrayList<RTreeNode> elementsToSplit, RTreeNode node, RTreeNode newNode) {
        if (tooFewEntries(node.getChildren().size(), elementsToSplit.size())) {
            node.addChildren(elementsToSplit);
        } else if (tooFewEntries(newNode.getChildren().size(), elementsToSplit.size())) {
            newNode.addChildren(elementsToSplit);
        } else {
            while (elementsToSplit.size() > 0) {
                RTreeNode[] nextAssignment = pickNext(elementsToSplit, node, newNode);
                nextAssignment[1].addChild(nextAssignment[0]);
            }
        }
    }

    private boolean tooFewEntries(int numberOfChildren, int elementsLeft) {
        return numberOfChildren < minimumChildren && elementsLeft <= minimumChildren - numberOfChildren;
    }

    private RTreeNode[] pickNext(ArrayList<RTreeNode> elementsToSplit, RTreeNode node1, RTreeNode node2) {
        float greatestAreaDif = Float.NEGATIVE_INFINITY;
        int nextElementIndex = 0;
        RTreeNode assign = null;
        RTreeNode assignTo = null;

        for (int i = 0; i < elementsToSplit.size(); i++) {
            RTreeNode n = elementsToSplit.get(i);
            float newAreaWithNode1 = getNewBoundingBoxArea(node1.getCoordinates(), n.getCoordinates());
            float newAreaWithNode2 = getNewBoundingBoxArea(node2.getCoordinates(), n.getCoordinates());
            float areaDif = Math.abs(newAreaWithNode1 - newAreaWithNode2);
            if (areaDif > greatestAreaDif) {
                greatestAreaDif = areaDif;
                nextElementIndex = i;
                assign = n;
                assignTo = newAreaWithNode1 < newAreaWithNode2 ? node1 : node2;
            }
        }

        if (greatestAreaDif == 0) assignTo = resolveTies(node1, node2);

        elementsToSplit.remove(nextElementIndex);
        return new RTreeNode[]{assign, assignTo};
    }

    private RTreeNode resolveTies(RTreeNode node1, RTreeNode node2) {
        float area1 = getArea(node1.getCoordinates());
        float area2 = getArea(node2.getCoordinates());

        RTreeNode assignTo;
        if (area1 == area2) {
            int entries1 = node1.getChildren().size();
            int entries2 = node2.getChildren().size();

            if (entries1 == entries2) {
                assignTo = node1;
            } else {
                assignTo = entries1 < entries2 ? node1 : node2;
            }
        } else {
            assignTo = area1 < area2 ? node1 : node2;
        }
        return assignTo;
    }

    private RTreeNode[] splitNodeLinearCost(RTreeNode node) {
        ArrayList<RTreeNode> elementsToSplit = new ArrayList<>(node.getChildren());
        int[] furthestPair = new int[2];
        float furthestSeparation = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < numberOfCoordinates; i += 2) {
            int[] currentFurthestPairIndices = new int[2];
            float leftmostRightSide = Float.POSITIVE_INFINITY;
            float rightmostLeftSide = Float.NEGATIVE_INFINITY;
            float leftmostSide = Float.POSITIVE_INFINITY;
            float rightmostSide = Float.NEGATIVE_INFINITY;

            for (int j = 0; j < elementsToSplit.size(); j++) {
                if (elementsToSplit.get(j).getCoordinates()[i] > rightmostLeftSide) {
                    rightmostLeftSide = elementsToSplit.get(j).getCoordinates()[i];
                    currentFurthestPairIndices[0] = j;
                }
                if (elementsToSplit.get(j).getCoordinates()[i + 1] < leftmostRightSide) {
                    leftmostRightSide = elementsToSplit.get(j).getCoordinates()[i + 1];
                    currentFurthestPairIndices[1] = j;
                }
                if (elementsToSplit.get(j).getCoordinates()[i] < leftmostSide) {
                    leftmostSide = elementsToSplit.get(j).getCoordinates()[i];
                }
                if (elementsToSplit.get(j).getCoordinates()[i + 1] > rightmostSide) {
                    rightmostSide = elementsToSplit.get(j).getCoordinates()[i + 1];
                }
            }

            float totalWidth = Math.abs(rightmostSide - leftmostSide);
            float normalizedWidth = Math.abs(rightmostLeftSide - leftmostRightSide) / totalWidth;

            if (normalizedWidth > furthestSeparation && currentFurthestPairIndices[0] != currentFurthestPairIndices[1]) {
                furthestSeparation = normalizedWidth;
                furthestPair = currentFurthestPairIndices;
            }
        }

        if (furthestPair[0] == furthestPair[1]) {
            throw new RuntimeException("Same element exists twice"); // Can only happen if two elements are completely on top of each other.
        }

        return distributeNodesLinearCost(node, furthestPair, elementsToSplit);
    }

    private RTreeNode[] distributeNodesLinearCost(RTreeNode node, int[] furthestPair, ArrayList<RTreeNode> elementsToSplit) {
        RTreeNode newNode = new RTreeNode(createNewCoordinateArray(), node.isLeaf(), minimumChildren, maximumChildren, node.getParent());
        updateNodes(node, newNode, elementsToSplit.get(furthestPair[0]), elementsToSplit.get(furthestPair[1]));
        removeElementsFromElementsToSplit(elementsToSplit, furthestPair[0], furthestPair[1]);

        for (int i = 0; i < elementsToSplit.size(); i++) {
            if (i % 2 == 0) {
                node.addChild(elementsToSplit.get(i));
            } else {
                newNode.addChild(elementsToSplit.get(i));
            }
        }
        return new RTreeNode[]{node, newNode};
    }

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

    private int[] pickSeedsQuadratic(ArrayList<RTreeNode> elementsToSplit) {
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
            System.out.println("Level: " + level);
            for (RTreeNode r : result.get(level)) {
                if (r.getParent() == null) {
                    System.out.println("Node Coordinates: " + Arrays.toString(r.getCoordinates()) + " Parent: " + null + " leaf: " + r.isLeaf());
                } else {
                    System.out.println("Node Coordinates: " + Arrays.toString(r.getCoordinates()) + " Parent Coordinates: " + Arrays.toString(r.getParent().getCoordinates()) + " leaf: " + r.isLeaf());
                }
            }
            level++;
        }
    }

    private HashMap<Integer, ArrayList<RTreeNode>> getPrintTree(RTreeNode theRoot, int level, HashMap<Integer, ArrayList<RTreeNode>> result) {
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
