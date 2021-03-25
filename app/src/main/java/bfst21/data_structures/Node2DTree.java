package bfst21.data_structures;



import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import bfst21.Osm_Elements.Element;


import javafx.geometry.Point2D;

public class Node2DTree<Value extends Element>{
    private KDTreeNode root;
    private List<KDTreeNode> list;
    private boolean isSorted;
    private int i;

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

    public void addALl(List<Value> nodes){
        if(list == null){
            list = new ArrayList<>();
        }
        for (Value node: nodes ){
            add(node);
        }
        
    }

    private void add(Value node){
        list.add(new KDTreeNode(node));
        isSorted = false;
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


    private void buildTree(){
        if (list == null || list.isEmpty()) {
            return;
        }
        list.sort(comparatorX);
        int lo = 0;
        int hi = list.size();
        int mid = (lo + hi) / 2;

        root = list.get(mid);
        root.onXAxis = true;        
        buildTree(true, list.subList(lo, mid), root);
        buildTree(false, list.subList(mid+1, hi), root);

    }

    
    private void buildTree(boolean isLeft,List<KDTreeNode> nodes, KDTreeNode parent){
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        //nodes = checkForDuplikates(nodes, parent);
        if(nodes.isEmpty()){
            return;
        }
        nodes.sort(parent.onXAxis ? comparatorX  : comparatorY );
        

        int lo = 0;
        int hi = nodes.size();
        int mid = (lo + hi) / 2;
        KDTreeNode child = nodes.get(mid);
        
        child.onXAxis = !parent.onXAxis;
        
        if(isLeft){
            parent.leftChild = child;
        } else{
            parent.rightChild=child;
        }

        buildTree(true, nodes.subList(0, mid), child);
        if (mid + 1 < nodes.size())
            buildTree(false, nodes.subList(mid+1, hi), child);
    }

    private List<KDTreeNode> checkForDuplikates(List<KDTreeNode> nodes, KDTreeNode parent){
        
        int lo = 0;
        int hi = nodes.size();
        int mid = (lo + hi) / 2;

        if(nodes.get(mid).node.getxMax() == parent.node.getxMax() && nodes.get(mid).node.getyMax() == parent.node.getyMax() ){
           
            nodes.remove(mid);
            if(!nodes.isEmpty()){
                nodes = checkForDuplikates(nodes, parent);
            }
            
        }
        
        return nodes;
    }



     public Value getNearestNode(float x, float y){
        if(!isSorted){
            buildTree();
            isSorted = true;
            System.out.println(i);
        }
        double shortestDistance = Double.MAX_VALUE;

        KDTreeNode nearestNode = getNearestNode(root, x, y, shortestDistance, null, root.onXAxis);

        return nearestNode.node;
        
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
        double compare = xAxis ? Math. abs(x - nextNode.node.getxMax()) : Math. abs(y - nextNode.node.getyMax());

        KDTreeNode node1 = compare < 0 ? nextNode.leftChild : nextNode.rightChild;
        KDTreeNode node2 = compare < 0 ? nextNode.rightChild : nextNode.leftChild;

        nearestNode = getNearestNode(node1, x, y, shortestDistance, nearestNode, !xAxis);


        if(shortestDistance > Math. abs((xAxis ? x - nextNode.node.getxMax() : y - nextNode.node.getyMax()))){
            nearestNode = getNearestNode(node2, x, y, shortestDistance, nearestNode, !xAxis);
        }
        
        return nearestNode;
        
    }

    private double getDistance(KDTreeNode from, float x, float y){
        Point2D p = new Point2D(x, y);
       double result = p.distance(from.node.getxMax(), from.node.getyMax());
        return result;
    }

    
    
}
