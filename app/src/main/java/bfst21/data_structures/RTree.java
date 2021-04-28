package bfst21.data_structures;

import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import bfst21.view.MapCanvas;
import javafx.geometry.Point2D;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class RTree implements Serializable {
    @Serial private static final long serialVersionUID = 7154862203691144752L;

    private final int minimumChildren, maximumChildren, numberOfCoordinates;
    ArrayList<ArrayList<Long>> splitInsertResults;
    private RTreeNode root;
    private long size;
    private int returnListSize;

    public RTree(int minimumChildren, int maximumChildren, int numberOfCoordinates, int returnListSize) {
        this.minimumChildren = minimumChildren;
        this.maximumChildren = maximumChildren;
        this.numberOfCoordinates = numberOfCoordinates;
        root = null;
        size = 0;
        splitInsertResults = new ArrayList<>();
        this.returnListSize = returnListSize;
    }

    /**
     * Used to create a new coordinate array with the maximum MMB values for a new node. Ensures that the new node's MMB coordinates
     * afterward can be updated correctly.
     *
     * @return A float array with the min x, max x, min y, and min y coordinates.
     */
    private float[] createNewCoordinateArray() {
        return new float[]{Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
    }

    /**
     * Recursively searches the R-tree by going through the nodes whose minimum bounding box intersects with the search bounds.
     * If debug is selected, elements to visualize the elements' minimum bounding boxes are also created along with a rectangle to visualize the canvas bounds.
     *
     * @param xMin  The minimum x-coordinate of the canvas.
     * @param xMax  The maximum x-coordinate of the canvas.
     * @param yMin  The minimum y-coordinate of the canvas.
     * @param yMax  The maximum y-coordinate of the canvas.
     * @param debug True if debug mode is selected. Otherwise, false.
     * @param currentZoomLevel The current zoom level for the GUI.
     * @return An ArrayList with the Element objects that intersect with the search bounds.
     */
    public ArrayList<ArrayList<Element>> search(float xMin, float xMax, float yMin, float yMax, boolean debug, int currentZoomLevel) {
        Map<String, Byte> zoomMap = MapCanvas.zoomMap;
        if (root != null) {
            float[] searchCoordinates = new float[]{xMin, xMax, yMin, yMax};
            ArrayList<ArrayList<Element>> results = prepareResultArray();
            if (debug) {
                float coordinateChange = xMin * 0.0005f;
                searchCoordinates = new float[]{xMin + coordinateChange, xMax + (-coordinateChange), yMin + coordinateChange, yMax + (-coordinateChange)};
                results.get(0).addAll(createDebugRectangle(searchCoordinates, "motorway"));
                searchDebug(searchCoordinates, root, results, currentZoomLevel, zoomMap);
            } else {
                search(searchCoordinates, root, results, currentZoomLevel, zoomMap);
            }
            return results;
        } else {
            throw new RuntimeException("No elements in the RTree");
        }
    }

    /**
     * Make a list with as many lists as layers for the map.
     * @return An ArrayList with empty nested Arraylists.
     */
    private ArrayList<ArrayList<Element>> prepareResultArray() {
        ArrayList<ArrayList<Element>> results = new ArrayList<>();
        while (results.size() <= returnListSize) {
            results.add(new ArrayList<>());
        }
        return results;
    }

    /**
     * Search for elements in the R-tree based on search coordinates.
     * @param searchCoordinates The coordinates to search for elements within where the minimum coordinate is followed by maximum for each dimension.
     * @param node The current Node to check.
     * @param results List with elements that are within the search coordinates.
     * @param currentZoomLevel The current zoom level for the GUI.
     * @param zoomMap A map with types as keys and the layers where the types should be drawn as values.
     */
    private void search(float[] searchCoordinates, RTreeNode node, ArrayList<ArrayList<Element>> results, int currentZoomLevel, Map<String, Byte> zoomMap) {
        if (node.isLeaf()) {
            for (RTreeNode r : node.getChildren()) {
                for (Element e : r.getElementEntries()) {
                    String type = e.getType();
                    if (zoomMap.get(type) != null && zoomMap.get(type) <= currentZoomLevel && intersects(searchCoordinates, e.getCoordinates())) {
                        int layer = e.getLayer();
                        results.get(layer).add(e);
                    }
                }
            }
        } else {
            for (RTreeNode r : node.getChildren()) {
                if (intersects(searchCoordinates, r.getCoordinates())) {
                    search(searchCoordinates, r, results, currentZoomLevel, zoomMap);
                }
            }
        }
    }

    /**
     * Search for elements in the R-tree based on search coordinates and add elements which visualize the r-tree. Separate method from
     * search to avoid extra checks in the original method.
     * @param searchCoordinates The coordinates to search for elements within where the minimum coordinate is followed by maximum for each dimension.
     * @param node The current Node to check.
     * @param results List with elements that are within the search coordinates.
     * @param currentZoomLevel The current zoom level for the GUI.
     * @param zoomMap A map with types as keys and the layers where the types should be drawn as values.
     */
    private void searchDebug(float[] searchCoordinates, RTreeNode node, ArrayList<ArrayList<Element>> results, int currentZoomLevel, Map<String, Byte> zoomMap) {
        if (node.isLeaf()) {
            for (RTreeNode r : node.getChildren()) {
                for (Element e : r.getElementEntries()) {
                    String type = e.getType();
                    if (zoomMap.get(type) != null && zoomMap.get(type) <= currentZoomLevel && intersects(searchCoordinates, e.getCoordinates())) {
                        int layer = e.getLayer();
                        results.get(layer).addAll(createDebugRectangle(e.getCoordinates(), "residential"));
                        results.get(layer).add(e);
                    }
                }
            }
        } else {
            for (RTreeNode r : node.getChildren()) {
                if (intersects(searchCoordinates, r.getCoordinates())) {
                    //results.add(createDebugRelation(r.getCoordinates())); // TODO: 4/5/21 Decide if the r-tree node' boxes also should be drawn (non-leaf nodes) 
                    searchDebug(searchCoordinates, r, results, currentZoomLevel, zoomMap);
                }
            }
        }
    }

    /**
     * Determine if two coordinate bounding boxes intersect.
     * @param coordinates1 The first coordinate set with minimum followed by maximum for each dimension.
     * @param coordinates2 The second coordinate set with minimum followed by maximum for each dimension.
     * @return True if the bounding boxes intersect. False if not.
     */
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

    /**
     * Check if the minimum coordinate from the first element and the maximum coordinate for the second element of a certain dimension do not intersect.
     * @param minCoordinateFirstElement The first element's minimum coordinate for the current dimension.
     * @param maxCoordinateSecondElement The second element's minimum coordinate for the current dimension.
     * @return True if the coordinates do not intersect. False if they intersect.
     */
    private boolean specificCoordinatesDoesNotIntersect(float minCoordinateFirstElement, float maxCoordinateSecondElement) {
        return minCoordinateFirstElement >= maxCoordinateSecondElement;
    }

    /**
     * Creates a Way for the debug visualization mode.
     * @param firstCoordinate The first coordinate for the start of the way.
     * @param secondCoordinate The second coordinate for the start of the way.
     * @param thirdCoordinate The first coordinate for the end of the way.
     * @param fourthCoordinate The second coordinate for the end of the way.
     * @return A Way with two Nodes.
     */
    private Way createDebugWay(float firstCoordinate, float secondCoordinate, float thirdCoordinate, float fourthCoordinate) {
        Way w = new Way();
        w.addNode(new Node(firstCoordinate, secondCoordinate));
        w.addNode(new Node(thirdCoordinate, fourthCoordinate));
        return w;
    }

    /**
     * Creates a rectangle which acts as pseudo canvas bounds when using the debug mode.
     * @param searchCoordinates The coordinates for the rectangle.
     * @return A list with four ways which make up the rectangle.
     */
    private ArrayList<Way> createDebugRectangle(float[] searchCoordinates, String type) {
        ArrayList<Way> ways = new ArrayList<>();
        ways.add(createDebugWay(searchCoordinates[0], searchCoordinates[2], searchCoordinates[0], searchCoordinates[3]));
        ways.add(createDebugWay(searchCoordinates[0], searchCoordinates[2], searchCoordinates[1], searchCoordinates[2]));
        ways.add(createDebugWay(searchCoordinates[1], searchCoordinates[2], searchCoordinates[1], searchCoordinates[3]));
        ways.add(createDebugWay(searchCoordinates[0], searchCoordinates[3], searchCoordinates[1], searchCoordinates[3]));
        for (Way w : ways) {
            w.setType(type);
        }
        return ways;
    }

    /**
     * Insert an Element into the R-tree by choosing a leaf, which requires the smallest MMB increase. Afterward, checks if it is necessary to
     * create a new root, split nodes, and adjust MMB coordinates.
     *
     * @param element The Element object to insert.
     */
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

    /**
     * Recursively chooses a leaf where a new element can be placed. The method finds the child whose MMB requires the least increase
     * if the new element is inserted.
     *
     * @param element The Element object to insert.
     * @param node    The current node whose children should be checked.
     * @return RTreeNode which is a leaf where the new element can be placed.
     */
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

    /**
     * Checks if a node has more elements than the maximum allowed. If yes, the node is split and the tree is adjusted.
     *
     * @param node The Node object to check for overflow.
     */
    private void checkOverflow(RTreeNode node) {
        if (node.overflow()) { // TODO: 3/31/21 Decide on split method 
            //RTreeNode[] result = splitNodeShuffle(node);
            //RTreeNode[] result = splitNodeQuadraticCost(node);
            RTreeNode[] result = splitNodeLinearCost(node);
            adjustTree(result[0], result[1]);
            checkOverflow(node.getParent());
        } else {
            adjustTree(node, null);
        }
    }

    /**
     * Recursively adjusts RTreeNode(s) to ensure that MMBs have the right coordinates and a new root is created if necessary.
     *
     * @param firstNode  The first node to adjust.
     * @param secondNode The second node to adjust. Might be null if it is only the root or a single node that should be adjusted.
     */
    private void adjustTree(RTreeNode firstNode, RTreeNode secondNode) {
        if (firstNode.getParent() == null && (secondNode == null || firstNode == secondNode)) { // only the root should be updated
            updateNodeMMBCoordinates(firstNode);
        } else if (firstNode.getParent() == null && secondNode != null) { // need to join two nodes under one root and adjust the root
            createNewRoot(firstNode, secondNode);
            adjustTree(root, null);
        } else if (secondNode == null) { // only one node to adjust
            updateNodeMMBCoordinates(firstNode);
            adjustTree(firstNode.getParent(), null);
        } else { // two nodes to adjust
            updateNodeMMBCoordinates(firstNode);
            updateNodeMMBCoordinates(secondNode);
            adjustTree(firstNode.getParent(), secondNode.getParent());
        }
    }

    /**
     * Creates a new root for two nodes and thus grows the R-tree's height by one.
     *
     * @param firstNode  The first RTreeNode.
     * @param secondNode The second RTreeNode.
     */
    private void createNewRoot(RTreeNode firstNode, RTreeNode secondNode) {
        RTreeNode newRoot = new RTreeNode(createNewCoordinateArray(), false, minimumChildren, maximumChildren, null);
        newRoot.addChild(firstNode);
        newRoot.addChild(secondNode);
        root = newRoot;
        adjustTree(firstNode, secondNode);
    }

    /**
     * Updates a node's MMB coordinates to ensure that all its children's coordinates are included.
     *
     * @param node The RtreeNode whose coordinates should be updated.
     */
    private void updateNodeMMBCoordinates(RTreeNode node) {
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

    /**
     * Splits a node's children by first shuffling the children ArrayList and then inserting them into two new nodes.
     * The method reuses the old node by removing its children and inserting the new ones. The other children are inserted into a new node.
     *
     * @param node The RTreeNode that needs to be split.
     * @return An array with the two new RTree nodes.
     */
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
            // set the parent of the new node to the old node's parent
            node.getParent().addChild(newNode);
        }
        return new RTreeNode[]{node, newNode};
    }

    /**
     * Splits a node by finding the two children nodes who if put together would create the biggest MMB.
     * Then the rest of the elements are distributed based on which assignment would lead to the smallest MMB increase.
     *
     * @param node The RTreeNode to split.
     * @return An array with the two new RTree nodes.
     */
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

    /**
     * Determines the two nodes, which if put in the same node would lead to the biggest MMB.
     *
     * @param elementsToSplit An ArrayList with RTreeNodes that should be split.
     * @return The indices of the nodes that would create the biggest MMB.
     */
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

    /**
     * Remove elements based on their indices from the list of elements.
     *
     * @param elementsToSplit An ArrayList with the RTreeNodes that need to be split.
     * @param index1          The first index to remove an RTreeNode from.
     * @param index2          The second index to remove an RTreeNode from.
     */
    private void removeElementsFromElementsToSplit(ArrayList<RTreeNode> elementsToSplit, int index1, int index2) {
        if (index1 < index2) {
            elementsToSplit.remove(index2);
            elementsToSplit.remove(index1);
        } else {
            elementsToSplit.remove(index1);
            elementsToSplit.remove(index2);
        }
    }

    /**
     * Update two nodes by removing children and updating with the new children.
     * The parent of the new node is also updated to match the other node.
     *
     * @param node              The first node to assign children to. The node is reused and thus has its old children removed.
     * @param newNode           The new node to assign children to.
     * @param elementForNode    The children for the first node.
     * @param elementForNewNode The children for the second node.
     */
    private void updateNodes(RTreeNode node, RTreeNode newNode, RTreeNode elementForNode, RTreeNode elementForNewNode) {
        node.removeChildren();
        node.addChild(elementForNode);
        node.updateCoordinate(createNewCoordinateArray());
        newNode.addChild(elementForNewNode);
        if (node.getParent() != null) node.getParent().addChild(newNode);
    }

    /**
     * Distributes the remaining elements. If one of the nodes needs the rest of the elements to have the minimum number of elements,
     * the elements are assigned to it. Otherwise, the elements are assigned based on which assignment would create the smallest MMB.
     *
     * @param elementsToSplit An ArrayList of RTreeNodes to split.
     * @param node            The first node to assign elements to.
     * @param newNode         The second node to assign elements to.
     */
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

    /**
     * Checks if an element has less than the minimum children amount and if there are not more elements left than the number
     * required to reach the minimum number of elements.
     *
     * @param numberOfChildren The number of children in a node.
     * @param elementsLeft     The number of elements left to split.
     * @return True, if the rest of the elements should be inserted into the node. Otherwise, false.
     */
    private boolean tooFewEntries(int numberOfChildren, int elementsLeft) {
        return numberOfChildren < minimumChildren && elementsLeft <= minimumChildren - numberOfChildren;
    }

    /**
     * Picks the next node to assign an element to. Finds the requires MMB increase if an element is inserted into the two nodes.
     * This is repeated for all the nodes and the element with the greatest difference between the two MMb increases are chosen
     * and assigned to the node requiring the smallest MMB increase.
     *
     * @param elementsToSplit An ArrayList with RTreeNodes to split.
     * @param node1           The first RTreeNode, which the elements can be placed in.
     * @param node2           The second RTreeNode, which the elements can be placed in.
     * @return An array with the two input nodes with updated children.
     */
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

        // If the greatest difference is zero, then we need to resolve the tie.
        if (greatestAreaDif == 0) assignTo = resolveTies(node1, node2);

        elementsToSplit.remove(nextElementIndex);
        return new RTreeNode[]{assign, assignTo};
    }

    /**
     * Resolves a tie where inserting an element would require the same MMB increase.
     * Choose the node with the smallest MMB area. If they have the same, choose the one with the smallest number of entries.
     * Otherwise, assign to the first node.
     *
     * @param node1 The first RTreeNode.
     * @param node2 The second RTreeNode.
     * @return The RTreeNode where the element should be inserted.
     */
    private RTreeNode resolveTies(RTreeNode node1, RTreeNode node2) {
        float area1 = getArea(node1.getCoordinates());
        float area2 = getArea(node2.getCoordinates());

        RTreeNode assignTo;
        if (area1 == area2) {
            int entries1 = node1.getChildren().size();
            int entries2 = node2.getChildren().size();

            if (entries1 == entries2) {
                // assign to the first node.
                assignTo = node1;
            } else {
                // assign to the one with the smallest number of entries.
                assignTo = entries1 < entries2 ? node1 : node2;
            }
        } else {
            // assign to the one with the smallest area.
            assignTo = area1 < area2 ? node1 : node2;
        }
        return assignTo;
    }

    /**
     * Splits a node's children by finding the element pair whose normalized distance between the rightmost left side
     * and the leftmost right side is the greatest. The method checks all dimensions and the normalized distance is
     * found by dividing by the length of the entire set on that dimension. Afterward, the rest of the nodes are
     * distributed.
     *
     * @param node The RTreeNode to split.
     * @return An array with two RTreeNodes with updated children.
     */
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
                if (elementsToSplit.get(j).getCoordinates()[i] > rightmostLeftSide) { // for the rightmost left side
                    rightmostLeftSide = elementsToSplit.get(j).getCoordinates()[i];
                    currentFurthestPairIndices[0] = j;
                }
                if (elementsToSplit.get(j).getCoordinates()[i + 1] < leftmostRightSide) { // for the leftmost right side
                    leftmostRightSide = elementsToSplit.get(j).getCoordinates()[i + 1];
                    currentFurthestPairIndices[1] = j;
                }
                if (elementsToSplit.get(j).getCoordinates()[i] < leftmostSide) { // for the greatest width
                    leftmostSide = elementsToSplit.get(j).getCoordinates()[i];
                }
                if (elementsToSplit.get(j).getCoordinates()[i + 1] > rightmostSide) { // for the greatest width
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

        if (elementsToSplit.get(furthestPair[0]) == elementsToSplit.get(furthestPair[1])) {
            throw new RuntimeException("Same element exists twice"); // Can only happen if two elements are completely on top of each other. // TODO: 4/27/21 FIXXXXXX 
        }
        return distributeNodesLinearCost(node, furthestPair, elementsToSplit);
    }

    /**
     * Distributes the rest of the nodes to split by going through the list and either assigning them to the first or the second node-
     *
     * @param node            The node that can be reused.
     * @param furthestPair    The indices of the pair of elements that are furthest from each other.
     * @param elementsToSplit An ArrayList with the RTreeNodes to split.
     * @return An array with two RTreeNodes and their updated children-
     */
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

    /**
     * Get the area of a MMB.
     *
     * @param coordinates The MMB's coordinates.
     * @return The area as a float.
     */
    private float getArea(float[] coordinates) {
        float area = 1;
        for (int i = 0; i < numberOfCoordinates - 1; i += 2) {
            area *= (coordinates[i + 1] - coordinates[i]);
        }
        return Math.abs(area);
    }

    /**
     * Determines the updated MMB area if an element is inserted into a different element.
     *
     * @param coordinatesFirstNode The coordinates of the first element.
     * @param coordinateSecondNode The coordinates of the second element.
     * @return The new area as a float.
     */
    private float getNewBoundingBoxArea(float[] coordinatesFirstNode, float[] coordinateSecondNode) {
        float[] newCoordinates = new float[numberOfCoordinates];

        for (int i = 0; i < numberOfCoordinates; i += 2) {
            newCoordinates[i] = Math.min(coordinatesFirstNode[i], coordinateSecondNode[i]);
            newCoordinates[i + 1] = Math.max(coordinatesFirstNode[i + 1], coordinateSecondNode[i + 1]);
        }
        return getArea(newCoordinates);
    }

    /**
     * Finds the area difference between two MMB.
     *
     * @param firstCoordinates  The first coordinates.
     * @param secondCoordinates The second coordinates.
     * @return The area difference as a float.
     */
    private float findAreaDifference(float[] firstCoordinates, float[] secondCoordinates) {
        float areaWithBoth = getNewBoundingBoxArea(firstCoordinates, secondCoordinates);
        float areaElement1 = getArea(firstCoordinates);
        float areaElement2 = getArea(secondCoordinates);
        return areaWithBoth - areaElement1 - areaElement2;
    }

    /**
     * Visualizes the R-Tree by printing the nodes, their information, and their level.
     */
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

    public void splitMethodInsertTest(Element element) {
        size++;
        ArrayList<Long> currentResult = new ArrayList<>();
        currentResult.add(size);

        long start = System.nanoTime();
        insert(element);
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        currentResult.add(timeElapsed);

        splitInsertResults.add(currentResult);
    }

    // TODO: 4/21/21 move name to nodeholder to avoid cast
    // TODO: 4/22/21  get rid of [] part and use element coor immediately
    /*public Way getNearestRoad(float x, float y) {
        // Hjaltason, Gísli, and Hanan Samet. “Distance Browsing in Spatial Databases.” ACM transactions on database systems 24.2 (1999): 265–318. Web.
        //System.out.println(x + " " + y);
        //x = 10.526522f;
        //y = -98.54738f;
        PriorityQueue<PriorityQueueEntry> pq = new PriorityQueue<>();
        pq.add(new PriorityQueueEntry(true, root, null, 0));

        while (!pq.isEmpty()) {
            PriorityQueueEntry entry = pq.poll();

            if (!entry.isRTreeNode) {
                //System.out.println("");
                while (entry.element == pq.peek().element) {
                    pq.poll();
                }
                /*PriorityQueueEntry another = pq.poll();
                double diAnother = another.distance;
                double entryDi = entry.distance;
                //System.out.println(entryDi < diAnother);

                return (Way) entry.element;/*
                Way way = (Way) entry.element;
                if (!pq.isEmpty() && distanceToElement(x, y, way) > pq.peek().distance) {
                    pq.add(new PriorityQueueEntry(false, null, entry.element, distanceToElement(x, y, way)));
                } else {
                    return way;
                }
            } else if (entry.rTreeNode.isLeaf()) {
                for (RTreeNode n : entry.rTreeNode.getChildren()) {
                    for (Element e : n.getElementEntries()) {
                        if (e instanceof Way) {
                            Way w = (Way) e;
                            if (w.isHighWay() && w.hasName()) {
                                ArrayList<Way> segments = createWaySegments(w);
                                for (Way segment : segments) {
                                    //if (distanceToElement(x, y, segment) >= minDistMBB(x, y, n.getCoordinates())) { // TODO: 4/22/21 ???
                                        pq.add(new PriorityQueueEntry(false, null, segment, distanceToElement(x, y, segment)));
                                    //}
                                }
                            }
                        }
                    }
                }
            } else {
                for (RTreeNode node : entry.rTreeNode.getChildren()) {
                    pq.add(new PriorityQueueEntry(true, node, null, minDistMBB(x, y, entry.rTreeNode.getCoordinates())));
                }
            }
        }
        return null;
    }*/

    // TODO: 4/21/21 move name to nodeholder to avoid cast
    // TODO: 4/22/21  get rid of [] part and use element coor immediately
    public Way getNearestRoad(float x, float y) {
        // Hjaltason, Gísli, and Hanan Samet. “Distance Browsing in Spatial Databases.” ACM transactions on database systems 24.2 (1999): 265–318. Web.
        //x = 10.530582f;
        //y = -98.55807f;
        //test(x, y);
        //System.out.println(x + " " + y);
        PriorityQueue<PriorityQueueEntry> pq = new PriorityQueue<>();
        pq.add(new PriorityQueueEntry(true, false, root, null, 0));

        while (!pq.isEmpty()) {
            PriorityQueueEntry entry = pq.poll();

            if (!entry.isRTreeNode || entry.isBoundingRectangle) {
                if (entry.isBoundingRectangle && !pq.isEmpty() && distanceToElement(x, y, (Way) entry.element) > pq.peek().distance) {
                    pq.add(new PriorityQueueEntry(false, false, null, entry.element, distanceToElement(x, y, (Way) entry.element)));
                } else {
                    /*double dis1 = distanceToElement(x, y, (Way) entry.element);
                    double disbb1 = minDistMBB(x, y, entry.element.getCoordinates());
                    PriorityQueueEntry entry2 = pq.poll();
                    double dis2 = distanceToElement(x, y, (Way) entry2.element);
                    double disbb2 = minDistMBB(x, y, entry2.element.getCoordinates());*/

                    return (Way) entry.element;
                }
            } else if (entry.rTreeNode.isLeaf()) {
                for (RTreeNode n : entry.rTreeNode.getChildren()) {
                    for (Element e : n.getElementEntries()) {
                        if (e instanceof Way) {
                            Way w = (Way) e;
                            if (w.isHighWay() && w.hasName()) { // TODO: 4/22/21 highway?
                                pq.add(new PriorityQueueEntry(false, true, null, e, minDistMBB(x, y, e.getCoordinates())));
                            }
                        }
                    }
                }
            } else {
                for (RTreeNode node : entry.rTreeNode.getChildren()) {
                    pq.add(new PriorityQueueEntry(true, false, node, null, minDistMBB(x, y, entry.rTreeNode.getCoordinates())));
                }
            }
        }
        return null;
    }

    public ArrayList<Way> createWaySegments(Way w) {
        ArrayList<Way> segments = new ArrayList<>();
        for (int i = 0; i < w.getNodes().size() - 1; i++) {
            Way segment = new Way();
            segment.setAsHighWay();
            segment.setName(w.getName());
            segment.addNode(w.getNodes().get(i));
            segment.addNode(w.getNodes().get(i + 1));
            segments.add(segment);
        }
        return segments;
    }

    public void test(float x, float y) {
        Way min = test(x, y, root, Double.POSITIVE_INFINITY, null);
        System.out.println(min);
    }

    private Way test(float x, float y, RTreeNode node, double minDistance, Way way) {
        if (node == null) {
            return way;
        }
        if (node.isLeaf()) {
            for (RTreeNode n : node.getChildren()) {
                for (Element e : n.getElementEntries()) {
                    if (e instanceof Way) {
                        Way w = (Way) e;
                        if (w.isHighWay() && w.hasName()) {
                            double distance = distanceToElement(x, y, w);
                            if (distance < minDistance) {
                                minDistance = distance;
                                way = w;
                            }
                        }
                    }
                }
            }
        } else {
            for (RTreeNode n : node.getChildren()) {
                return test(x, y, n, minDistance, null);
            }
        }
        return way;
    }

    private double distanceToElement(float queryX, float queryY, Way way) {

        double minDistance = Double.POSITIVE_INFINITY;

        List<Node> nodes = way.getNodes();

        for (int i = 0; i < nodes.size() - 1; i++) {
            Point2D firstNode = new Point2D(nodes.get(i).getxMin(), nodes.get(i).getyMin());
            Point2D lastNode = new Point2D(nodes.get(i + 1).getxMin(), nodes.get(i + 1).getyMin());

            double numerator = Math.abs(((lastNode.getX() - firstNode.getX() ) * (firstNode.getY() - queryY)) - ((firstNode.getX() - queryX) * (lastNode.getY() - firstNode.getY())));
            double denominator = Math.sqrt(Math.pow(lastNode.getX() - firstNode.getX(), 2) + Math.pow(lastNode.getY() - firstNode.getY(), 2));
            double distance = numerator / denominator;
            if (distance < minDistance) minDistance = distance;
        }

        return minDistance;
    }


    private double minDistMBB(float queryX, float queryY, float[] coor) {
        double dx = Math.max(coor[0] - queryX, Math.max(0, queryX - coor[1]));
        double dy = Math.max(coor[2] - queryY, Math.max(0, queryY - coor[3]));
        return Math.sqrt(dx * dx + dy * dy);
    }

    private class PriorityQueueEntry implements Comparable<PriorityQueueEntry> {
        private boolean isRTreeNode;
        private boolean isBoundingRectangle;
        private RTreeNode rTreeNode;
        private Element element;
        private double distance;

        public PriorityQueueEntry(boolean isRTreeNode, boolean isBoundingRectangle, RTreeNode rTreeNode, Element element, double distance) {
        //public PriorityQueueEntry(boolean isRTreeNode, RTreeNode rTreeNode, Element element, double distance) {
            this.isRTreeNode = isRTreeNode;
            this.isBoundingRectangle = isBoundingRectangle;
            this.rTreeNode = rTreeNode;
            this.element = element;
            this.distance = distance;
        }

        @Override
        public int compareTo(PriorityQueueEntry e) {
            return Double.compare(distance, e.distance);
        }
    }
}
