package bfst21;

import bfst21.Exceptions.KDTreeEmptyException;
import bfst21.Exceptions.NoNavigationResultException;
import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;
import bfst21.data_structures.*;
import bfst21.view.CanvasBounds;

import java.util.ArrayList;

public class MapData {
    private float minX, minY, maxX, maxY;
    private boolean rTreeDebug;
    private ArrayList<ArrayList<Element>> mapSegment; //Only content within bounds
    private KDTree<Node> closetRoadTree;
    private RTree rTree;
    private AddressTriesTree addressTree;
    private ElementToElementsTreeMap<Node, Way> nodeToHighWay;
    private DijkstraSP dijkstra;
    private Way currentDijkstraRoute;

    public MapData() {
        mapSegment = new ArrayList<>();
    }

    public void addDataTrees(KDTree<Node> highWayRoadNodes, RTree rTree, ElementToElementsTreeMap<Node, Relation> nodeToRestriction, ElementToElementsTreeMap<Way, Relation> wayToRestriction, AddressTriesTree addressTree, ElementToElementsTreeMap<Node, Way> nodeToWayMap) {
        this.rTree = rTree;
        this.closetRoadTree = highWayRoadNodes;
        this.addressTree = addressTree;
        nodeToHighWay = nodeToWayMap;
        dijkstra = new DijkstraSP(nodeToHighWay, nodeToRestriction, wayToRestriction);

        buildTrees();
    }

    private void buildTrees() {
        closetRoadTree.buildTree();
    }


    public void searchInData(CanvasBounds bounds, int zoomLevel) {
        mapSegment = rTree.search(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY(), rTreeDebug, zoomLevel);
    }

    public void setRTreeDebug(boolean selected) {
        rTreeDebug = selected;
    }

    public String getNearestRoad(float x, float y) {
        String names = "";
        try {
            Node node = closetRoadTree.getNearestNode(x, y);
            names = getNodeHighWayNames(node);

        } catch (KDTreeEmptyException e) {
            names = e.getMessage();
        }
        return names;
    }

    /*public String getNearestRoad(float x, float y) { // TODO: 4/22/21 in progress
        Way way = rTree.getNearestRoad(x, y);
        if (way.getName() == null) {
            System.out.println("");
        }
        return way.getName();
    }*/

    public String getNodeHighWayNames(Node node) {
        String names = "";
        ArrayList<String> list = new ArrayList<>();
        ArrayList<Way> ways = nodeToHighWay.getElementsFromNode(node);
        if (ways != null) {
            for (Way way : ways) {
                if (way.getName() != null && !list.contains(way.getName())) list.add(way.getName());
            }
            names = String.join(", ", list);
        }
        return names;

    }

    public Node getNearestRoadNode(float x, float y) {
        Node nearestRoadNode = null;
        try {
            nearestRoadNode = closetRoadTree.getNearestNode(x, y);
        } catch (KDTreeEmptyException e) {
            e.printStackTrace();
        }
        return nearestRoadNode;
    }

    public void setDijkstraRoute(Node from, Node to, boolean car, boolean bike, boolean walk, boolean fastest) throws NoNavigationResultException {
        ArrayList<Node> path = dijkstra.getPath(from, to, car, bike, walk, fastest);
        currentDijkstraRoute = new Way(0);
        currentDijkstraRoute.setType("navigation");
        for (int i = 0; i < path.size() - 1; i++) {
            currentDijkstraRoute.addNode(path.get(i));
        }
    }

    public double getDistanceNav() throws NoNavigationResultException {
        return dijkstra.getTotalDistance();
    }

    public double getTimeNav() throws NoNavigationResultException {
        return dijkstra.getTotalTime();
    }

    public Way getCurrentDijkstraRoute() {
        return currentDijkstraRoute;
    }

    public void setCurrentDijkstraRouteToNull() {
        currentDijkstraRoute = null;
    }

    public ArrayList<ArrayList<Element>> getMapSegment() {
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
