package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Relation extends NodeHolder {

    private ArrayList<Way> ways;
    private ArrayList<Node> inner;
    private ArrayList<Node> outer;
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
            checkMaxAndMin(way);
        }
    }

    private void checkMaxAndMin(Way way) {
        //if(yMin > way.getyMin())  yMin = way.getyMin();

    }


    public void setName(String name) {
        this.name = name;
    }

    public void setRestriction(String restriction) {
        this.restriction = restriction;
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (inner.size() > 0) {
            gc.beginPath();
            gc.moveTo(inner.get(0).getxMin(), inner.get(0).getyMin());
            for (var node : inner) {
                gc.lineTo(node.getxMin(), node.getyMin());
            }

        }

        if (outer.size() > 0) {

            gc.moveTo(outer.get(0).getxMin(), outer.get(0).getyMin());
            for (var node : outer) {
                gc.lineTo(node.getxMin(), node.getyMin());
            }
            gc.stroke();
        }

        // TODO: 28-03-2021 make draw method
        //gc.beginPath();

        /*for (Way w : ways) {
            gc.setStroke(Color.RED);
            List<Node> n = w.getNodes();

        }*/

        /*for (Way w : ways) {
            gc.setStroke(Color.RED);
            gc.beginPath();
            w.relationDraw(gc);
            gc.stroke();
        }*/


            /*if(isMultiPolygon){
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
            }*/
    }

    public void setIsMultiPolygon() {
        isMultiPolygon = true;
    }

    public boolean isMultiPolygon() {
        return isMultiPolygon;
    }

    private ArrayList<Way> mergeWays(ArrayList<Way> ways) {
        /*
         * Inner and outer rings are created from closed ways whenever possible,
         * except when these ways become very large (on the order of 2000 nodes). W
         * ays are usually not shared by different multipolygons.
         * From OSM wiki - mapping stype best practice with Relations
         */
        Map<Node, Way> pieces = new HashMap<>();
        for (var way : ways) {
            var before = pieces.remove(way.first());
            var after = pieces.remove(way.last());
            if (before == after) after = null;
            var merged = Way.merge(before, way, after);
            pieces.put(merged.first(), merged);
            pieces.put(merged.last(), merged);
        }
        ArrayList<Way> merged = new ArrayList<>();
        pieces.forEach((node, way) -> {
            if (way.last() == node) {
                merged.add(way);
            }
        });
        return merged;
    }


    public void LastWayOuter(boolean isOuter) {
        if (!ways.isEmpty()) {
            if (isOuter) {
                if (outer == null) outer = new ArrayList<>();
                outer.addAll(ways.get(ways.size() - 1).getNodes());
            } else {
                if (inner == null) inner = new ArrayList<>();
                inner.addAll(ways.get(ways.size() - 1).getNodes());
            }
        }
    }




}
