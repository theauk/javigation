package bfst21;

import bfst21.Exceptions.KDTreeEmptyException;
import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Specifik_Elements.AddressNode;
import bfst21.Osm_Elements.Specifik_Elements.TravelWay;
import bfst21.data_structures.AddressTriesTree;
import bfst21.data_structures.KDTree;
import bfst21.data_structures.RTree;
import bfst21.data_structures.RoadGraph;
import bfst21.view.CanvasBounds;

import java.util.ArrayList;
import java.util.List;

public class MapData {
    private KDTree<Node> roadNodesTree;
    private RTree rTree;
    private List<Element> mapSegment; //Only content within bounds
    private float minX, minY, maxX, maxY;
    private AddressTriesTree addressTree;
    private RoadGraph roadGraph;

    public MapData() {

        mapSegment = new ArrayList<>();
        rTree = new RTree(1, 30, 4);
        roadNodesTree = new KDTree<>();
        addressTree = new AddressTriesTree();
        roadGraph = new RoadGraph();
    }

    public void addRoad(TravelWay way){
        roadGraph.add(way);

            if(way.getName() != null){
                roadNodesTree.addALl(way.getNodes());
        }
        addData(way);
    }

    public void addData(List<Element> toAdd) {
        rTree.insertAll(toAdd);
    }
    public void addData(Element toAdd){
        rTree.insert(toAdd);
    }

    public void searchInData(CanvasBounds bounds) {
        mapSegment = rTree.search(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
    }


    public String getNearestRoad(float x, float y) {
        String names = "";
        try {
            Node node = roadNodesTree.getNearestNode(x, y);
            for (String s : node.getName()) {
                names += s + " ";
            }
            names += "   x: " + node.getxMax() + "    y: " + node.getyMax();
        } catch (KDTreeEmptyException e){
            names = e.getMessage();
        }
        return names;
    }

    public List<Element> getMapSegment() {
        return mapSegment;
    }

    public void addAddress(AddressNode node){
        addressTree.put(node);

    }

    public AddressNode getAddressNode(String address){
        return addressTree.getAddressNode(address);
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
