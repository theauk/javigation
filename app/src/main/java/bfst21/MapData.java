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
    private RTreeHolder rTreeHolder;
    private transient ArrayList<ArrayList<Element>> mapSegment; //Only content within bounds
    private float minX, minY, maxX, maxY;
    private AddressTriesTree addressTree;
    private transient boolean rTreeDebug;

    private ElementToElementsTreeMap<Node, Relation> nodeToRestriction;
    private ElementToElementsTreeMap<Node, Way> nodeToHighWay;
    private ElementToElementsTreeMap<Way, Relation> wayToRestriction;
    private transient List<Element> currentRoute;
    private transient Node searchResult;

    private transient List<Node> userAddedPoints;
    private Relation coastlines;
    private HashMap<Element, String> elementToText;

    public MapData() {
        mapSegment = new ArrayList<>();
        currentRoute = new ArrayList<>();
    }

    /**
     * Adds the different trees which have data from the OSM file.
     * @param highWayRoadNodes A KD-tree which holds Nodes that are part of highways (ways which can be navigated on).
     * @param rTree An R-tree which holds Elements that should be drawn.
     * @param nodeToRestriction A tree map that maps nodes to restrictions.
     * @param wayToRestriction A tree map that maps ways to restrictions.
     * @param addressTree A trie which can be used to search for addresses.
     * @param nodeToWayMap A tree map which maps nodes to ways which have the respective nodes on them.
     */
    public void addDataTrees(KDTree<Node> highWayRoadNodes, RTreeHolder rTree, ElementToElementsTreeMap<Node, Relation> nodeToRestriction, ElementToElementsTreeMap<Way, Relation> wayToRestriction, AddressTriesTree addressTree, ElementToElementsTreeMap<Node, Way> nodeToWayMap) {
        this.rTreeHolder = rTree;
        this.kdTree = highWayRoadNodes;
        this.addressTree = addressTree;
        this.nodeToHighWay = nodeToWayMap;
        this.nodeToRestriction = nodeToRestriction;
        this.wayToRestriction = wayToRestriction;
        currentRoute = new ArrayList<>();
        userAddedPoints = new ArrayList<>();
        buildKDTrees();
    }

    /**
     * Gets the relation representing the coastlines.
     * @return A relation that represents the coastlines.
     */
    public Relation getCoastlines() {
        return coastlines;
    }

    /**
     * Sets the relation which holds the coastlines.
     * @param relation The relation representing the coastlines.
     */
    public void setCoastlines(Relation relation) {
        coastlines = relation;
    }

    /**
     * Builds the KD-tree after all necessary elements have been inserted.
     */
    private void buildKDTrees() {
        kdTree.buildTree();
    }

    /**
     * Updates the field that holds the elements which are within the canvas bounds and which should be drawn at the current zoom level.
     * @param bounds The canvas bounds.
     * @param zoomLevel The current zoom level.
     */
    public void searchInRTree(CanvasBounds bounds, int zoomLevel) {
        mapSegment = rTreeHolder.search(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY(), rTreeDebug, zoomLevel);
    }

    /**
     * Gets a nested list of elements that should be drawn at the current zoom level and bounds. The sublist corresponds to the drawing layers.
     * @return A list with lists of Elements.
     */
    public ArrayList<ArrayList<Element>> getMapSegment() {
        return mapSegment;
    }

    /**
     * Sets if the R-tree visualizer/debug-mode should be on or off.
     * @param selected True if the debug mode should be on. Otherwise, false.
     */
    public void setRTreeDebug(boolean selected) {
        rTreeDebug = selected;
    }

    /**
     * Gets the nearest road to a query point.
     * @param x The query point's x-coordinate.
     * @param y The query point's y-coordinate.
     * @param useKDTree True if the KD-tree should be used to search. False if the R-tree should be used.
     * @return The name of the nearest road.
     */
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

    /**
     * Gets the nearest road to a query point via the R-tree.
     * @param x The query point's x-coordinate.
     * @param y The query point's y-coordinate.
     * @return The nearest Way.
     */
    public Way getNearestRoadRTree(float x, float y) {
        return getNearestRoadRTreePQEntry(x, y, null).getWay();
    }

    /**
     * Gets the nearest road R-tree priority queue entry given a query point. The entry also holds the way segment which are closest to the point.
     * @param x The query point's x-coordinate.
     * @param y The query point's y-coordinate.
     * @return The priority queue entry with the nearest Way.
     */
    public RTree.NearestRoadPriorityQueueEntry getNearestRoadRTreePQEntry(float x, float y, String addressWayName) {
        return rTreeHolder.getNearestRoad(x, y, addressWayName);
    }

    /**
     * Gets the nearest road to a query point via the KD-tree.
     * @param x The query point's x-coordinate.
     * @param y The query point's y-coordinate.
     * @return The name of the nearest road.
     */
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

    /**
     * Gets the names of ways which have a certain node on them.
     * @param node The node to find the ways for.
     * @return A comma-separated string of way names which have the given node on them.
     */
    public String getNodeHighWayNames(Node node) {
        String names = "";
        ArrayList<String> list = new ArrayList<>();
        ArrayList<Way> ways = nodeToHighWay.getElementsFromKeyElement(node);
        if (ways != null) {
            for (Way way : ways) {
                if (way.getName() != null && !list.contains(way.getName())) list.add(way.getName());
            }
            names = String.join(", ", list);
        }
        return names;
    }

    /**
     * Adds a node to the list of user added points.
     * @param toAdd The Node to add.
     */
    public void addToUserPointList(Node toAdd) {
        toAdd.setType("user_added");
        userAddedPoints.add(toAdd);
    }

    /**
     * Gets the list of user added points.
     * @return A list of nodes.
     */
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
        resetAllUserInput();
    }

    /**
     * Gets the last drawn route.
     * @return A list of elements that makes up the last drawn route.
     */
    public List<Element> getCurrentRoute() {
        return currentRoute;
    }

    /**
     * Sets the current route.
     * @param currentRoute A list of elements that makes up the currently drawn route.
     */
    public void setCurrentRoute(List<Element> currentRoute) {
        this.currentRoute = currentRoute;
    }

    /**
     * Deletes the current route
     */
    public void resetCurrentRoute(){
        currentRoute = new ArrayList<>();
    }

    /**
     * Resets search results shown on map.
     */
    public void resetCurrentSearchResult(){
        searchResult = null;
    }

    /**
     * Resets all user input to be shown on map.
     */
    public void resetAllUserInput(){
        resetCurrentRoute();
        resetCurrentSearchResult();
        userAddedPoints = new ArrayList<>();
    }

    /**
     * Search result from user input to show on map
     * @param node node to add
     */
    public void addUserSearchResult(Node node){
        searchResult = node;
    }

    /**
     * Search results from user input
     * @return List of nodes to be drawn
     */
    public Node getUserSearchResult(){
        return searchResult;
    }

    /**
     * Sets a map which holds elements mapping to city names.
     * @param elementToCityName A HashMao with Elements mapping to city names.
     */
    public void setElementToText(HashMap<Element, String> elementToCityName) {
        this.elementToText = elementToCityName;
    }

    /**
     * Gets the text corresponding to an Element.
     * @param element The Element to look up.
     * @return The text mapping to the Element.
     */
    public String getTextFromElement(Element element) {
        return elementToText.get(element);
    }

    /**
     * Gets addresses from the address trie which match a given prefix.
     * @param prefix The prefix to search for.
     * @return A list of AddressTrieNode that have addresses which match the prefix.
     */
    public List<AddressTrieNode> getAutoCompleteAddresses(String prefix){
        return addressTree.searchWithPrefix(prefix);
    }

    public AddressTriesTree getAddressTree() {
        return addressTree;
    }

    /**
     * Gets a map which maps nodes to restrictions that have the specified node as a part of the restriction.
     * @return A tree map with nodes mapping to restrictions in the form of relations.
     */
    public ElementToElementsTreeMap<Node, Relation> getNodeToRestriction() {
        return nodeToRestriction;
    }

    /**
     * Gets a map which maps ways to restrictions that have the specified way as a part of the restriction.
     * @return A tree map with ways mapping to restrictions in the form of relations.
     */
    public ElementToElementsTreeMap<Way, Relation> getWayToRestriction() {
        return wayToRestriction;
    }

    /**
     * Gets a map which maps nodes to highways (ways that can be navigated on).
     * @return A tree mao with nodes mapping to ways.
     */
    public ElementToElementsTreeMap<Node, Way> getNodeToHighWay() {
        return nodeToHighWay;
    }

    public float getMinX() { // TODO: 5/1/21 Hvad er de her præcist? bounds af kortet?
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
