package bfst21.data_structures;


import bfst21.Exceptions.KDTreeEmptyException;
import bfst21.Osm_Elements.Element;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class KDTree<Value extends Element> {
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
    private KDTreeNode root;
    private List<KDTreeNode> list;
    private boolean isSorted;

    public void addALl(List<Value> nodes) {
        if (list == null) {
            list = new ArrayList<>();
        }
        for (Value node : nodes) {
            add(node);
        }

    }

    public void add(Value node) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(new KDTreeNode(node));
        isSorted = false;
    }

    private void buildTree() throws KDTreeEmptyException {
        if (list == null || list.isEmpty()) {
            throw new KDTreeEmptyException("No nodes in the tree");
        }
        list.sort(comparatorX);
        int lo = 0;
        int hi = list.size();
        int mid = (lo + hi) / 2;

        root = list.get(mid);
        root.onXAxis = true;
        buildTree(false, list.subList(mid + 1, hi), root);
        buildTree(true, list.subList(0, mid), root);
        isSorted = true;


    }

    private void buildTree(boolean isLeft, List<KDTreeNode> nodes, KDTreeNode parent) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        List<KDTreeNode> nodesCopy;

        if (isParentAndNextMidSame(nodes, parent)) {
            removeDuplicates(nodes, (nodes.size()) / 2);
            nodesCopy = new ArrayList<>(nodes);
        } else {
            nodesCopy = nodes;
        }

        if (nodesCopy.isEmpty()) {
            return;
        }

        nodesCopy.sort(parent.onXAxis ? comparatorX : comparatorY);


        KDTreeNode child = nodesCopy.get(nodesCopy.size() / 2);

        child.onXAxis = !parent.onXAxis;

        if (isLeft) {
            parent.leftChild = child;
        } else {
            parent.rightChild = child;
        }

        buildTree(true, nodesCopy.subList(0, nodesCopy.size() / 2), child);
        if ((nodesCopy.size() / 2) + 1 < nodesCopy.size()) {
            buildTree(false, nodesCopy.subList(nodesCopy.size() / 2 + 1, nodesCopy.size()), child);
        }

    }

    private void removeDuplicates(List<KDTreeNode> nodes, int mid) {
        nodes.remove(mid);
    }

    private boolean isParentAndNextMidSame(List<KDTreeNode> nodes, KDTreeNode parent) {
        int lo = 0;
        int hi = nodes.size();
        int mid = (lo + hi) / 2;
        return (nodes.get(mid).node.getxMax() == parent.node.getxMax() && nodes.get(mid).node.getyMax() == parent.node.getyMax());
    }

    public Value getNearestNode(float x, float y) throws KDTreeEmptyException {
        if (!isSorted) {
            buildTree();
        }

        double shortestDistance = Double.MAX_VALUE;
        KDTreeNode nearestNode = getNearestNode(root, x, y, shortestDistance, null, root.onXAxis);
        return nearestNode.node;

    }

    private KDTreeNode getNearestNode(KDTreeNode currentNode, float x, float y, double shortestDistance, KDTreeNode nearestNode, Boolean xAxis) {
        if (currentNode == null) {
            return nearestNode;
        }

        double newDistance = getDistance(currentNode, x, y);
        if (newDistance < shortestDistance) {
            shortestDistance = newDistance;
            nearestNode = currentNode;
        }

        //checks if we should search the left or right side of the tree first, to save time/space.
        double compare = xAxis ? Math.abs(x - currentNode.node.getxMax()) : Math.abs(y - currentNode.node.getyMax());

        KDTreeNode node1 = compare < 0 ? currentNode.leftChild : currentNode.rightChild;
        KDTreeNode node2 = compare < 0 ? currentNode.rightChild : currentNode.leftChild;

        nearestNode = getNearestNode(node1, x, y, shortestDistance, nearestNode, !xAxis);

        // Checks if its worth checking on the other side of tree.
        if (possibleCloserNode(shortestDistance, currentNode, x, y)) {

            nearestNode = getNearestNode(node2, x, y, shortestDistance, nearestNode, !xAxis);
        }
        return nearestNode;

    }

    private boolean possibleCloserNode(Double shortestDistance, KDTreeNode currentNode, float x, float y) {
        double possibleNewDistance = Math.abs(currentNode.onXAxis ? x - currentNode.node.getxMax() : y - currentNode.node.getyMax());
        return shortestDistance > Math.abs(possibleNewDistance);
    }

    private double getDistance(KDTreeNode from, float x, float y) {
        Point2D p = new Point2D(x, y);
        double result = p.distance(from.node.getxMax(), from.node.getyMax());
        return result;
    }

    // TODO: 26-03-2021 remove both print methods when no longer needed.
    public void printTree() throws KDTreeEmptyException {
        if (root == null) {
            buildTree();
        }
        Integer level = 1;

        HashMap<Integer, ArrayList<KDTreeNode>> result = new HashMap<>();

        result = getPrintTree(root, level, result);

        while (result.get(level) != null) {
            System.out.println("");
            System.out.println("Level: " + level);
            for (KDTreeNode node : result.get(level)) {
                System.out.println("Node id: " + node.node.getId() + " : x:" + node.node.getxMax() + " y: " + node.node.getyMax());
                if (node.leftChild != null) {
                    System.out.println("Has left child, id: " + node.leftChild.node.getId());
                }
                if (node.rightChild != null) {
                    System.out.println("Has right child, id: " + node.rightChild.node.getId());
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
            int levelCopy = level.intValue();

            getPrintTree(node.leftChild, level, result);
            getPrintTree(node.rightChild, levelCopy, result);

        }

        return result;

    }

    private class KDTreeNode {

        private Value node;
        private KDTreeNode leftChild;
        private KDTreeNode rightChild;
        private Boolean onXAxis;


        public KDTreeNode(Value node) {
            this.node = node;
        }
    }
}





    
    

