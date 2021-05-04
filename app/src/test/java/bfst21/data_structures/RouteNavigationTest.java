package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;
import bfst21.exceptions.NoNavigationResultException;
import bfst21.utils.VehicleType;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

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

    private long getID() {
        idCount++;
        return idCount;
    }

    private Node createNode(float x, float y) {
        return new Node(getID(), x, y);
    }

    private Way createWay(ArrayList<Node> nodes, double maxSpeed) { // TODO: 5/4/21 udvid med typer
        Way w = new Way(getID());
        w.setAsHighWay();
        w.setMaxSpeed(maxSpeed);
        w.setType("unclassified");
        w.addAllNodes(nodes);
        for (Node n : nodes) {
            nodeToHighwayMap.put(n, w);
        }
        return w;
    }

    @Test
    void name() throws NoNavigationResultException {
        ArrayList<Node> nodesStraightWay = new ArrayList<>();
        nodesStraightWay.add(createNode(0, 0));
        nodesStraightWay.add(createNode(0, 4));
        Way straightWay = createWay(nodesStraightWay, 1);

        ArrayList<Node> nodesRightWay = new ArrayList<>();
        nodesRightWay.add(createNode(0, 4));
        nodesRightWay.add(createNode(3, 4));
        Way rightWay = createWay(nodesRightWay, 1);

        Node from = new Node(getID(), 0, 0);
        Node to = new Node(getID(), 3, 4);

        routeNavigation.setNodeToHighwayMap(nodeToHighwayMap);
        routeNavigation.setNodeToRestriction(nodeToRestriction);
        routeNavigation.setWayToRestriction(wayToRestriction);

        routeNavigation.setupRoute(from, to, straightWay, rightWay, new int[]{0, 1}, new int[]{0, 1}, VehicleType.CAR, true, true);
        routeNavigation.testGetCurrentRoute();
        System.out.println(routeNavigation.getTotalDistance());
    }
}