package bfst21.osm_elements;

import java.util.ArrayList;

public class Relation extends NodeHolder {

    private ArrayList<Way> ways;
    private ArrayList<Node> nodes;

    public Relation() {
        ways = new ArrayList<>();
        nodes = new ArrayList<>();
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
}
