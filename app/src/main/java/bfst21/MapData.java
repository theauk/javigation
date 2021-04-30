package bfst21;

import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;
import bfst21.data_structures.*;
import bfst21.exceptions.KDTreeEmptyException;
import bfst21.view.CanvasBounds;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapData implements Serializable {
    @Serial
    private static final long serialVersionUID = 8514196836151887206L;

    private KDTree<Node> kdTree;
    private RTree rTree;
    private transient ArrayList<ArrayList<Element>> mapSegment; //Only content within bounds
    private float minX, minY, maxX, maxY;
    private AddressTriesTree addressTree;
    private transient boolean rTreeDebug;

    private ElementToElementsTreeMap<Node, Relation> nodeToRestriction;
    private ElementToElementsTreeMap<Node, Way> nodeToHighWay;
    private ElementToElementsTreeMap<Way, Relation> wayToRestriction;
    private transient List<Element> currentRoute;

    private transient List<Node> userAddedPoints;
    private Relation coastlines;
    private HashMap<Element, String> elementToText;

    public MapData() {
        mapSegment = new ArrayList<>();
        currentRoute = new ArrayList<>();
    }

    public void addDataTrees(KDTree<Node> highWayRoadNodes, RTree rTree, ElementToElementsTreeMap<Node, Relation> nodeToRestriction, ElementToElementsTreeMap<Way, Relation> wayToRestriction, AddressTriesTree addressTree, ElementToElementsTreeMap<Node, Way> nodeToWayMap) {
        this.rTree = rTree;
        this.kdTree = highWayRoadNodes;
        this.addressTree = addressTree;
        this.nodeToHighWay = nodeToWayMap;
        this.nodeToRestriction = nodeToRestriction;
        this.wayToRestriction = wayToRestriction;
        currentRoute = new ArrayList<>();

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
        kdTree.buildTree();
    }

    public void searchInData(CanvasBounds bounds, int zoomLevel) {
        mapSegment = rTree.search(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY(), rTreeDebug, zoomLevel);
    }

    public void setRTreeDebug(boolean selected) {
        rTreeDebug = selected;
    }

    public String getNearestRoad(float x, float y, boolean useKDTree) {
        if (useKDTree) {
            return getNearestRoadKDTree(x, y);
        }
        else {
            Way w = getNearestRoadRTree(x, y);
            if (w.getName() != null) return w.getName();
            else return "";
        }
    }

    public Way getNearestRoadRTree(float x, float y) {
        return rTree.getNearestRoad(x, y);
    }

    public String getNearestRoadKDTree(float x, float y) {
        String names = "";
        try {
            Node node = kdTree.getNearestNode(x, y);
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
            nearestRoadNode = kdTree.getNearestNode(x, y);
        } catch (KDTreeEmptyException e) {
            e.printStackTrace();
        }
        return nearestRoadNode;
    }

    public void addToUserPointList(Node toAdd) {
        toAdd.setType("user_added");
        userAddedPoints.add(toAdd);
    }

    public List<Node> getUserAddedPoints() {
        return userAddedPoints;
    }

    /**
     * Defines a custom read object method for deserialization of MapData
     * to ensure no NullPointerExceptions are thrown because of transient fields.
     *
     * @param in the ObjectInputStream used for reading the object.
     * @throws IOException if the file could not be found or the stream is interrupted.
     * @throws ClassNotFoundException if used class could not be found.
     */
    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        currentRoute = new ArrayList<>();
        userAddedPoints = new ArrayList<>();
    }

    public List<Element> getCurrentRoute() {
        return currentRoute;
    }

    public void setCurrentRoute(List<Element> currentRoute) {
        this.currentRoute = currentRoute;
    }

    public ArrayList<ArrayList<Element>> getMapSegment() {
        return mapSegment;
    }


    public String getTextFromElement(Element element) {
        return elementToText.get(element);
    }

    public List<AddressTrieNode> getAutoCompleteAdresses(String prefix){
        return addressTree.searchWithPrefix(prefix);
    }

    public void setElementToText(HashMap<Element, String> elementToCityname) {
        this.elementToText = elementToCityname;
    }


    public ElementToElementsTreeMap<Node, Relation> getNodeToRestriction() {
        return nodeToRestriction;
    }

    public ElementToElementsTreeMap<Node, Way> getNodeToHighWay() {
        return nodeToHighWay;
    }

    public ElementToElementsTreeMap<Way, Relation> getWayToRestriction() {
        return wayToRestriction;
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
