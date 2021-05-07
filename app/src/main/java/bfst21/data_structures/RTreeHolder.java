package bfst21.data_structures;

import bfst21.Osm_Elements.Element;
import bfst21.view.MapCanvas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Sorts elements into different R-trees depending on their type.
 */
public class RTreeHolder {
    private Map<String, RTree> rTreeMap;
    private HashMap<String, Integer> print;
    private ArrayList<ArrayList<RTree>> rTreeList;
    private int returnListSize;
    private Map<String, Byte> zoomMap;
    private RTree closetRoadRTree;
    private int[] lengths; // TODO: 5/7/21 delete?

    public RTreeHolder(int minimumChildren, int maximumChildren, int numberOfCoordinates, int returnListSize) {
        rTreeList = new ArrayList<>();
        print = new HashMap<>();
        lengths = new int[MapCanvas.MAX_ZOOM_LEVEL];
        rTreeMap = new HashMap<>();
        this.returnListSize = returnListSize;
        zoomMap = MapCanvas.zoomMap;

        while (rTreeList.size() <= MapCanvas.MAX_ZOOM_LEVEL) {
            rTreeList.add(new ArrayList<>());
        }

        for (String type : zoomMap.keySet()) {
            RTree rTree = new RTree(minimumChildren, maximumChildren, numberOfCoordinates);
            rTreeMap.put(type, rTree);
            print.put(type, 0);
            rTreeList.get(zoomMap.get(type)).add(rTree);
        }
    }

    public void setClosetRoadRTree(RTree rTree) {
        closetRoadRTree = rTree;
    }

    public void insert(Element element) {
        String type = element.getType();
        if (zoomMap.get(type) != null) {
            rTreeMap.get(type).insert(element);
            int i = print.get(type);
            i++;
            print.put(type, i);
        }
    }

    /**
     * Search for elements in the R-tree based on the current zoom level.
     * @param xMin The x-min coordinate of the bounding box.
     * @param xMax The x-man coordinate of the bounding box.
     * @param yMin The y-min coordinate of the bounding box.
     * @param yMax The y-max coordinate of the bounding box.
     * @param debug True, if debug elements should be shown. Otherwise, false.
     * @param currentZoomLevel The current zoom level.
     * @return A nested list with elements within the bounding box sorted based on zoom levels.
     */
    public ArrayList<ArrayList<Element>> search(float xMin, float xMax, float yMin, float yMax, boolean debug, int currentZoomLevel) {
        ArrayList<ArrayList<Element>> results = getCleanArrayList();
        for (int i = 0; i <= currentZoomLevel; i++) {
            for (RTree rTree : rTreeList.get(i)) {
                rTree.search(xMin, xMax, yMin, yMax, debug, results);
            }
        }

        return results;
    }

    /**
     * Gets the closest NearestRoadPriorityQueueEntry to a query point.
     * @param x Query x-coordinate.
     * @param y Query y-coordinate.
     * @param addressWayName The name of the address street name if searching for the closest road to an address.
     *                       Otherwise, null if searching for closet road to the cursor.
     * @return The closest NearestRoadPriorityQueueEntry.
     */
    public RTree.NearestRoadPriorityQueueEntry getNearestRoad(float x, float y, String addressWayName) {
        return closetRoadRTree.getNearestRoad(x, y, addressWayName);
    }

    /**
     * Make a list with as many lists as layers for the map.
     *
     * @return An ArrayList with empty nested Arraylists.
     */
    private ArrayList<ArrayList<Element>> getCleanArrayList() {
        ArrayList<ArrayList<Element>> results = new ArrayList<>();
        while (results.size() <= returnListSize) {
            results.add(new ArrayList<>());
        }
        return results;
    }

    public void print() { // TODO: 5/7/21 delete?
        for (Map.Entry<String, Integer> entry : print.entrySet()) {
            System.out.println("Rtree " + entry.getKey() + ", " + entry.getValue());
        }
    }

}
