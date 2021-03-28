package bfst21.Osm_Elements.Specifik_Elements;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A TravelWay is walkable, cycleable and driveable.
 */
public class TravelWay extends Way{
    private String roadType;
    private String name;
    private int maxspeed;
    private boolean onewayRoad;
    private boolean isDriveable;
    private boolean isCycleable;
    private boolean isWalkable;

    public TravelWay(Way way, String roadType) {
        super(way.getId());
        super.addAllNodes(way.getNodes());
        roadType = roadType;
        setBooleans(roadType);
    }

    private void setBooleans(String type){
            isWalkable = true;
            isCycleable = true;
            isDriveable = true;
            onewayRoad = false;
            if(type.equals("motorway") || type.equals("trunk")){
                setNotCycleable();
                setNotWalkable();
           }
            if(type.equals("pedestrian") || type.equals("footway") || type.equals("steps")){
                setNotDriveable();
            }
            if(type.equals("cycleway")){
                setNotDriveable();
                setNotWalkable();
            }



    }

    public void setNotCycleable() {
        this.isCycleable = false;
    }

    public void setNotWalkable() {
        this.isWalkable = false;
    }

    public void setNotDriveable(){
        isDriveable = false;
    }

    public void setOnewayRoad() {
        onewayRoad = true;
    }

    public String getRoadType() {
        return roadType;
    }

    public int getMaxspeed() {
        return maxspeed;
    }

    public void setMaxspeed(int maxspeed) {
        this.maxspeed = maxspeed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        for(Node n: nodes){
            n.addRoadname(name);
        }
    }
    @Override
    public void draw(GraphicsContext gc) {
        //TODO Should check for one way.....
        gc.beginPath();
        gc.moveTo(nodes.get(0).getxMin(), nodes.get(0).getyMin());
        for (var node : nodes) {
            gc.lineTo(node.getxMin(), node.getyMin());
        }
        gc.stroke();
    }

}
