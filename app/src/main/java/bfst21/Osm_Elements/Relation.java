package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class Relation extends NodeHolder {

    private ArrayList<Way> ways;
    private String name;
    private String restriction;

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

        for (Way way : ways) { // TODO: 3/28/21 for rtree debug
            gc.moveTo(way.nodes.get(0).getxMin(), way.nodes.get(0).getyMin());
            for (Node node : way.nodes) {
                gc.lineTo(node.getxMin(), node.getyMin());
            }
            gc.stroke();
        }
    }
}
