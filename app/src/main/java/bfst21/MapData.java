package bfst21;

import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;
import bfst21.data_structures.Node2DTree;
import bfst21.data_structures.RTree;
import bfst21.view.CanvasBounds;

import java.util.ArrayList;
import java.util.List;

public class MapData {
    private Node2DTree<Node> roadNodes;

    private RTree rTree;
    private List<Element> mapSegment; //Only content within bounds
    private float minX, minY, maxX, maxY;

    public MapData() {

        mapSegment = new ArrayList<>();
        rTree = new RTree(1, 30, 4);
        roadNodes = new Node2DTree<>();
    }

    public void addData(List<Element> toAdd) {

        rTree.insertAll(toAdd);
    }

    public void searchInData(CanvasBounds bounds) {
        mapSegment = rTree.search(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
    }

    public void addRoadsNodes(List<Node> nodes) {
        roadNodes.addALl(nodes);
    }

    public String getNearestRoad(float x, float y) {
        String names = "";
        Node node = roadNodes.getNearestNode(x, y);
        // TODO: 26-03-2021 when all nodes in kd tree are sure to have name this check is unessecary
        try {
            for (String s : node.getName()) {
                names += s + " ";
            }
        } catch (Exception e){}

        return names;
    }

    public List<Element> getMapSegment() {
        return mapSegment;
    }



    public float getMinX() {
        return minX;
    }

    public void setMinX(float minX) {
        this.minX = minX;
    }

    public float getMinY() {
        return minY;
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    public float getMaxX() {
        return maxX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    public float getMaxY() {
        return maxY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }
}
