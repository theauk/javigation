package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;
import bfst21.exceptions.NoNavigationResultException;
import bfst21.utils.MapMath;
import bfst21.utils.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

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

    private float convertCoordinate(double coordinate) {
        return (float) MapMath.convertToScreen(coordinate);
    }

    private Node createNode(float x, double y) {
        return new Node(getID(), x, convertCoordinate(y));
    }

    private Way createWay(ArrayList<Node> nodes, double maxSpeed, String name, String type) { // TODO: 5/4/21 udvid med typer
        Way w = new Way(getID());
        w.setAsHighWay();
        w.setMaxSpeed(maxSpeed);
        w.setType(type);
        w.addAllNodes(nodes);
        w.setName(name);
        for (Node n : nodes) {
            nodeToHighwayMap.put(n, w);
        }
        return w;
    }

    private void setTreeMaps() {
        routeNavigation.setNodeToHighwayMap(nodeToHighwayMap);
        routeNavigation.setNodeToRestriction(nodeToRestriction);
        routeNavigation.setWayToRestriction(wayToRestriction);
    }

    @Test
    void rightTurnTest() throws NoNavigationResultException {
        Node from = createNode(12.1879864f , 55.449707);
        Node to = createNode(12.1882954f, 55.4500809);
        Node turnNode = createNode(12.1877728f, 55.4498749);

        ArrayList<Node> nodesStartWay = new ArrayList<>();
        nodesStartWay.add(createNode(12.1879864f , 55.449707));
        nodesStartWay.add(turnNode);
        Way startWay = createWay(nodesStartWay, 1, "Way 1", "unclassified");

        ArrayList<Node> nodesTurnRightOntoWay = new ArrayList<>();
        nodesTurnRightOntoWay.add(turnNode);
        nodesTurnRightOntoWay.add(createNode(12.1882954f , 55.4500809));
        Way turnRightOntoWay = createWay(nodesTurnRightOntoWay, 1, "Way 2", "unclassified");

        setTreeMaps();
        routeNavigation.setupRoute(from, to, startWay, turnRightOntoWay, new int[]{0, 1}, new int[]{0, 1}, VehicleType.CAR, false, true);
        routeNavigation.testGetCurrentRoute();

        String turnDirectionAStar = routeNavigation.getDirections().get(1);
        assertEquals("Turn right onto Way 2", turnDirectionAStar);

        routeNavigation.setupRoute(from, to, startWay, turnRightOntoWay, new int[]{0, 1}, new int[]{0, 1}, VehicleType.CAR, false, false);
        routeNavigation.testGetCurrentRoute();

        String turnDirectionDijkstra = routeNavigation.getDirections().get(1);
        assertEquals("Turn right onto Way 2", turnDirectionDijkstra);
    }

    @Test
    void leftTurnTest() throws NoNavigationResultException {
        Node from = createNode(12.1882954f, 55.4500809);
        Node to = createNode(12.1879864f , 55.449707);
        Node turnNode = createNode(12.1877728f, 55.4498749);

        ArrayList<Node> nodesStartWay = new ArrayList<>();
        nodesStartWay.add(turnNode);
        nodesStartWay.add(createNode(12.1882954f , 55.4500809));
        Way startWay = createWay(nodesStartWay, 1, "Way 1", "unclassified");

        ArrayList<Node> turnLeftOntoWayNodes = new ArrayList<>();
        turnLeftOntoWayNodes.add(createNode(12.1879864f , 55.449707));
        turnLeftOntoWayNodes.add(turnNode);
        Way turnLeftOntoWay = createWay(turnLeftOntoWayNodes, 1, "Way 2", "unclassified");

        setTreeMaps();
        routeNavigation.setupRoute(from, to, startWay, turnLeftOntoWay, new int[]{0, 1}, new int[]{0, 1}, VehicleType.CAR, false, true);
        routeNavigation.testGetCurrentRoute();

        String turnDirection = routeNavigation.getDirections().get(1);
        assertEquals("Turn left onto Way 2", turnDirection);

        routeNavigation.setupRoute(from, to, startWay, turnLeftOntoWay, new int[]{0, 1}, new int[]{0, 1}, VehicleType.CAR, false, false);
        routeNavigation.testGetCurrentRoute();

        String turnDirectionDijkstra = routeNavigation.getDirections().get(1);
        assertEquals("Turn left onto Way 2", turnDirectionDijkstra);
    }

    @Test
    void noNavigationResultExceptionTest() throws NoNavigationResultException {
        Node from = createNode(12.1882954f, 55.4500809);
        ArrayList<Node> fromNodes = new ArrayList<>();
        fromNodes.add(from);
        fromNodes.add(createNode(12.1882955f, 55.4500806));
        Way fromWay = createWay(fromNodes, 50, "Way 1", "unclassified");

        Node to = createNode(12.1879864f , 55.449707);
        ArrayList<Node> toNodes = new ArrayList<>();
        toNodes.add(to);
        toNodes.add(createNode(12.1879863f , 55.449705));
        Way toWay = createWay(toNodes, 50, "Way 2", "unclassified");

        setTreeMaps();
        routeNavigation.setupRoute(from, to, fromWay, toWay, new int[]{0, 1}, new int[]{0, 1}, VehicleType.CAR, false, true);

        assertThrows(NoNavigationResultException.class, () -> {
            routeNavigation.testGetCurrentRoute();
        });
        assertEquals(0, routeNavigation.getTotalDistance());
        assertEquals(0, routeNavigation.getTotalTime());
    }

    @Test
    void getDistanceAndTimeTest() throws NoNavigationResultException {
        // Assumption distance found via: https://www.movable-type.co.uk/scripts/latlong.html
        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(createNode(12.1934f, 55.4453));
        nodes.add(createNode(12.2024f, 55.4378));
        double speed = 50;
        Way w = createWay(nodes, speed, "Way 1", "unclassified");

        setTreeMaps();
        routeNavigation.setupRoute(nodes.get(0), nodes.get(1), w, w, new int[]{0, 1}, new int[]{0, 1}, VehicleType.CAR, false, true);
        routeNavigation.testGetCurrentRoute();
        double distance = Math.round(routeNavigation.getTotalDistance());
        assertEquals(1009.0, distance);

        double speedMs = speed * (5f / 18f);
        double timeCalculated = Math.round(distance / speedMs);
        double time = Math.round(routeNavigation.getTotalTime());
        assertEquals(timeCalculated, time);
    }

    @Test
    void continueOnSameWayTest() throws NoNavigationResultException {
        Node from = createNode(12.1882954f, 55.4500809);
        Node to = createNode(12.1879864f , 55.449707);
        Node turnNode = createNode(12.1877728f, 55.4498749);

        ArrayList<Node> nodesStartWay = new ArrayList<>();
        nodesStartWay.add(turnNode);
        nodesStartWay.add(createNode(12.1882954f , 55.4500809));
        Way startWay = createWay(nodesStartWay, 1, "WayName", "unclassified");

        ArrayList<Node> turnLeftOntoWayNodes = new ArrayList<>();
        turnLeftOntoWayNodes.add(createNode(12.1879864f , 55.449707));
        turnLeftOntoWayNodes.add(turnNode);
        Way turnLeftOntoWay = createWay(turnLeftOntoWayNodes, 1, "WayName", "unclassified");

        setTreeMaps();
        routeNavigation.setupRoute(from, to, startWay, turnLeftOntoWay, new int[]{0, 1}, new int[]{0, 1}, VehicleType.CAR, false, true);
        routeNavigation.testGetCurrentRoute();

        assertEquals(1, routeNavigation.getDirections().size());
    }

    @Test
    void takeFastestWayTest() {

    }

    @Test
    void ferrySpecialPathFeaturesTest() throws NoNavigationResultException {
        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(createNode(12.1934f, 55.4453));
        nodes.add(createNode(12.2024f, 55.4378));
        Way w = createWay(nodes, 50, "Ferry from A - B", "ferry");

        setTreeMaps();
        routeNavigation.setupRoute(nodes.get(0), nodes.get(1), w, w, new int[]{0, 1}, new int[]{0, 1}, VehicleType.CAR, false, true);
        routeNavigation.testGetCurrentRoute();
        assertTrue(routeNavigation.getSpecialPathFeatures().contains("a ferry"));
    }

    @Test
    void getCoordinatesForPanToRouteTest() throws NoNavigationResultException {
        Node from = createNode(12.18f, 55.445);
        Node to = createNode(12.22f, 55.45);
        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(createNode(12.19f, 55.445));
        nodes.add(createNode(12.20f, 55.437));
        nodes.add(createNode(12.21f, 55.438));
        Way w = createWay(nodes, 50, "WayName", "unclassified");

        setTreeMaps();
        routeNavigation.setupRoute(from, to, w, w, new int[]{0, 1}, new int[]{1, 2}, VehicleType.CAR, false, true);
        routeNavigation.testGetCurrentRoute();

        System.out.println(Arrays.toString(routeNavigation.getCoordinatesForPanToRoute()));
        double yMin = MapMath.convertToScreen(55.445);
        double yMax = MapMath.convertToScreen(55.438);

        // y-min and max are inserted opposite due to the conversion to screen coordinates
        assertArrayEquals(new float[]{12.19f, 12.21f, (float) yMin, (float) yMax}, routeNavigation.getCoordinatesForPanToRoute());
    }

    @Test
    void onlyBikeOnBikeRoadsTest() {

    }

    @Test
    void onlyWalkOnWalkingRoadsTest() {

    }

    @Test
    void restrictionViaNodeTest() {

    }

    @Test
    void restrictionViaWayTest() {

    }

    @Test
    void roundaboutExitTest() {

    }

    @Test
    void keepRightTest() {

    }


}