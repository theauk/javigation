package bfst21.data_structures;

import bfst21.Exceptions.KDTreeEmptyException;
import bfst21.Osm_Elements.Element;
import javafx.geometry.Point2D;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class KDTree<Value extends Element> implements Serializable {
    @Serial
    private static final long serialVersionUID = 2546489468741939125L;

    private final transient Comparator<KDTreeNode> comparatorX = new Comparator<KDTreeNode>() {
        @Override
        public int compare(KDTreeNode p1, KDTreeNode p2) {
            return Float.compare(p1.node.getxMax(), p2.node.getxMax());
        }
    };
    private final transient Comparator<KDTreeNode> comparatorY = new Comparator<KDTreeNode>() {
        @Override
        public int compare(KDTreeNode p1, KDTreeNode p2) {
            return Float.compare(p1.node.getyMax(), p2.node.getyMax());
        }
    };
    private KDTreeNode root;
    private List<KDTreeNode> list;
    private int startDim;
    private int numCor;
    private int numDim;
    private HashSet<Value> hashList;

    public KDTree(int startDim, int numCor) {
        this.startDim = startDim;
        this.numCor = numCor;
        numDim = numCor / 2;
        list = new ArrayList<>();
        hashList = new HashSet<>();
    }

    private Comparator<KDTreeNode> getComparatorFromDimension(int dim) {
        return dim == 0 ? comparatorX : comparatorY;
    }

    private int getMedian(int low, int high) {
        return (low + high) / 2;
    }

    public void addAll(List<Value> nodes) {
        hashList.addAll(nodes);
    }

    public void buildTree() {
        for (Value value : hashList) list.add(new KDTreeNode(value));
        buildTree(list, startDim);
    }

    /**
     * Recursively builds the tree with the list provided. Method only gets called by buildTree().
     * Uses comparators on the list for every recursive call to make sure the tree is built evenly,
     *
     * @param nodes List of nodes that the tree should be built from.
     * @param dim   dimension, meaning x or y axis,
     * @return returns a KDTreeNode
     */
    private KDTreeNode buildTree(List<KDTreeNode> nodes, int dim) {
        if (nodes.isEmpty()) {
            return null;
        }

        Comparator<KDTreeNode> comp = getComparatorFromDimension(dim % numCor); // Chooses x or y comparator.
        nodes.sort(comp);

        int med = getMedian(0, nodes.size());
        KDTreeNode medNode = nodes.get(med);
        medNode.onXAxis = dim % numCor == 0;

        if (root == null) {
            root = medNode;
        }

        medNode.leftChild = buildTree(nodes.subList(0, med), dim + numDim);              // builds the left subtree return the child to this node.
        medNode.rightChild = buildTree(nodes.subList(med + 1, nodes.size()), dim + numDim); // builds the right subtree return the child to this node.

        return medNode;
    }

    /**
     * @param x coordinate
     * @param y coordinate
     * @return Nearest value
     * @throws KDTreeEmptyException throws if the tree is empty
     */
    public Value getNearestNode(float x, float y) throws KDTreeEmptyException {

        KDTreeNode nearestNode = getNearestNode(x, y, root, null);
        return nearestNode.node;
    }

    /**
     * Helper method for getNearest Node
     * Calculates whether or not it is worth checking the 'other side' of the tree for possible closer node.
     * It returns true if the shortest distance is larger than the distance between x or y to the current nodes axis.
     */
    private boolean possibleCloserNode(Double shortestDistance, KDTreeNode currentNode, float x, float y) {
        double possibleNewDistance = Math.abs(currentNode.onXAxis ? x - currentNode.node.getxMax() : y - currentNode.node.getyMax());
        return shortestDistance > Math.abs(possibleNewDistance);
    }

    private double getDistance(KDTreeNode from, float x, float y) {
        if (from == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            Point2D p = new Point2D(x, y);
            return p.distance(from.node.getxMax(), from.node.getyMax());
        }
    }

    /**
     * Recursively finds the nearest node, determining the most promising side and looking there first.
     * @param x coordinate
     * @param y coordinate
     * @param currentNode node to search
     * @param nearestNode nearest node so far.
     * @return returns the nearest node
     */
    private KDTreeNode getNearestNode(float x, float y, KDTreeNode currentNode, KDTreeNode nearestNode) {
        KDTreeNode currentNearest = nearestNode;

        if (currentNode == null) { // end of tree
            return currentNearest;
        }

        double currentDistance = getDistance(currentNode, x, y);
        double minimumDistance = getDistance(currentNearest, x, y);

        if (currentDistance < minimumDistance) {  // if currentnode is closer than currentNearest
            currentNearest = currentNode;
        }

        boolean isCoordinateLessThan;  // finds if the most promising side of the tree is left or right
        if (currentNode.onXAxis) {
            isCoordinateLessThan = x < currentNode.node.getxMax();
        } else {
            isCoordinateLessThan = y < currentNode.node.getyMax();
        }

        if (isCoordinateLessThan) { // searches the most promising side first then checks if it is worth checking the other side.
            currentNearest = getNearestNode(x, y, currentNode.leftChild, currentNearest);
            if (possibleCloserNode(getDistance(currentNearest, x, y), currentNode, x, y)) {
                currentNearest = getNearestNode(x, y, currentNode.rightChild, currentNearest);
            }
        } else {
            currentNearest = getNearestNode(x, y, currentNode.rightChild, currentNearest);
            if (possibleCloserNode(getDistance(currentNearest, x, y), currentNode, x, y)) {
                currentNearest = getNearestNode(x, y, currentNode.leftChild, currentNearest);
            }
        }
        return currentNearest;
    }

    // TODO: 26-03-2021 remove both print methods when no longer needed.
    public void printTree() {
        Integer level = 1;
        HashMap<Integer, ArrayList<KDTreeNode>> result = new HashMap<>();
        result = getPrintTree(root, level, result);

        while (result.get(level) != null) {
            System.out.println("");
            System.out.println("Level: " + level);
            for (KDTreeNode node : result.get(level)) {
                System.out.println("Node id: " + node.node.getId() + " : x: " + node.node.getxMax() + " y: " + node.node.getyMax() + " axis: " + node.onXAxis + " name: ");
                if (node.leftChild != null) {
                    System.out.println("Has left child, id: " + node.leftChild.node.getId() + " name: ");
                }
                if (node.rightChild != null) {
                    System.out.println("Has right child, id: " + node.rightChild.node.getId() + " name: ");
                }
            }
            level++;
        }
        System.out.println("");
    }

    private HashMap<Integer, ArrayList<KDTreeNode>> getPrintTree(KDTreeNode node, Integer level, HashMap<Integer, ArrayList<KDTreeNode>> result) {
        if (node != null) {
            if (result.get(level) == null) {
                ArrayList<KDTreeNode> newAL = new ArrayList<>();
                newAL.add(node);
                result.put(level, newAL);
            } else {
                ArrayList<KDTreeNode> current = result.get(level);
                current.add(node);
                result.put(level, current);
            }
            level += 1;
            int levelCopy = level;

            getPrintTree(node.leftChild, level, result);
            getPrintTree(node.rightChild, levelCopy, result);
        }
        return result;
    }

    private class KDTreeNode implements Serializable {
        @Serial
        private static final long serialVersionUID = -6786678243546431229L;

        private Value node;
        private KDTreeNode leftChild;
        private KDTreeNode rightChild;
        private boolean onXAxis;

        public KDTreeNode(Value node) {
            this.node = node;
        }
    }
}