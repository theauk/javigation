package bfst21.data_structures;



import java.util.Comparator;
import java.util.List;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Specifik_Elements.KDTreeNode;
import javafx.geometry.Point2D;

public class Node2DTree{
    private KDTreeNode root;

    private final Comparator<KDTreeNode> comparatorX = new Comparator<KDTreeNode>() {
        @Override
        public int compare(KDTreeNode p1, KDTreeNode p2) {
            return Double.compare(p1.getNode().getX(), p2.getNode().getX());
        }
    };

    private final Comparator<KDTreeNode> comparatorY = new Comparator<KDTreeNode>() {
        @Override
        public int compare(KDTreeNode p1, KDTreeNode p2) {
            return Double.compare(p1.getNode().getY(), p2.getNode().getY());
        }
    };

    

    public Node2DTree(List<KDTreeNode> nodes){
        if(nodes != null){
            buildTree(nodes);
        }
        
    }


    private void buildTree(List<KDTreeNode> nodes){
        nodes.sort(comparatorX);
        int lo = 0;
        int hi = nodes.size();
        int mid = (lo + hi) / 2;

        root = nodes.get(mid);
        root.setIsOnXAxis(true);        
        buildTree(true, nodes.subList(lo, mid), root);
        buildTree(false, nodes.subList(mid+1, hi), root);

    }

    //TODO split point, lige nu bygger den træet helt ud, men burde der være et slut punkt, med en liste?
    private void buildTree(boolean isLeft,List<KDTreeNode> nodes, KDTreeNode parent){
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        nodes.sort(parent.IsOnXAxis() ? comparatorX : comparatorY);
        int lo = 0;
        int hi = nodes.size();
        int mid = (lo + hi) / 2;

        KDTreeNode child = nodes.get(mid);
        child.setIsOnXAxis(!parent.IsOnXAxis());
        
        if(isLeft){
            parent.setLeftChild(child);
        } else{
            parent.setRightChild(child);
        }

        buildTree(true, nodes.subList(0, mid), child);
        if (mid + 1 < nodes.size())
            buildTree(false, nodes.subList(mid+1, hi), child);
    }



     public KDTreeNode getNearestNode(float x, float y){
        double shortestDistance = Double.MAX_VALUE;

        KDTreeNode nearestNode = getNearestNode(root, x, y, shortestDistance, null, root.IsOnXAxis());

        return nearestNode;
        
    }
    
    private KDTreeNode getNearestNode(KDTreeNode nextNode, float x, float y, double shortestDistance, KDTreeNode nearestNode, Boolean xAxis){
        if (nextNode == null){
            return nearestNode;
        }
        
        
        double newDistance = getDistance(nextNode, x, y);
        if(newDistance<shortestDistance){
            shortestDistance = newDistance;
            nearestNode = nextNode;
        }


        //checks if we should search the left or right side of the tree first, to save time/space.
        double compare = xAxis ? Math. abs(x - nextNode.getNode().getX()) : Math. abs(y - nextNode.getNode().getY());

        KDTreeNode node1 = compare < 0 ? nextNode.getLeftChild() : nextNode.getRightChild();
        KDTreeNode node2 = compare < 0 ? nextNode.getRightChild() : nextNode.getLeftChild();

        nearestNode = getNearestNode(node1, x, y, shortestDistance, nearestNode, !xAxis);


        if(shortestDistance > Math. abs((xAxis ? x - nextNode.getNode().getX() : y - nextNode.getNode().getY()))){
            nearestNode = getNearestNode(node2, x, y, shortestDistance, nearestNode, !xAxis);
        }
        
        return nearestNode;
        
    }

    private double getDistance(KDTreeNode from, float x, float y){
        Point2D p = new Point2D(x, y);
       double result = p.distance(from.getNode().getX(), from.getNode().getY());
        return result;
    }
    
}
