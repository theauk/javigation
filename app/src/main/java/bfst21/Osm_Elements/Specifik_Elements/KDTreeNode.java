package bfst21.Osm_Elements.Specifik_Elements;

import java.util.List;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;

public class KDTreeNode extends Node{
    private KDTreeNode leftChild;
    private KDTreeNode rightChild;
    private Boolean OnXAxis;
    private List<Way> partOfWays;

    public KDTreeNode(Node node) {
        super(node.getID(), node.getY(), node.getX());
        
    }

    public KDTreeNode getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(KDTreeNode leftChild) {
        this.leftChild = leftChild;
    }

    public KDTreeNode getRightChild() {
        return rightChild;
    }

    public void setRightChild(KDTreeNode rightChild) {
        this.rightChild = rightChild;
    }

    public Boolean IsOnXAxis() {
        return OnXAxis;
    }

    public void setIsOnXAxis(Boolean xAxis) {
        this.OnXAxis = xAxis;
    }

    public List<Way> getPartOfWays() {
        return partOfWays;
    }

    public void setPartOfWays(List<Way> partOfWays) {
        this.partOfWays = partOfWays;
    }

    public void addWay(Way way){
        partOfWays.add(way);
    }

    
}
