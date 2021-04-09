package bfst21;

import bfst21.Exceptions.KDTreeEmptyException;
import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;

import bfst21.Osm_Elements.Way;
import bfst21.data_structures.AddressTriesTree;
import bfst21.data_structures.KDTree;
import bfst21.data_structures.RTree;
import bfst21.data_structures.RoadGraph;
import bfst21.view.CanvasBounds;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MapData {
    private KDTree<Node> closetRoadTree;
    private RTree rTree;
    private List<Element> mapSegment; //Only content within bounds
    private float minX, minY, maxX, maxY;
    private AddressTriesTree addressTree;
    private RoadGraph roadGraph;
    private boolean rTreeDebug;

    public MapData() {
        mapSegment = new ArrayList<>();

    }

    public void addDataTrees(KDTree<Node> highWayRoadNodes, RTree rTree, RoadGraph roadGraph,AddressTriesTree addressTree ){
        this.rTree = rTree;
        this.closetRoadTree = highWayRoadNodes;
        this.addressTree = addressTree;
        this.roadGraph = roadGraph;
        buildTrees();
    }

    private void buildTrees() {
        closetRoadTree.buildTree();
    }


    public void searchInData(CanvasBounds bounds) {
        mapSegment = rTree.search(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY(), rTreeDebug);
    }

    public void setRTreeDebug(boolean selected) {
        rTreeDebug = selected;
    }

    public String getNearestRoad(float x, float y) {
        String names = "";
        HashSet<String> list = new HashSet<>();
        try {
            Node node =  closetRoadTree.getNearestNode(x,y);
            if(node.getReferencedHighWays() != null){
                for(Way way : node.getReferencedHighWays()){
                    if(way.getName()!=null)list.add(way.getName());
                }
                names = String.join(", ", list);
            }

        } catch (KDTreeEmptyException e) {
            names = e.getMessage();
        }
        return names;
    }

    public List<Element> getMapSegment() {
        return mapSegment;
    }



    public Node getAddressNode(String address) {
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
