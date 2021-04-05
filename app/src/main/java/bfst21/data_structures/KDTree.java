package bfst21.data_structures;


import bfst21.Exceptions.KDTreeEmptyException;
import bfst21.Osm_Elements.Element;
import javafx.geometry.Point2D;

import java.util.*;

public class KDTree<Value extends Element> {
    private KDTreeNode root;
    private List<KDTreeNode> list;
    private boolean isSorted;
    private int removes = 0;
    public KDTreeNode theaRoot;
    private int startDim;
    private int numCor;
    private int numDim;
    private int duplicateCount = 0;

    public KDTree(int startDim, int numCor) {
        this.startDim = startDim;
        this.numCor = numCor;
        numDim = numCor / 2;
        list = new ArrayList<>();
        isSorted = false;
    }

    private final Comparator<KDTreeNode> comparatorX = new Comparator<KDTreeNode>() {
        @Override
        public int compare(KDTreeNode p1, KDTreeNode p2) {
            return Double.compare(p1.node.getxMax(), p2.node.getxMax());
        }
    };
    private final Comparator<KDTreeNode> comparatorY = new Comparator<KDTreeNode>() {
        @Override
        public int compare(KDTreeNode p1, KDTreeNode p2) {
            return Double.compare(p1.node.getyMax(), p2.node.getyMax());
        }
    };

    private Comparator<KDTreeNode> getComparatorFromDimension(int dim) {
        return dim == 0 ? comparatorX : comparatorY;
    }

    public int getMedian(int low, int high) {
        return (low + high) / 2; // TODO: 4/4/21 minus one? Otherwise it always takes an index higher?
    }

    public void addAll(String name, List<Value> nodes) {
        for (Value node : nodes) {
            list.add(new KDTreeNode(name, node));
        }
    }

    /*private void buildTree() throws KDTreeEmptyException {
        if (list.isEmpty()) {
            throw new KDTreeEmptyException("No nodes in the kd-tree");
        }
        list.sort(getComparatorFromDimension(startDim));
        int lo = 0;
        int hi = list.size();
        int mid = getMedian(lo, hi);

        root = list.get(mid);
        buildTree(list.subList(mid + 1, hi), 0);
        buildTree(list.subList(0, mid), 0);
    }*/

    public void testBuild() { // TODO: 4/4/21 for debugging
        buildTree(list, startDim, null);
    }

    private KDTreeNode buildTree(List<KDTreeNode> nodes, int dim, KDTreeNode parent) {
        if (nodes.isEmpty()) {
            return null;
        }

        Comparator<KDTreeNode> comp = getComparatorFromDimension(dim % numCor);
        nodes.sort(comp);

        int med = getMedian(0, nodes.size());
        KDTreeNode medNode = nodes.get(med);
        medNode.onXAxis = dim % numCor == 0;

        if (root == null) {
            root = medNode;
        }

        if (medNode == parent) {
            duplicateCount++; // TODO: 4/5/21 Why is it zero? 
        }

        medNode.leftChild = buildTree(nodes.subList(0, med), dim + numDim, medNode);
        medNode.rightChild = buildTree(nodes.subList(med + 1, nodes.size()), dim + numDim, medNode);

        return medNode;
    }

    public String getNearestNode(float x, float y) throws KDTreeEmptyException {
        if (!isSorted) { // TODO: 4/5/21 should preferably be sorted before first search because this way it makes the program freeze momentarily
            buildTree(list, startDim, null);
            System.out.println("Duplicates: " + duplicateCount);
            isSorted = true;
        }

        double shortestDistance = Double.POSITIVE_INFINITY;
        float[] point = new float[]{x, y};
        //KDTreeNode nearestNode = getNearestNode(root, x, y, shortestDistance, null, true);
        //KDTreeNode nearestNode = getNearestNode(point, root, shortestDistance, null, startDim);
        KDTreeNode nearestNode = getNearestNode(x, y, root, null, true);
        return nearestNode.name;

    }

    private boolean possibleCloserNode(Double shortestDistance, KDTreeNode currentNode, float x, float y) {
        double possibleNewDistance = Math.abs(currentNode.onXAxis ? x - currentNode.node.getxMax() : y - currentNode.node.getyMax());
        return shortestDistance > Math.abs(possibleNewDistance);
    }

    private double getDistance(KDTreeNode from, float[] cor) {
        if(from == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            Point2D p = new Point2D(cor[0], cor[1]);
            return p.distance(from.node.getxMax(), from.node.getyMax());
        }
    }

    private KDTreeNode getNearestNode(float x, float y, KDTreeNode currentNode, KDTreeNode nearestNode, boolean xAxis) {
        KDTreeNode currentNearest = nearestNode;

        if (currentNode == null) {
            return currentNearest;
        }

        double currentDistance = getDistance(currentNode, new float[]{x, y});
        double minimumDistance = getDistance(currentNearest, new float[]{x, y});

        if (currentDistance < minimumDistance) {
            currentNearest = currentNode;
        }

        boolean isCoordinateLessThan;
        if(xAxis) {
            isCoordinateLessThan = x < currentNode.node.getxMax();
        } else {
            isCoordinateLessThan = y < currentNode.node.getyMax();
        }

        if (isCoordinateLessThan) {
            currentNearest = getNearestNode(x, y, currentNode.leftChild, currentNearest, !xAxis);
            currentNearest = getNearestNode(x, y, currentNode.rightChild, currentNearest, !xAxis);
        } else {
            currentNearest = getNearestNode(x, y, currentNode.rightChild, currentNearest, !xAxis);
            currentNearest = getNearestNode(x, y, currentNode.leftChild, currentNearest, !xAxis);
        }
        return currentNearest;
    }

    /*private KDTreeNode getNearestNode(KDTreeNode currentNode, float x, float y, double shortestDistance, KDTreeNode nearestNode, Boolean xAxis) {
        if (currentNode == null) {
            return nearestNode;
        }

        double newDistance = getDistance(currentNode, new float[]{x, y});
        if (newDistance < shortestDistance) {
            shortestDistance = newDistance;
            nearestNode = currentNode;
        }

        //checks if we should search the left or right side of the tree first, to save time/space.
        double compare = xAxis ? Math.abs(x) - Math.abs(currentNode.node.getxMax()) : Math.abs(y) - Math.abs(currentNode.node.getyMax());

        KDTreeNode node1 = compare < 0 ? currentNode.leftChild : currentNode.rightChild;
        KDTreeNode node2 = compare < 0 ? currentNode.rightChild : currentNode.leftChild;

        nearestNode = getNearestNode(node1, x, y, shortestDistance, nearestNode, !xAxis);

        // Checks if its worth checking on the other side of tree.
        if (possibleCloserNode(shortestDistance, currentNode, x, y)) {

            nearestNode = getNearestNode(node2, x, y, shortestDistance, nearestNode, !xAxis);
        }
        return nearestNode;

    }*/





    // TODO: 26-03-2021 remove both print methods when no longer needed.
    public void printTree() {
        Integer level = 1;
        HashMap<Integer, ArrayList<KDTreeNode>> result = new HashMap<>();
        result = getPrintTree(root, level, result);

        while (result.get(level) != null) {
            System.out.println("");
            System.out.println("Level: " + level);
            for (KDTreeNode node : result.get(level)) {
                System.out.println("Node id: " + node.node.getId() + " : x: " + node.node.getxMax() + " y: " + node.node.getyMax() + " axis: " + node.onXAxis + " name: " + node.name);
                if (node.leftChild != null) {
                    System.out.println("Has left child, id: " + node.leftChild.node.getId() + " name: " + node.leftChild.name);
                }
                if (node.rightChild != null) {
                    System.out.println("Has right child, id: " + node.rightChild.node.getId()+ " name: " + node.rightChild.name);
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

    private class KDTreeNode {
        private String name;
        private Value node;
        private KDTreeNode leftChild;
        private KDTreeNode rightChild;
        private Boolean onXAxis;


        public KDTreeNode(String name, Value node) {
            this.node = node;
            this.name = name;
        }
    }
}