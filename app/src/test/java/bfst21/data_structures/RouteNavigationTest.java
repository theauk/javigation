package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class RouteNavigationTest {

    private long idCount;
    private RouteNavigation routeNavigation;
    private ElementToElementsTreeMap<Node, Way> nodeToHighwayMap;
    private ElementToElementsTreeMap<Node, Relation> nodeToRestriction;
    private ElementToElementsTreeMap<Way, Relation> wayToRestriction;

    @BeforeEach
    void setUp() {
        idCount = 0;
        routeNavigation = new RouteNavigation();
        nodeToHighwayMap = new ElementToElementsTreeMap<>();
        nodeToRestriction = new ElementToElementsTreeMap<>();
        wayToRestriction = new ElementToElementsTreeMap<>();
    }

    private Node createNode(float x, float y) {
        Node n = new Node(idCount, x, y);
        idCount++;
        return n;
    }

    private void createWays(ArrayList<Node> nodes) { // TODO: 5/4/21 udvid med typer
        Way w = new Way(idCount);
        idCount++;
        w.setAsHighWay();
        w.addAllNodes(nodes);
        for (Node n : nodes) {
            nodeToHighwayMap.put(n, w);
        }
    }

    @Test
    void name() {
        ArrayList<Node> nodes = new ArrayList<>();
        int[] fromCoordinates = new int[]{0, 0};
        int[] toCoordinates = new int[]{5, 5};

    }
}