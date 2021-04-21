package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Relation extends NodeHolder {

    private ArrayList<Way> ways;
    private String name; // TODO: 4/20/21 skal den bruges? 
    private boolean isMultiPolygon;

    private ArrayList<Way> innerWays;
    private ArrayList<Way> outerWays;


    private String restriction;
    private Way to, from, viaWay;
    private Node viaNode;

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

    public Node getViaNode() {
        return viaNode;
    }

    public Way getViaWay() {
        return viaWay;
    }

    public void setViaNode(Node viaNode) {
        this.viaNode = viaNode;
    }

    public void setViaWay(Way viaWay) {
        this.viaWay = viaWay;
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

    public String getRestriction() {
        return restriction;
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
            if(ways != null) {
                for (Way w : ways) {
                    w.draw(gc);
                }
            } if(nodes != null){
                super.draw(gc);
            }
        }
    }

    public void setIsMultiPolygon() {
        isMultiPolygon = true;
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
}
