package bfst21;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bfst21.Osm_Elements.Node;
import javafx.geometry.Point2D;

public class Node2DTree{
    private Node root;
    private final Comparator<Node> comparatorX = new Comparator<Node>() {

        @Override
        public int compare(Node p1, Node p2) {
            return Double.compare(p1.getX(), p2.getY());
        }
    };

    private final Comparator<Node> comparatorY = new Comparator<Node>() {

        @Override
        public int compare(Node p1, Node p2) {
            return Double.compare(p1.getY(), p2.getY());
        }
    };

   

    public Node2DTree(List<Node> nodes){
        buildTree(nodes);
    }


    private void buildTree(List<Node> nodes){
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
    private void buildTree(boolean isLeft,List<Node> nodes, Node parent){
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        nodes.sort(parent.IsOnXAxis() ? comparatorX : comparatorY);
        int lo = 0;
        int hi = nodes.size();
        int mid = (lo + hi) / 2;

        Node child = nodes.get(mid);
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

    //TODO smide print statements ud
    public void printTree(){
        System.out.println(root.getID() + " " + root.getX() + " " + root.getY());
        printTree(root, true);
    }

    public void printTree(Node node, boolean x){
        System.out.println("Parent");
        
        
        if(node.getLeftChild() != null){
            System.out.println("Chil left    " + " " + node.getX() + " " + node.getY());
            printTree(node.getLeftChild(), !x);
        }
        if(node.getRightChild() != null){
            System.out.println("Chil Right"+ " " + node.getX() + " " + node.getY());
            printTree(node.getRightChild(), !x);
        }
   

    }

    

   


    private int compareNodes(Node currentNode, Node node){
        if(currentNode.IsOnXAxis()){
            return compareTo(currentNode.getX(), node.getX());
        }
        else {
            return compareTo(currentNode.getY(), node.getY());
        }
        
    }
    private int compareTo(double currentNode, double newNode){
        // less than (return -1)    equal to (return 0)       greater than (return 1)
        if(currentNode < newNode){
            return -1;
        }
        if(currentNode > newNode){
            return 1;
        }
        else {
            //TODO edgecase??
            return 0;
        }
    }

     //TODO does the x and y coordinates from the screen match lon and lat?
     public Node getNearestNode(long lon, long lat){
        Node nearestNode = root;

        double shortestDistance = getDistance(nearestNode, lon, lat);
        
        int compare = compareTo(nearestNode.getX(), lon);
        //TODO problem, selvom at koordinatet i den ene side af træet kan den tætteste node være i den den anden side.
        if(compare == -1){
            return getNearestNode(nearestNode.getRightChild(), lon, lat, shortestDistance, nearestNode);
        } else {
            return getNearestNode(nearestNode.getLeftChild(), lon, lat, shortestDistance, nearestNode);
        }
        
      

        
    }
    //TODO get this done
    private Node getNearestNode(Node nextNode, long lon, long lat, double shortestDistance, Node nearestNode){
        double newDistance = getDistance(nextNode, lon, lat);
        if(newDistance<shortestDistance)
            shortestDistance = newDistance;

        

        return nearestNode;
    }

    private double getDistance(Node from, long lon, long lat){
        Point2D p = new Point2D(lon, lat);
       double result = p.distance(from.getX(), from.getY());
        return result;
    }
    
}
