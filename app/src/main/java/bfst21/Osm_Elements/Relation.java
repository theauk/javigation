package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Relation extends NodeHolder {

    private ArrayList<Way> ways;
    private ArrayList<Way> inner;
    private ArrayList<Way> outer;
    private String name;
    private String restriction;
    private boolean isMultiPolygon;

    public Relation(long id) {
        super(id);
        ways = new ArrayList<>();
    }

    public ArrayList<Way> getWays() {
        return ways;
    }

    // TODO: 28-03-2021 due to small input not all relations are "full" // some elements are missing, therefore we need to check for null 
    public void addWay(Way way) {
        if (way != null) {
            ways.add(way);
        }
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setRestriction(String restriction) {
        this.restriction = restriction;
    }

    @Override
    public void draw(GraphicsContext gc) {
        // TODO: 28-03-2021 make draw method
        gc.beginPath();


            if(isMultiPolygon){
                if(inner != null){
                    for (Way way : inner) {
                        way.draw(gc);
                    }
                }
                if(outer != null){
                    for (Way way : outer) {
                        way.draw(gc);
                    }
                }

                gc.fill();
            }else {
                for (Way way : ways) { // TODO: 3/28/21 for rtree debug
                    way.draw(gc);
                }
                gc.stroke();
            }
    }
    public void setIsMultiPolygon(){
        isMultiPolygon = true;
    }

    public boolean isMultiPolygon(){
        return isMultiPolygon;
    }

    private ArrayList<Way> mergeWays(ArrayList<Way> ways) {
        /*
        * Inner and outer rings are created from closed ways whenever possible,
        * except when these ways become very large (on the order of 2000 nodes). W
        * ays are usually not shared by different multipolygons.
        * From OSM wiki - mapping stype best practice with Relations
        */
        Map<Node,Way> pieces = new HashMap<>();
        for (var way : ways) {
            var before = pieces.remove(way.first());
            var after = pieces.remove(way.last());
            if (before == after) after = null;
            var merged = Way.merge(before, way, after);
            pieces.put(merged.first(),merged);
            pieces.put(merged.last(),merged);
        }
        ArrayList<Way> merged = new ArrayList<>();
        pieces.forEach((node,way) -> {
            if (way.last() == node) {
                merged.add(way);
            }
        });
        return merged;
    }


    public void LastWayOuter(boolean isOuter) {
        if(!ways.isEmpty()) {
            if (isOuter) {
                if (outer == null) outer = new ArrayList<>();
                outer.add(ways.get(ways.size() - 1));
            } else {
                if (inner == null) inner = new ArrayList<>();
                inner.add(ways.get(ways.size() - 1));
            }
        }
    }
}
