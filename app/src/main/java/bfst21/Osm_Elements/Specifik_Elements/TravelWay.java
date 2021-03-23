package bfst21.Osm_Elements.Specifik_Elements;

import bfst21.Osm_Elements.Way;

/**
 * A TravelWay is walkable, cycleable and driveable.
 */
public class TravelWay {
    private Way way;
    private String name;
    private final String type;
    private int maxspeed;
    private boolean onewayRoad;
    private String cycleway;
    private String footway;

    public TravelWay(Way way, String type){
        this.way = way;
        this.type = type;
    }


    public void setMaxspeed(int maxspeed) {
        this.maxspeed = maxspeed;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getMaxspeed() {
        return maxspeed;
    }
    public String getName() {
        return name;
    }
}
