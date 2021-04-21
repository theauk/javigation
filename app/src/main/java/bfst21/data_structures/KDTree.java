package bfst21.data_structures;

import bfst21.Exceptions.KDTreeEmptyException;
import bfst21.Osm_Elements.Element;
import javafx.geometry.Point2D;

import java.util.*;

public class KDTree<Value extends Element> {
    private final Comparator<KDTreeNode> comparatorX = new Comparator<KDTreeNode>() {
        @Override
        public int compare(KDTreeNode p1, KDTreeNode p2) {
            return Float.compare(p1.node.getxMax(), p2.node.getxMax());
        }
    };
    private final Comparator<KDTreeNode> comparatorY = new Comparator<KDTreeNode>() {
        @Override
        public int compare(KDTreeNode p1, KDTreeNode p2) {
            int c = Float.compare(p1.node.getyMax(), p2.node.getyMax());
            return c;
        }
    };
    private KDTreeNode root;
    private List<KDTreeNode> list;
    private int startDim;
    private int numCor;
    private int numDim;
    private boolean noDuplicates;
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
    public void buildTree(){
        for (Value value : hashList) list.add(new KDTreeNode(value));
        buildTree(list, startDim);
    }
    private KDTreeNode buildTree(List<KDTreeNode> nodes, int dim) {
        if (nodes.isEmpty()) {
            return null;
        }

        Comparator<KDTreeNode> comp = getComparatorFromDimension(dim % numCor);
        nodes.sort(comp);
        if(!noDuplicates){
            noDuplicates = true;
        }

        int med = getMedian(0, nodes.size());
        KDTreeNode medNode = nodes.get(med);
        medNode.onXAxis = dim % numCor == 0;

        if (root == null) {
            root = medNode;
        }

        medNode.leftChild = buildTree(nodes.subList(0, med), dim + numDim);
        medNode.rightChild = buildTree(nodes.subList(med + 1, nodes.size()), dim + numDim);

        return medNode;
    }


    public Value getNearestNode(float x, float y) throws KDTreeEmptyException {

        KDTreeNode nearestNode = getNearestNode(x, y, root, null);
        return nearestNode.node;
    }

    private boolean possibleCloserNode(Double shortestDistance, KDTreeNode currentNode, float x, float y) {
        double possibleNewDistance = Math.abs(currentNode.onXAxis ? x - currentNode.node.getxMax() : y - currentNode.node.getyMax());
        return shortestDistance > Math.abs(possibleNewDistance);
    }

    private double getDistance(KDTreeNode from, float[] cor) {
        if (from == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            Point2D p = new Point2D(cor[0], cor[1]);
            return p.distance(from.node.getxMax(), from.node.getyMax());
        }
    }

    private KDTreeNode getNearestNode(float x, float y, KDTreeNode currentNode, KDTreeNode nearestNode) {
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
        if (currentNode.onXAxis) {
            isCoordinateLessThan = x < currentNode.node.getxMax();
        } else {
            isCoordinateLessThan = y < currentNode.node.getyMax();
        }

        if (isCoordinateLessThan) {
            currentNearest = getNearestNode(x, y, currentNode.leftChild, currentNearest);
            if (possibleCloserNode(getDistance(currentNearest, new float[]{x, y}), currentNode, x, y)) {
                currentNearest = getNearestNode(x, y, currentNode.rightChild, currentNearest);
            }
        } else {
            currentNearest = getNearestNode(x, y, currentNode.rightChild, currentNearest);
            if (possibleCloserNode(getDistance(currentNearest, new float[]{x, y}), currentNode, x, y)) {
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

    private class KDTreeNode {
        private Value node;
        private KDTreeNode leftChild;
        private KDTreeNode rightChild;
        private boolean onXAxis;

        public KDTreeNode(Value node) {
            this.node = node;
        }
    }
}