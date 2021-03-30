package bfst21.Osm_Elements.Specifik_Elements;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;

/**
 * A TravelWay is walkable, cycleable and driveable.
 */
public class TravelWay {
    private final String type;
    private Way way;
    private String name;
    private double maxspeed;
    private boolean onewayRoad;
    private String cycleway;
    private String footway;

    public TravelWay(Way way, String type) {
        this.way = way;
        this.type = type;


    }

    public void setCycleway(String cycleway) {
        this.cycleway = cycleway;
    }

    public void setFootway(String footway) {
        this.footway = footway;
    }

    public void setOnewayRoad(boolean onewayRoad) {
        this.onewayRoad = onewayRoad;
    }

    public String getType() {
        return type;
    }

    public Way getWay() {
        return way;
    }

    public double getMaxspeed() {
        return maxspeed;
    }

    public void setMaxspeed(double maxspeed) {
        this.maxspeed = maxspeed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        //TODO way is null??
        if (way != null) {
            for (Node n : way.getNodes()) {
                n.addRoadname(name);
            }
        }
    }
}
