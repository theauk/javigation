package bfst21;

import bfst21.Exceptions.KDTreeEmptyException;
import bfst21.Exceptions.NoNavigationResultException;
import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;
import bfst21.data_structures.*;
import bfst21.view.CanvasBounds;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class MapData implements Serializable {
    @Serial
    private static final long serialVersionUID = 8514196836151887206L;

    private KDTree<Node> closetRoadTree;
    private RTree rTree;
    private ArrayList<ArrayList<Element>> mapSegment; //Only content within bounds
    private float minX, minY, maxX, maxY;
    private AddressTriesTree addressTree;
    private boolean rTreeDebug;
    private ElementToElementsTreeMap<Node, Way> nodeToHighWay;
    private DijkstraSP dijkstra;
    private ArrayList<Element> currentDijkstraRoute;
    private ArrayList<Node> userAddedPoints;
    private Relation coastlines;
    private HashMap<Element, String> elementToText;

    public MapData() {
        mapSegment = new ArrayList<>();
    }

    public void addDataTrees(KDTree<Node> highWayRoadNodes, RTree rTree, ElementToElementsTreeMap<Node, Relation> nodeToRestriction, ElementToElementsTreeMap<Way, Relation> wayToRestriction, AddressTriesTree addressTree, ElementToElementsTreeMap<Node, Way> nodeToWayMap) {
        this.rTree = rTree;
        this.closetRoadTree = highWayRoadNodes;
        this.addressTree = addressTree;
        nodeToHighWay = nodeToWayMap;
        dijkstra = new DijkstraSP(nodeToHighWay, nodeToRestriction, wayToRestriction);
        currentDijkstraRoute = new ArrayList<>();
        userAddedPoints = new ArrayList<>();

        buildTrees();
    }

    public Relation getCoastlines() {
        return coastlines;
    }

    public void setCoastlines(Relation relation) {
        coastlines = relation;
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
        currentDijkstraRoute = new ArrayList<>();
        if (path.size() > 0) {
            Way route = new Way();
            Node start = path.get(0);
            Node end = path.get(path.size() - 1);
            setRouteElementType(route, start, end);

            route.setType("navigation");
            for (int i = 0; i < path.size() - 1; i++) {
                route.addNode(path.get(i));
            }
            currentDijkstraRoute.add(route);
            currentDijkstraRoute.add(start);
            currentDijkstraRoute.add(end);
        }
    }

    public double getDistanceNav() throws NoNavigationResultException {
        return dijkstra.getTotalDistance();
    }

    public double getTimeNav() throws NoNavigationResultException {
        return dijkstra.getTotalTime();
    }

    public void addToUserPointList(Node toAdd) {
        toAdd.setType("user_added");
        userAddedPoints.add(toAdd);
    }

    public ArrayList<Node> getUserAddedPoints() {
        return userAddedPoints;
    }

    private void setRouteElementType(Way way, Node start, Node end) {
        way.setType("navigation");
        start.setType("start_route_note");
        end.setType("end_route_note");
    }

    public ArrayList<Element> getCurrentDjikstraRoute() {
        return currentDijkstraRoute;
    }

    public void removeCurrentDijkstraRoute() {
        currentDijkstraRoute = new ArrayList<>();
    }

    public ArrayList<ArrayList<Element>> getMapSegment() {
        return mapSegment;
    }

    public Node getAddressNode(String address) {
        return addressTree.getAddressNode(address);
    }

    public String getTextFromElement(Element element) {
        String result = elementToText.get(element);
        return result;
    }

    public void setElementToText(HashMap<Element, String> elementToCityname) {
        this.elementToText = elementToCityname;
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
