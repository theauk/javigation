package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;
import bfst21.exceptions.NoNavigationResultException;
import bfst21.utils.VehicleType;
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

    private long getID() {
        idCount++;
        return idCount;
    }

    private float convertCoordinate(double coordinate) {
        return (float) -coordinate / 0.56f;
    }

    private Node createNode(float x, double y) {
        return new Node(getID(), x, convertCoordinate(y));
    }

    private Way createWay(ArrayList<Node> nodes, double maxSpeed, String name, String type) {
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
        Node from = createNode(12.1879864f, 55.449707);
        Node to = createNode(12.1882954f, 55.4500809);
        Node turnNode = createNode(12.1877728f, 55.4498749);

        ArrayList<Node> nodesStartWay = new ArrayList<>();
        nodesStartWay.add(createNode(12.1879864f, 55.449707));
        nodesStartWay.add(turnNode);
        Way startWay = createWay(nodesStartWay, 1, "Way 1", "unclassified");

        ArrayList<Node> nodesTurnRightOntoWay = new ArrayList<>();
        nodesTurnRightOntoWay.add(turnNode);
        nodesTurnRightOntoWay.add(createNode(12.1882954f, 55.4500809));
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
        Node to = createNode(12.1879864f, 55.449707);
        Node turnNode = createNode(12.1877728f, 55.4498749);

        ArrayList<Node> nodesStartWay = new ArrayList<>();
        nodesStartWay.add(turnNode);
        nodesStartWay.add(createNode(12.1882954f, 55.4500809));
        Way startWay = createWay(nodesStartWay, 1, "Way 1", "unclassified");

        ArrayList<Node> turnLeftOntoWayNodes = new ArrayList<>();
        turnLeftOntoWayNodes.add(createNode(12.1879864f, 55.449707));
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

        Node to = createNode(12.1879864f, 55.449707);
        ArrayList<Node> toNodes = new ArrayList<>();
        toNodes.add(to);
        toNodes.add(createNode(12.1879863f, 55.449705));
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
        Node to = createNode(12.1879864f, 55.449707);
        Node turnNode = createNode(12.1877728f, 55.4498749);

        ArrayList<Node> nodesStartWay = new ArrayList<>();
        nodesStartWay.add(turnNode);
        nodesStartWay.add(createNode(12.1882954f, 55.4500809));
        Way startWay = createWay(nodesStartWay, 1, "WayName", "unclassified");

        ArrayList<Node> turnLeftOntoWayNodes = new ArrayList<>();
        turnLeftOntoWayNodes.add(createNode(12.1879864f, 55.449707));
        turnLeftOntoWayNodes.add(turnNode);
        Way turnLeftOntoWay = createWay(turnLeftOntoWayNodes, 1, "WayName", "unclassified");

        setTreeMaps();
        routeNavigation.setupRoute(from, to, startWay, turnLeftOntoWay, new int[]{0, 1}, new int[]{0, 1}, VehicleType.CAR, false, true);
        routeNavigation.testGetCurrentRoute();

        assertEquals(1, routeNavigation.getDirections().size());
    }

    @Test
    void ferrySpecialPathFeaturesTest() throws NoNavigationResultException {
        ArrayList<Node> nodes = new ArrayList<>();
        Node sharedNode = createNode(12.1934f, 55.4453);
        nodes.add(sharedNode);
        Node endFerryNode = createNode(12.2024f, 55.4378);
        nodes.add(endFerryNode);
        Way w = createWay(nodes, 50, "Ferry from A - B", "ferry");

        ArrayList<Node> beforeWayNodes = new ArrayList<>();
        beforeWayNodes.add(createNode(12.1930f, 55.4450));
        beforeWayNodes.add(sharedNode);
        Way beforeWay = createWay(beforeWayNodes, 50, "Before Way", "Way");

        ArrayList<Node> afterWayNodes = new ArrayList<>();
        afterWayNodes.add(endFerryNode);
        afterWayNodes.add(createNode(12.21f, 55.4480));
        Way afterWay = createWay(afterWayNodes, 50, "After Way", "Way");

        setTreeMaps();
        routeNavigation.setupRoute(createNode(12.1930f, 55.4450), createNode(12.21f, 55.4480), beforeWay, afterWay, new int[]{0, 1}, new int[]{0, 1}, VehicleType.CAR, false, true);
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

        double yMin = convertCoordinate(55.445);
        double yMax = convertCoordinate(55.438);

        assertArrayEquals(new float[]{12.19f, 12.21f, (float) yMin, (float) yMax}, routeNavigation.getCoordinatesForPanToRoute());
    }

    @Test
    void onlyBikeOnBikeRoadsTest() throws NoNavigationResultException {
        Node from = createNode(12.19f, 55.445);
        Node to = createNode(12.21f, 55.438);

        ArrayList<Node> nodesMotorway = new ArrayList<>();
        nodesMotorway.add(from);
        nodesMotorway.add(createNode(12.20f, 55.437));
        nodesMotorway.add(to);
        Way motorway = createWay(nodesMotorway, 110, "Motorway", "motorway");

        ArrayList<Node> nodesBikeWay = new ArrayList<>();
        nodesBikeWay.add(from);
        nodesBikeWay.add(createNode(12.4f, 55.437));
        nodesBikeWay.add(to);
        createWay(nodesBikeWay, 20, "Bike Way", "cycleway");

        setTreeMaps();
        routeNavigation.setupRoute(from, to, motorway, motorway, new int[]{0, 1}, new int[]{1, 2}, VehicleType.BIKE, false, true);
        routeNavigation.testGetCurrentRoute();

        assertFalse(routeNavigation.getDirections().get(0).contains("Motorway"));
        assertTrue(routeNavigation.getDirections().get(0).contains("Bike Way"));
    }

    @Test
    void onlyWalkOnWalkingRoadsTest() throws NoNavigationResultException {
        Node from = createNode(13.19f, 55.445);
        Node to = createNode(13.21f, 55.438);

        ArrayList<Node> nodesMotorway = new ArrayList<>();
        nodesMotorway.add(from);
        nodesMotorway.add(createNode(14.20f, 55.437));
        nodesMotorway.add(to);
        Way motorway = createWay(nodesMotorway, 110, "Motorway", "motorway");

        ArrayList<Node> nodesWalkWay = new ArrayList<>();
        nodesWalkWay.add(from);
        nodesWalkWay.add(createNode(16f, 55.437));
        nodesWalkWay.add(to);
        Way walkWay = createWay(nodesWalkWay, 5, "Walk Way", "footway");

        setTreeMaps();
        routeNavigation.setupRoute(from, to, motorway, walkWay, new int[]{1, 2}, new int[]{0, 1}, VehicleType.WALK, false, true);
        routeNavigation.testGetCurrentRoute();

        assertFalse(routeNavigation.getDirections().get(0).contains("Motorway"));
        assertTrue(routeNavigation.getDirections().get(0).contains("Walk Way"));
    }

    @Test
    void takeFastestWayTest() throws NoNavigationResultException {
        Node from = createNode(13f, 55.4);
        Node to = createNode(15f, 55.5);

        ArrayList<Node> nodesSlowWay = new ArrayList<>();
        nodesSlowWay.add(from);
        nodesSlowWay.add(createNode(14f, 55.45));
        nodesSlowWay.add(to);
        Way slowWay = createWay(nodesSlowWay, 30, "Slow Way", "unclassified");

        ArrayList<Node> nodesFastWay = new ArrayList<>();
        nodesFastWay.add(from);
        nodesFastWay.add(createNode(14f, 55.45));
        nodesFastWay.add(to);
        createWay(nodesFastWay, 100, "Fast Way", "unclassified");

        setTreeMaps();

        // A*
        routeNavigation.setupRoute(from, to, slowWay, slowWay, new int[]{0, 1}, new int[]{1, 2}, VehicleType.CAR, true, true);
        routeNavigation.testGetCurrentRoute();
        assertFalse(routeNavigation.getDirections().get(0).contains("Slow Way"));
        assertTrue(routeNavigation.getDirections().get(0).contains("Fast Way"));

        // Dijkstra
        routeNavigation.setupRoute(from, to, slowWay, slowWay, new int[]{0, 1}, new int[]{1, 2}, VehicleType.CAR, true, false);
        routeNavigation.testGetCurrentRoute();
        assertFalse(routeNavigation.getDirections().get(0).contains("Slow Way"));
        assertTrue(routeNavigation.getDirections().get(0).contains("Fast Way"));
    }

    @Test
    void roundaboutExitTest() throws NoNavigationResultException {
        // exit nodes
        Node exitNode1 = createNode(12.21058f, 55.51891);
        Node exitNode2 = createNode(12.21081f, 55.51913);
        Node exitNode3 = createNode(12.21051f, 55.51910);

        // exit ways
        ArrayList<Node> exit1WayNodes = new ArrayList<>();
        exit1WayNodes.add(createNode(12.21045f, 55.51882));
        exit1WayNodes.add(exitNode1);
        Way exit1Way = createWay(exit1WayNodes, 30, "Exit 1", "unclassified");

        ArrayList<Node> exit2WayNodes = new ArrayList<>();
        exit2WayNodes.add(createNode(12.21102f, 55.51931));
        exit2WayNodes.add(exitNode2);
        Way exit2Way = createWay(exit2WayNodes, 30, "Exit 2", "unclassified");

        ArrayList<Node> exit3WayNodes = new ArrayList<>();
        exit3WayNodes.add(createNode(12.20993f, 55.51923));
        exit3WayNodes.add(exitNode3);
        Way exit3Way = createWay(exit3WayNodes, 30, "Exit 3", "unclassified");

        // roundabout way
        ArrayList<Node> roundaboutWayNodes = new ArrayList<>();
        roundaboutWayNodes.add(exitNode1);
        roundaboutWayNodes.add(exitNode2);
        roundaboutWayNodes.add(exitNode3);
        Way roundaboutWay = createWay(roundaboutWayNodes, 30, "Roundabout Way", "roundabout");
        roundaboutWay.setOnewayRoad();

        setTreeMaps();
        routeNavigation.setupRoute(createNode(12.21045f, 55.51882), createNode(12.20993f, 55.51923), exit1Way, exit3Way, new int[]{0, 1}, new int[]{0, 1}, VehicleType.CAR, true, true);
        routeNavigation.testGetCurrentRoute();

        ArrayList<String> directions = routeNavigation.getDirections();
        assertTrue(directions.get(1).contains("At the roundabout, take the 2. exit onto Exit 3"));
        assertTrue(directions.get(2).contains("Follow Exit 3 and you will arrive at your destination"));

        // Test that one-way exits should not be counted
        exit2Way.setOnewayRoad();
        setTreeMaps();
        routeNavigation.setupRoute(createNode(12.21045f, 55.51882), createNode(12.20993f, 55.51923), exit1Way, exit3Way, new int[]{0, 1}, new int[]{0, 1}, VehicleType.CAR, true, true);
        routeNavigation.testGetCurrentRoute();
        directions = routeNavigation.getDirections();
        assertTrue(directions.get(1).contains("At the roundabout, take the 1. exit onto Exit 3"));
        assertTrue(directions.get(2).contains("Follow Exit 3 and you will arrive at your destination"));
    }

    @Test
    void keepRightTest() throws NoNavigationResultException {
        Node linkNode = createNode(12.1879863f, 55.449706);

        ArrayList<Node> wayBeforeExitNodes = new ArrayList<>();
        wayBeforeExitNodes.add(createNode(12.1879864f, 55.449707));
        wayBeforeExitNodes.add(linkNode);
        Way wayBeforeExit = createWay(wayBeforeExitNodes, 130, "Motorway", "motorway");

        Node linkNodeAfterExit = createNode(12.1879862f, 55.449705);
        ArrayList<Node> exitWayNodes = new ArrayList<>();
        exitWayNodes.add(linkNode);
        exitWayNodes.add(linkNodeAfterExit);
        createWay(exitWayNodes, 130, "Exit 13", "motorway_link");

        ArrayList<Node> wayAfterExitNodes = new ArrayList<>();
        wayAfterExitNodes.add(linkNodeAfterExit);
        wayAfterExitNodes.add(createNode(12.1879859f, 55.449707));
        Way wayAfterExit = createWay(wayAfterExitNodes, 130, "Way after exit", "motorway_link");

        setTreeMaps();
        routeNavigation.setupRoute(createNode(12.1879865f, 55.449707), createNode(12.187984f, 55.449707), wayBeforeExit, wayAfterExit, new int[]{0, 1}, new int[]{1, 2}, VehicleType.CAR, true, true);
        routeNavigation.testGetCurrentRoute();

        String keepRightDirection = routeNavigation.getDirections().get(1);
        assertTrue(keepRightDirection.contains("Keep right and take Exit 13"));
    }

    @Test
    void restrictionViaNodeTest() throws NoNavigationResultException {
        Relation restriction = new Relation(0);
        Node viaRestriction = createNode(11.9373f, 55.2384);

        ArrayList<Node> restrictionWayNodes = new ArrayList<>();
        Node from = createNode(11.9301f, 55.2384);
        Node to = createNode(11.9422f, 55.2384);
        restrictionWayNodes.add(from);
        restrictionWayNodes.add(viaRestriction);
        restrictionWayNodes.add(to);
        Way restrictionWay = createWay(restrictionWayNodes, 80, "Restriction Way", "Way");

        restriction.setFrom(restrictionWay);
        restriction.setViaNode(viaRestriction);
        restriction.setTo(restrictionWay);
        restriction.setRestriction("no_left_turn");
        restriction.setType("restriction");
        nodeToRestriction.put(restriction.getViaNode(), restriction);

        ArrayList<Node> slowWayNodes = new ArrayList<>();
        slowWayNodes.add(from);
        slowWayNodes.add(createNode(11.9381f, 55.2406));
        slowWayNodes.add(to);
        Way slowWay = createWay(slowWayNodes, 30, "Slow Way", "Way");

        ArrayList<Node> beforeWayNodes = new ArrayList<>();
        Node startNode = createNode(11.9283f, 55.2388);
        beforeWayNodes.add(createNode(11.9283f, 55.2388));
        beforeWayNodes.add(from);
        Way beforeWay = createWay(beforeWayNodes, 50, "Before Way", "Way");

        setTreeMaps();
        routeNavigation.setupRoute(startNode, to, beforeWay, slowWay, new int[]{0, 1}, new int[]{1, 2}, VehicleType.CAR, false, true);
        routeNavigation.testGetCurrentRoute();

        for (String s : routeNavigation.getDirections()) {
            assertFalse(s.contains("Restriction Way"));
        }
    }

    @Test
    void restrictionViaWayTest() throws NoNavigationResultException {
        Node fromRestrictionNode = createNode(12.57014f, 55.66954);
        Node betweenNode = createNode(12.57034f, 55.66960);
        Node toRestrictionNode = createNode(12.57027f, 55.66966);

        ArrayList<Node> restrictionWayNodes = new ArrayList<>();
        restrictionWayNodes.add(fromRestrictionNode);
        restrictionWayNodes.add(createNode(12.57021f, 55.66955));
        restrictionWayNodes.add(betweenNode);
        Way restrictionWay = createWay(restrictionWayNodes, 50, "Restriction Way", "Way");

        ArrayList<Node> fromWayNodes = new ArrayList<>();
        fromWayNodes.add(createNode(12.56978f, 55.66981));
        fromWayNodes.add(fromRestrictionNode);
        Node nodeThatThePathShouldGoVia = createNode(12.57051f, 55.66937);
        fromWayNodes.add(nodeThatThePathShouldGoVia);
        Way fromWay = createWay(fromWayNodes, 50, "From Way", "Way");

        ArrayList<Node> toWayNodes = new ArrayList<>();
        toWayNodes.add(nodeThatThePathShouldGoVia);
        toWayNodes.add(betweenNode);
        toWayNodes.add(toRestrictionNode);

        Way toWay = createWay(toWayNodes, 50, "To Way", "Way");

        Relation restriction = new Relation(0);
        restriction.setFrom(fromWay);
        restriction.setViaWay(restrictionWay);
        restriction.setTo(toWay);
        restriction.setRestriction("no_u_turn");
        restriction.setType("restriction");
        wayToRestriction.put(restriction.getViaWay(), restriction);

        setTreeMaps();
        routeNavigation.setupRoute(createNode(12.56978f, 55.66981), createNode(12.57027f, 55.66966), fromWay, toWay, new int[]{0, 1}, new int[]{1, 2}, VehicleType.CAR, true, true);
        routeNavigation.testGetCurrentRoute();

        for (String s : routeNavigation.getDirections()) {
            assertFalse(s.contains("Restriction Way"));
        }
    }

    @Test
    void oneWayTest() throws NoNavigationResultException {
        ArrayList<Node> oneWayNodes = new ArrayList<>();
        Node fromNode = createNode(14.71659f, 55.09957);
        Node toNode = createNode(14.7205f, 55.1014);
        oneWayNodes.add(toNode);
        oneWayNodes.add(fromNode);
        Way oneWay = createWay(oneWayNodes, 50, "One Way Way", "Way");
        oneWay.setOnewayRoad();

        ArrayList<Node> slowWayNodes = new ArrayList<>();
        slowWayNodes.add(fromNode);
        slowWayNodes.add(createNode(14.72291f, 55.09890));
        slowWayNodes.add(toNode);
        Way slowWay = createWay(slowWayNodes, 50, "Slow Way", "Way");

        ArrayList<Node> beforeNodes = new ArrayList<>();
        Node startNode = createNode(14.71592f, 55.09925);
        beforeNodes.add(startNode);
        beforeNodes.add(fromNode);
        Way beforeWay = createWay(beforeNodes, 50, "Before Way", "Way");

        setTreeMaps();
        routeNavigation.setupRoute(createNode(14.71592f, 55.09925), createNode(14.7205f, 55.1014), beforeWay, slowWay, new int[]{0, 1}, new int[]{1, 2}, VehicleType.CAR, true, true);
        routeNavigation.testGetCurrentRoute();

        for (String s : routeNavigation.getDirections()) {
            assertFalse(s.contains("One Way Way"));
        }
    }
}