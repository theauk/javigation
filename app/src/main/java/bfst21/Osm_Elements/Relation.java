package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Relation extends NodeHolder {

    private ArrayList<Way> ways;
    private String name;
    private boolean isMultiPolygon;

    private ArrayList<Way> innerWays;
    private ArrayList<Way> outerWays;


    private String restriction;
    private Way to, from;
    private Node via;

    public Way getTo() {
        return to;
    }

    public void setTo(Way to) {
        this.to = to;
    }

    public Way getFrom() {
        return from;
    }

    public void setFrom(Way from) {
        this.from = from;
    }

    public Node getVia() {
        return via;
    }

    public void setVia(Node via) {
        this.via = via;
    }

    public Relation(long id) {
        super(id);
        ways = new ArrayList<>();
    }

    public ArrayList<Way> getWays() {
        return ways;
    }

    public void addWay(Way way) {
        if (way != null) {
            ways.add(way);
            updateCoordinates(way);
        }
    }

    private void updateCoordinates(Way way) {
        checkX(way.xMin);
        checkX(way.xMax);
        checkY(way.yMin);
        checkY(way.yMax);
    }

    public void addInnerOuterWay(Way way, boolean inner) {
        if (innerWays == null || outerWays == null) {
            innerWays = new ArrayList<>();
            outerWays = new ArrayList<>();
        }
        if (inner) {
            innerWays.add(way);
        } else {
            outerWays.add(way);
        }
        updateCoordinates(way);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRestriction(String restriction) {
        this.restriction = restriction;
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (isMultiPolygon) {
            if (innerWays.size() > 0) {
                for (Way w : innerWays) {
                    w.draw(gc);
                }
            }

            if (outerWays.size() > 0) {
                for (Way w : outerWays) {
                    w.draw(gc);
                }
            }
        } else {
            if (ways != null) {
                for (Way w : ways) {
                    w.draw(gc);
                }
            }
            if (nodes != null && nodes.size() != 0) {
                super.draw(gc);
            }
        }
    }

    public void setIsMultiPolygon() {
        isMultiPolygon = true;
    }

    public void mergeWays() {
        System.out.println("ways before :" + ways.size());
        ways = mergeWays(ways);
        System.out.println("ways after :" + ways.size());
    }

    private ArrayList<Way> mergeWays(ArrayList<Way> toMerge) {
        /*
         * Inner and outer rings are created from closed ways whenever possible,
         * except when these ways become very large (on the order of 2000 nodes). W
         * ays are usually not shared by different multipolygons.
         * From OSM wiki - mapping stype best practice with Relations
         */
        Map<Node, Way> pieces = new HashMap<>();
        ArrayList<Way> mergedList = new ArrayList<>();
        for (Way way : toMerge) {
            if (way.first() == way.last()) {
                mergedList.add(way);
            } else {
                Way before = pieces.remove(way.first());
                Way after = pieces.remove(way.last());
                if (before == after) after = null;
                Way merged = merge(before, way, after);
                pieces.put(merged.first(), merged);
                pieces.put(merged.last(), merged);

            }
        }
                pieces.forEach((node, way) -> {
                    if (way.last() == node) {
                        mergedList.add(way);
                    }
                });

        return mergedList;
    }

    private ArrayList<Way> mergeWaysTemp(ArrayList<Way> toMerge) {
        ArrayList<Way> mergedList = new ArrayList<>();
        for (Way way : toMerge) {
            if (way.first() == way.last()) {
                mergedList.add(way);
            } else {

            }

        }
        return mergedList;
    }





     private Way mergeTwoWays(Way w1, Way w2){
        List<Node> list = w2.getNodes();
        list.remove(1);
        w1.getNodes().addAll(list);
        return w1;

     }

    private Way merge(Way before, Way coast, Way after) {
        return merge(merge(before, coast), after);
    }

    private Way merge(Way first, Way second) {
        if (first == null) return second;
        if (second == null) return first;
        Way merged = new Way();
        merged.nodes.addAll(first.nodes);
        merged.nodes.addAll(second.nodes.subList(1, second.nodes.size()));
        return merged;
    }



}