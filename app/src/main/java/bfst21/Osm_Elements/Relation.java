package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class Relation extends NodeHolder {

    private ArrayList<Way> ways;
    private String name;
    private String restriction; // probably shouldn't be string - ex. it's so if the relation is a route for bicycle
    private String type;
    // route have distances.

    public Relation(long id) {
        super(id);
        ways = new ArrayList<>();
    }

    public ArrayList<Way> getWays() {
        return ways;
    }

    // TODO: 28-03-2021 due to small input not all relations are "full" // some elements are missing, therefore we need to check for null 
    public void addWay(Way way) {
        if(way != null){
            ways.add(way);
        }
        
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRestriction(String restriction) {
        this.restriction = restriction;
    }

    @Override
    public void draw(GraphicsContext gc) {
        // TODO: 28-03-2021 make draw method

    }
}
