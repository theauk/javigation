package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class Relation extends NodeHolder {

    private ArrayList<Way> ways;
    private ArrayList<Node> nodes;
    private ArrayList<Relation> relations;
    private String name;
    private String route; // probably shouldn't be string - ex. it's so if the relation is a route for bicycle
    private String type;
    // route have distances.

    public Relation(long id) {
        super(id);
        ways = new ArrayList<>();
        nodes = new ArrayList<>();
        relations = new ArrayList<>();
    }

    public ArrayList<Way> getWays() {
        return ways;
    }

    public void addWay(Way way) {
        ways.add(way);
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    @Override
    public void draw(GraphicsContext gc) {

    }
}
