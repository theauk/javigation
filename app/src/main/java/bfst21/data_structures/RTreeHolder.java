package bfst21.data_structures;

import bfst21.Osm_Elements.Element;
import bfst21.view.MapCanvas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RTreeHolder {
    private Map<String, RTree> rTreeMap;
    private HashMap<String, Integer> print;
    private ArrayList<ArrayList<RTree>> rTreeList;
    private int returnListSize;
    private Map<String, Byte> zoomMap;
    private RTree closetRoadRTree;
    private int[] lengths;

    public RTreeHolder(int minimumChildren, int maximumChildren, int numberOfCoordinates, int returnListSize){
        rTreeList = new ArrayList<>();
        print = new HashMap<>();
        lengths = new int[MapCanvas.MAX_ZOOM_LEVEL];
        rTreeMap = new HashMap<>();
        this.returnListSize = returnListSize;
        zoomMap = MapCanvas.zoomMap;

        while(rTreeList.size()<MapCanvas.MAX_ZOOM_LEVEL){
            rTreeList.add(new ArrayList<>());
        }

        for(String type : zoomMap.keySet()){
            RTree rTree = new RTree(minimumChildren, maximumChildren, numberOfCoordinates);
            rTreeMap.put(type,rTree);
            print.put(type, 0);
            rTreeList.get(zoomMap.get(type)).add(rTree);
        }
    }

    public void setClosetRoadRTree(RTree rTree){
        closetRoadRTree = rTree;
    }

    public void insert(Element element){
        String type = element.getType();
        if(zoomMap.get(type) != null ) {
            rTreeMap.get(type).insert(element);
            int i  = print.get(type);
            i++;
            print.put(type, i);
        }
    }

    public ArrayList<ArrayList<Element>> search(float xMin, float xMax, float yMin, float yMax, boolean debug, int currentZoomLevel){
        ArrayList<ArrayList<Element>> results = getCleanArrayList();

        for(int i = 0; i<= currentZoomLevel; i++) {
            for (RTree rTree : rTreeList.get(i)) {
                rTree.search(xMin, xMax, yMin, yMax, debug, results);
            }
        }

        return results;
    }

    public RTree.NearestRoadPriorityQueueEntry getNearestRoad(float x, float y){
        return closetRoadRTree.getNearestRoad(x,y);
    }

    /**
     * Make a list with as many lists as layers for the map.
     * @return An ArrayList with empty nested Arraylists.
     */
    private ArrayList<ArrayList<Element>> getCleanArrayList(){
        ArrayList<ArrayList<Element>> results = new ArrayList<>();
        while (results.size() <= returnListSize) {
            results.add(new ArrayList<>());
        }
        return results;
    }

    public void print(){
        for(Map.Entry<String, Integer> entry: print.entrySet()){
            System.out.println("Rtree " + entry.getKey() + ", " + entry.getValue());
        }
    }

}
