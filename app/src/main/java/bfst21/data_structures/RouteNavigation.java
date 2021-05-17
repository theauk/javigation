package bfst21.data_structures;

import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;
import bfst21.exceptions.NoNavigationResultException;
import bfst21.utils.MapMath;
import bfst21.utils.VehicleType;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.*;

/**
 * The RouteNavigation class handles route navigation between two points (Dijkstra or A*).
 */
public class RouteNavigation extends Service<List<Element>> {

    private ElementToElementsTreeMap<Node, Way> nodeToHighwayMap;
    private ElementToElementsTreeMap<Node, Relation> nodeToRestriction;
    private ElementToElementsTreeMap<Way, Relation> wayToRestriction;

    private Node from, to;
    private Way fromWay, toWay;
    private int[] nearestFromWaySegmentIndices;
    private int[] nearestToWaySegmentIndices;
    private VehicleType vehicleType;
    private boolean fastest, aStar;

    private List<Node> path;

    private Map<Node, DistanceAndTimeEntry> unitsTo;
    private Map<Node, Node> nodeBefore;
    private Map<Node, Way> wayBefore;
    private PriorityQueue<Node> pq;

    private boolean needToCheckUTurns;
    private final int maxSpeed;

    private double currentDistanceDescription;
    private double currentTimeDescription;
    private ArrayList<String> routeDescription;
    private HashSet<String> specialPathFeatures;
    private float[] coordinatesForPanToRoute;

    public RouteNavigation() {
        this.maxSpeed = 130;
    }

    /**
     * Starts the separate routing thread.
     */
    public void startRouting() {
        if(!isRunning()) {
            reset();
            start();
        }
    }

    @Override
    protected Task<List<Element>> createTask() {
        return new Task<>() {
            @Override
            protected List<Element> call() throws Exception {
                updateMessage("Calculating the best route...");
                return getCurrentRoute();
            }
        };
    }

    /**
     * Gets the route between two points.
     * @return A list of nodes which make up the route.
     * @throws NoNavigationResultException If no route can be found.
     */
    private List<Element> getCurrentRoute() throws NoNavigationResultException {
        List<Node> path = createRoute();
        List<Element> currentRoute = new ArrayList<>();

        if (path.size() > 0) {
            Way route = new Way();
            Node start = path.get(0);
            Node end = path.get(path.size() - 1);

            route.setType("navigation");
            start.setType("start_route_note");
            end.setType("end_route_note");

            for (Node node : path) {
                route.addNode(node);
            }

            currentRoute.add(route);
            currentRoute.add(start);
            currentRoute.add(end);
        }

        removeFromToNodesFromTheirWays();
        return currentRoute;
    }

    /**
     * Used for testing the class.
     * @throws NoNavigationResultException If no route can be found.
     */
    public void testGetCurrentRoute() throws NoNavigationResultException {
        getCurrentRoute();
    }

    /**
     * Prepares the necessary fields for the route navigation.
     */
    private void setup() {
        needToCheckUTurns = false;
        routeDescription = new ArrayList<>();
        unitsTo = new HashMap<>();
        nodeBefore = new HashMap<>();
        wayBefore = new HashMap<>();
        pq = new PriorityQueue<>((a, b) -> Integer.compare(unitsTo.get(a).compareTo(unitsTo.get(b)), 0)); // compares based on units to
        pq.add(from);
        unitsTo.put(from, new DistanceAndTimeEntry(0, 0, 0));
        routeDescription = new ArrayList<>();
        specialPathFeatures = new HashSet<>();
        coordinatesForPanToRoute = new float[]{Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
    }

    /**
     * Sets the tree map which holds information about which nodes are on which ways.
     * @param nodeToHighwayMap The map holding the node to way information.
     */
    public void setNodeToHighwayMap(ElementToElementsTreeMap<Node, Way> nodeToHighwayMap) {
        this.nodeToHighwayMap = nodeToHighwayMap;
    }

    /**
     * Sets the tree map which holds information about which nodes are a part of a restriction.
     * @param nodeToRestriction The map holding the node to restriction information.
     */
    public void setNodeToRestriction(ElementToElementsTreeMap<Node, Relation> nodeToRestriction) {
        this.nodeToRestriction = nodeToRestriction;
    }

    /**
     * Sets the tree map which holds information about which ways are a part of a restriction.
     * @param wayToRestriction The map holding the way to restriction information.
     */
    public void setWayToRestriction(ElementToElementsTreeMap<Way, Relation> wayToRestriction) {
        this.wayToRestriction = wayToRestriction;
    }

    /**
     * Creates and returns either the fastest or the shortest path between two Nodes.
     *
     * @return a list with Nodes that make up the path in reverse order.
     * @throws NoNavigationResultException If no route can be found.
     */
    private List<Node> createRoute() throws NoNavigationResultException {
        setup();
        Node n = checkNode();

        if (n != to) {
            setup();
            // Run the algorithm again and check for u-turns
            needToCheckUTurns = true;
            n = checkNode();
            if (n != to) throw new NoNavigationResultException();
        }
        path = getTrack(new ArrayList<>(), n);

        getRouteDescription();
        return path;
    }

    /**
     * Sets up the planned route to use the specified criteria.
     * @param from The from Node.
     * @param to The to Node.
     * @param fromWay The from Way.
     * @param toWay The to Way.
     * @param nearestFromWaySegmentIndices The indices in the from way where the to Node should be.
     * @param nearestToWaySegmentIndices The indices in the to way where the from Node should be.
     * @param vehicleType The selected vehicle type.
     * @param fastest True if fastest route needs to be found. False if shortest route should be found.
     * @param aStar True if the A* algorithm should be used. False if Dijkstra should be used.
     */
    public void setupRoute(Node from, Node to, Way fromWay, Way toWay, int[] nearestFromWaySegmentIndices, int[] nearestToWaySegmentIndices, VehicleType vehicleType, boolean fastest, boolean aStar) {
        this.from = from;
        this.to = to;
        this.fromWay = fromWay;
        this.toWay = toWay;
        this.nearestFromWaySegmentIndices = nearestFromWaySegmentIndices;
        this.nearestToWaySegmentIndices = nearestToWaySegmentIndices;
        this.vehicleType = vehicleType;
        this.fastest = fastest;
        this.aStar = aStar;
        addFromToNodesToFromToWays();
    }

    /**
     * Adds the to and from node to their respective ways so that it is possible to navigate from/to them.
     */
    private void addFromToNodesToFromToWays() {
        MapMath.updateNodeCoordinatesIfEndOfWay(fromWay, from);
        fromWay.addNodeBetweenIndices(from, nearestFromWaySegmentIndices[1]);
        MapMath.updateNodeCoordinatesIfEndOfWay(toWay, to);
        toWay.addNodeBetweenIndices(to, nearestToWaySegmentIndices[1]);
    }

    /**
     * Removes the to and from nodes from their respective ways.
     */
    private void removeFromToNodesFromTheirWays() {
        if (nearestFromWaySegmentIndices[1] > nearestToWaySegmentIndices[1]) {
            fromWay.removeNodeFromIndex(nearestFromWaySegmentIndices[1]);
            toWay.removeNodeFromIndex(nearestToWaySegmentIndices[1]);
        } else {
            toWay.removeNodeFromIndex(nearestToWaySegmentIndices[1]);
            fromWay.removeNodeFromIndex(nearestFromWaySegmentIndices[1]);
        }
    }

    /**
     * Gets the total distance for the path.
     * @return The total distance.
     */
    public double getTotalDistance() {
        if (unitsTo.get(to) != null) return unitsTo.get(to).distance;
        else return 0;
    }

    /**
     * Gets the total travelling time for the path.
     * @return The total travelling time.
     */
    public double getTotalTime() {
        if (unitsTo.get(to) != null) return unitsTo.get(to).time;
        else return 0;
    }

    /**
     * Gets a list of the directions to navigate the computed route.
     * @return An ArrayList with the directions where each entry is a navigation step.
     */
    public ArrayList<String> getDirections() {
        return routeDescription;
    }

    /**
     * Gets a list of special features on a path such as if it is necessary to take a ferry.
     * @return A list of special path features.
     */
    public HashSet<String> getSpecialPathFeatures() {
        return specialPathFeatures;
    }

    /**
     * Checks the next node in the priority queue with the smallest units/cost.
     * @return The final Node found for the path.
     */
    private Node checkNode() {
        Node n = null;
        while (!pq.isEmpty()) {
            n = pq.poll();
            if (n != to) relax(n);
            else break;
        }
        return n;
    }

    /**
     * Gets a list of the Nodes that make up the path.
     * @param nodes A list of the Nodes making up the path.
     * @param currentNode The Node which should be checked for the Node before it.
     * @return A list of the Nodes making up the path in reverse order (to first and from at the end).
     */
    private List<Node> getTrack(List<Node> nodes, Node currentNode) {
        if (currentNode != null) {
            nodes.add(currentNode);
            updateCoordinatesForPanToRoute(currentNode);
            getTrack(nodes, nodeBefore.get(currentNode));
        }
        return nodes;
    }

    /**
     * Update the coordinates that represents the bounding box that can hold the route.
     * @param currentNode The current node to check.
     */
    private void updateCoordinatesForPanToRoute(Node currentNode) {
        if (currentNode.getxMax() < coordinatesForPanToRoute[0]) coordinatesForPanToRoute[0] = currentNode.getxMax(); // x_min
        if (currentNode.getxMax() > coordinatesForPanToRoute[1]) coordinatesForPanToRoute[1] = currentNode.getxMax(); // x_max
        if (currentNode.getyMax() < coordinatesForPanToRoute[2]) coordinatesForPanToRoute[2] = currentNode.getyMax(); // y_max
        if (currentNode.getyMax() > coordinatesForPanToRoute[3]) coordinatesForPanToRoute[3] = currentNode.getyMax(); // y_max
    }

    /**
     * Gets the coordinates that represents the smallest bounding box that contains the route.
     * @return A float array with x-min, x-max, y-min, and y-max.
     */
    public float[] getCoordinatesForPanToRoute() {
        return coordinatesForPanToRoute;
    }

    /**
     * Relaxes the Nodes adjacent to the current Node.
     * @param currentFrom The current Node to examine.
     */
    private void relax(Node currentFrom) {
        ArrayList<Way> waysWithFromNode = new ArrayList<>();
        if (nodeToHighwayMap.getElementsFromKeyElement(currentFrom) != null) {
            waysWithFromNode = nodeToHighwayMap.getElementsFromKeyElement(currentFrom);
        } else if (currentFrom == from) {
            waysWithFromNode.add(fromWay);
        }

        for (Way w : waysWithFromNode) {
            ArrayList<Node> adjacentNodes = new ArrayList<>();

            if (vehicleType == VehicleType.CAR) {
                if (w.isDriveable()) {
                    if (!w.isOnewayRoad()) {
                        getPreviousNode(adjacentNodes, w, currentFrom);
                    }
                    getNextNode(adjacentNodes, w, currentFrom);
                }
            } else if (vehicleType == VehicleType.BIKE) {
                if (w.isCycleable()) {
                    if (!w.isOneWayForBikes()) {
                        getPreviousNode(adjacentNodes, w, currentFrom);
                    }
                    getNextNode(adjacentNodes, w, currentFrom);
                }
            } else if (vehicleType == VehicleType.WALK) {
                if (w.isWalkable()) {
                    getPreviousNode(adjacentNodes, w, currentFrom);
                    getNextNode(adjacentNodes, w, currentFrom);
                }
            }
            if (!adjacentNodes.isEmpty()) {
                for (Node n : adjacentNodes) {
                    if (vehicleType == VehicleType.WALK || !isThereARestriction(wayBefore.get(currentFrom), currentFrom, w)) {
                        if (aStar) {
                            checkDistanceAStar(currentFrom, n, w);
                        } else {
                            checkDistanceDijkstra(currentFrom, n, w);
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the Node before a specified Node on a certain Way.
     * @param adjacentNodes The list of adjacent Nodes to the current from Node.
     * @param w             The current Way.
     * @param currentFrom   The current from Node.
     */
    private void getPreviousNode(List<Node> adjacentNodes, Way w, Node currentFrom) {
        Node previousNode = w.getPreviousNode(currentFrom);
        if (previousNode != null) adjacentNodes.add(previousNode);
    }

    /**
     * Gets the next Node after a specified Node on a certain Way.
     * @param adjacentNodes The list of adjacent Nodes to the current from Node.
     * @param w             The current Way.
     * @param currentFrom   The current from Node.
     */
    private void getNextNode(List<Node> adjacentNodes, Way w, Node currentFrom) {
        Node nextNode = w.getNextNode(currentFrom);
        if (nextNode != null) adjacentNodes.add(nextNode);
    }

    /**
     * Checks if is a _no restriction (e.g. no left-turn).
     * @param fromWay The Way the path is coming from.
     * @param viaNode The Node the path is trying to go via.
     * @param toWay   The Way the path is trying to go to.
     * @return True if there is a restriction. False if not.
     */
    private boolean isThereARestriction(Way fromWay, Node viaNode, Way toWay) {
        if (checkRestrictionViaNode(fromWay, viaNode, toWay)) return true;
        if (fromWay != null) return checkRestrictionViaWay(fromWay, viaNode, toWay);
        return false;
    }

    /**
     * Checks if there is a _no restriction via a specified Node.
     * @param fromWay The Way the path is coming from.
     * @param viaNode The Node the path is trying to go via.
     * @param toWay   The Way the path is trying to go to.
     * @return True if there is a restriction. False if not.
     */
    private boolean checkRestrictionViaNode(Way fromWay, Node viaNode, Way toWay) {
        List<Relation> restrictionsViaNode = nodeToRestriction.getElementsFromKeyElement(viaNode);
        if (restrictionsViaNode != null) {
            for (Relation restriction : restrictionsViaNode) {
                if (restriction.getRestriction().contains("no_") && restriction.getFrom() == fromWay && restriction.getViaNode() == viaNode && restriction.getTo() == toWay) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if there is a _no restriction via a specified Way.
     * @param fromWay The Way the path is coming from.
     * @param viaNode The Node the path is trying to go via.
     * @param toWay   The Way the path is trying to go to.
     * @return True if there is a restriction. False if not.
     */
    private boolean checkRestrictionViaWay(Way fromWay, Node viaNode, Way toWay) {
        List<Relation> restrictionsViaWay = wayToRestriction.getElementsFromKeyElement(fromWay);
        if (restrictionsViaWay != null) {
            for (Relation restriction : restrictionsViaWay) {
                if (restriction.getRestriction().contains("no_") && restriction.getViaWay() == fromWay && restriction.getTo() == toWay) {
                    Node beforeNode = nodeBefore.get(viaNode);

                    while (wayBefore.get(beforeNode) == fromWay) { // "walk back" until finding a Node on a different Way
                        beforeNode = nodeBefore.get(beforeNode);
                    }

                    if (wayBefore.get(beforeNode) == restriction.getFrom()) { // find the Way with the Node – then check if that is the restriction's from Way
                        if (needToCheckUTurns)
                            unitsTo.put(viaNode, new DistanceAndTimeEntry(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Find the cost between two Nodes to update the priority queue.
     * @param currentFrom The from Node.
     * @param currentTo   The to Node.
     * @param w           The Way between the two Nodes.
     */
    private void checkDistanceAStar(Node currentFrom, Node currentTo, Way w) {
        double currentCost = unitsTo.get(currentTo) == null ? Double.POSITIVE_INFINITY : unitsTo.get(currentTo).cost;

        double distanceBetweenFromTo = MapMath.distanceBetweenTwoNodes(currentFrom, currentTo);
        double timeBetweenFromTo = getTravelTime(distanceBetweenFromTo, w);

        if (fastest) {
            double unitsToCurrentTo = unitsTo.get(currentFrom).time + timeBetweenFromTo;
            double unitsCurrentToToFinalTo = MapMath.distanceBetweenTwoNodes(currentTo, to) / maxSpeed;
            double newCost = unitsToCurrentTo + unitsCurrentToToFinalTo;
            if (newCost < currentCost) {
                updateMapsAndPQ(currentTo, currentFrom, w, distanceBetweenFromTo, timeBetweenFromTo, newCost);
            }
        } else {
            double unitsToCurrentTo = unitsTo.get(currentFrom).distance + distanceBetweenFromTo;
            double unitsCurrentToToFinalTo = MapMath.distanceBetweenTwoNodes(currentTo, to);
            double newCost = unitsToCurrentTo + unitsCurrentToToFinalTo;
            if (newCost < currentCost) {
                updateMapsAndPQ(currentTo, currentFrom, w, distanceBetweenFromTo, timeBetweenFromTo, newCost);
            }
        }
    }

    /**
     * Find the units between two Nodes to update the priority queue.
     * @param currentFrom The from Node.
     * @param currentTo   The to Node.
     * @param w           The Way between the Nodes.
     */
    private void checkDistanceDijkstra(Node currentFrom, Node currentTo, Way w) {
        double currentDistanceTo = unitsTo.get(currentTo) == null ? Double.POSITIVE_INFINITY : unitsTo.get(currentTo).distance;
        double currentTimeTo = unitsTo.get(currentTo) == null ? Double.POSITIVE_INFINITY : unitsTo.get(currentTo).time;

        double distanceBetweenFromTo = MapMath.distanceBetweenTwoNodes(currentFrom, currentTo);
        double timeBetweenFromTo = getTravelTime(distanceBetweenFromTo, w);

        if (fastest) {
            double newCost = unitsTo.get(currentFrom).time + timeBetweenFromTo;
            if (newCost < currentTimeTo) {
                updateMapsAndPQ(currentTo, currentFrom, w, distanceBetweenFromTo, timeBetweenFromTo, newCost);
            }
        } else {
            double newCost = unitsTo.get(currentFrom).distance + distanceBetweenFromTo;
            if (newCost < currentDistanceTo) {
                updateMapsAndPQ(currentTo, currentFrom, w, distanceBetweenFromTo, timeBetweenFromTo, newCost);
            }
        }
    }

    /**
     * Update the maps with new distance and time along with the unit/cost priority queue.
     * @param currentTo The from Node.
     * @param currentFrom The to Node.
     * @param w The Way between the Nodes.
     * @param distanceBetweenFromTo The distance between the two Nodes.
     * @param timeBetweenFromTo     The travelling time between the two Nodes.
     * @param newCost               The cost between the two Nodes.
     */
    private void updateMapsAndPQ(Node currentTo, Node currentFrom, Way w, double distanceBetweenFromTo, double timeBetweenFromTo, double newCost) {
        nodeBefore.put(currentTo, currentFrom);
        wayBefore.put(currentTo, w);
        if (unitsTo.containsKey(currentTo))
            pq.remove(currentTo);
        unitsTo.put(currentTo, new DistanceAndTimeEntry(unitsTo.get(currentFrom).distance + distanceBetweenFromTo, unitsTo.get(currentFrom).time + timeBetweenFromTo, newCost));
        pq.add(currentTo);
    }

    /**
     * Gets the travel time for a given distance and speed.
     * @param distance The distance to be used for the calculation.
     * @param w        The Way used to find the speed if travelling by car.
     * @return The travelling time.
     */
    private double getTravelTime(double distance, Way w) {
        double speed;

        if(vehicleType == VehicleType.CAR) speed = w.getMaxSpeed();
        else speed = vehicleType.speed();
        return distance / (speed * (5f / 18f));
    }

    /**
     * Gets the route description.
     */
    private void getRouteDescription() {
        currentDistanceDescription = unitsTo.get(path.get(path.size() - 2)).distance - unitsTo.get(path.get(path.size() - 1)).distance;
        currentTimeDescription = unitsTo.get(path.get(path.size() - 2)).time - unitsTo.get(path.get(path.size() - 1)).time;

        if (path.size() >= 3) getRouteDescriptionMoreThanTwoNodes();
        else getRouteDescriptionLessThanThreeNodes();
    }

    /**
     * Gets the route description for a path that has less than three nodes.
     */
    private void getRouteDescriptionLessThanThreeNodes() {
        Node f = path.get(path.size() - 1);
        Node t = path.get(path.size() - 2);
        if (fromWay.getType().equals("ferry")) {
            routeDescription.add(getFerryText(fromWay.getName()));
            specialPathFeatures.add("a ferry");
        } else {
            routeDescription.add("Head " + MapMath.compassDirection(f, t).toLowerCase() + " on " + wayBefore.get(t).getName() + " and you will arrive at your destination" + getCurrentDistanceText());
        }
    }

    /**
     * Gets the route description for a path that has more than two nodes.
     */
    private void getRouteDescriptionMoreThanTwoNodes() {
        boolean roundabout = false;
        boolean keepRight = false;
        boolean ferry = false;
        int roundAboutStartNodeIndex = 0;

        for (int i = path.size() - 1; i >= 2; i--) {
            Node f = path.get(i);
            Node v = path.get(i - 1);
            Node t = path.get(i - 2);
            Way wayBeforeVia = wayBefore.get(v);
            Way wayBeforeTo = wayBefore.get(t);
            String wayBeforeViaName = wayBeforeVia.getName() != null ? wayBeforeVia.getName() : "unnamed road";
            String wayBeforeToName = wayBeforeTo.getName() != null ? wayBeforeTo.getName() : "unnamed road";
            String wayBeforeViaType = wayBeforeVia.getType();
            String wayBeforeToType = wayBeforeTo.getType();

            if (!wayBeforeViaName.equals(wayBeforeToName) || !wayBeforeViaType.equals(wayBeforeToType)) {
                if (roundabout) {
                    routeDescription.add(getRoundaboutText(roundAboutStartNodeIndex, i, wayBeforeVia, wayBeforeToName));
                    roundabout = false;
                } else if (ferry) {
                    routeDescription.add(getFerryText(wayBeforeViaName));
                    ferry = false;
                } else {
                    if (keepRight) {
                        routeDescription.add(getKeepRightText(wayBeforeViaName));
                        keepRight = false;
                    } else {
                        routeDescription.add("Follow " + wayBeforeViaName + getCurrentDistanceText());
                    }

                    currentDistanceDescription = unitsTo.get(t).distance - unitsTo.get(v).distance;
                    currentTimeDescription = unitsTo.get(t).time - unitsTo.get(v).time;

                    String directionBetweenViaAndToWay = getDirection(MapMath.turnAngle(f, v, t), wayBeforeTo, wayBeforeToName);
                    switch (directionBetweenViaAndToWay) {
                        case "ROUNDABOUT" -> {
                            roundabout = true;
                            roundAboutStartNodeIndex = i - 1;
                        }
                        case "KEEP_RIGHT" -> keepRight = true;
                        case "FERRY" -> {
                            ferry = true;
                            specialPathFeatures.add("a ferry");
                        }
                        default -> routeDescription.add(directionBetweenViaAndToWay);
                    }
                }
            } else {
                currentDistanceDescription += unitsTo.get(t).distance - unitsTo.get(v).distance;
                currentTimeDescription += unitsTo.get(t).time - unitsTo.get(v).time;
            }
        }

        routeDescription.add(getArrivedAtDestinationText(roundabout, ferry));
        fixFirstDirection();
    }

    /**
     * Gets the current distance as a string.
     * @return The current distance on a new line.
     */
    private String getCurrentDistanceText() {
        return "\n" + MapMath.formatDistance(currentDistanceDescription, 2);
    }

    /**
     * Gets the direction for a ferry.
     * @param wayBeforeViaName The name of the way before the via Node.
     * @return The ferry direction string.
     */
    private String getFerryText(String wayBeforeViaName) {
        return "Take the " + wayBeforeViaName + " ferry " + getCurrentDistanceText();
    }

    /**
     * Gets the direction text for a roundabout, including the exit number.
     * @param roundAboutStartNodeIndex The index on the path where it enters the roundabout.
     * @param i The current index.
     * @param wayBeforeVia The way before the via Node.
     * @param wayBeforeToName The name of the way before the to Node.
     * @return The direction string for the roundabout.
     */
    private String getRoundaboutText(int roundAboutStartNodeIndex, int i, Way wayBeforeVia, String wayBeforeToName) {
        return "At the roundabout, take the " + getRoundaboutExit(roundAboutStartNodeIndex, i - 1, wayBeforeVia) + ". exit onto " + wayBeforeToName + getCurrentDistanceText();
    }

    /**
     * Gets the text for keep right directions.
     * @param wayBeforeViaName The name of the way which should be kept right on.
     * @return The keep right direction.
     */
    private String getKeepRightText(String wayBeforeViaName) {
        String keepRightName = "";
        if (wayBeforeViaName.contains("Exit")) keepRightName = " and take " + wayBeforeViaName;
        else if (!wayBeforeViaName.equals("unnamed way")) keepRightName = " on " + wayBeforeViaName;
        return "Keep right" + keepRightName + getCurrentDistanceText();
    }

    /**
     * Gets the final text for the direction, which informs the user that they have arrived at their destination.
     * @param roundabout If the route ends in a roundabout.
     * @param ferry If the route ends on a ferry.
     * @return The final direction.
     */
    private String getArrivedAtDestinationText(boolean roundabout, boolean ferry) {
        String text;
        String wayName = wayBefore.get(path.get(0)).getName();
        if (wayName == null) wayName = "unnamed way";

        if (roundabout) text = "Follow the roundabout";
        else if (ferry) text = "Take the " + wayName + " ferry";
        else text = "Follow " + wayName;

        text += " and you will arrive at your destination" + getCurrentDistanceText();
        return text;
    }

    /**
     * Gets the direction between two ways depending on the angle between them and the type of the current to way.
     * @param angle The angle between two ways.
     * @param wayBeforeTo The way before the current to Node.
     * @param wayBeforeToName The name of the way before the current to Node.
     * @return The direction between two ways.
     */
    private String getDirection(double angle, Way wayBeforeTo, String wayBeforeToName) {
        String type = wayBeforeTo.getType();

        if (type.equals("ferry")) {
            return "FERRY";
        } else if (type.equals("roundabout")) {
            return "ROUNDABOUT";
        } else if (angle > 0) {
            if (type.equals("primary_link") || type.equals("motorway_link")) return "KEEP_RIGHT";
            else return "Turn right onto " + wayBeforeToName;
        } else if (angle < 0) {
            return "Turn left onto " + wayBeforeToName;
        } else {
            return "Continue on " + wayBeforeToName;
        }
    }

    /**
     * Gets the number corresponding to the roundabout exit the path goes along.
     * @param roundaboutStartNodeIndex The index of the Node in the path where it enters the roundabout.
     * @param roundaboutEndIndex The index of the Node in the path where it leaves the roundabout.
     * @param roundaboutWay The Way object for the roundabout.
     * @return The roundabout exit number,
     */
    private String getRoundaboutExit(int roundaboutStartNodeIndex, int roundaboutEndIndex, Way roundaboutWay) {
        int exits = 0;
        for (int i = roundaboutStartNodeIndex - 1; i >= roundaboutEndIndex; i--) {
            ArrayList<Way> ways = nodeToHighwayMap.getElementsFromKeyElement(path.get(i));
            if (ways.size() > 1) {
                for (Way w : ways) {
                    if (w != roundaboutWay) {
                        if (!w.isOnewayRoad() && w.isDriveable()) exits++;
                        else if (w.getNextNode(path.get(i)) != null) exits++; // to ensure that we do not count one-way roads
                    }
                }
            }
        }
        return String.valueOf(exits);
    }

    /**
     * Changes the first direction from follow to head in a certain compass direction. This needs to be done at the end
     * because the path might have several segments from the same road in the beginning and we need to get the full
     * distance for all of the segments.
     */
    private void fixFirstDirection() {
        Node f = path.get(path.size() - 1);
        Node t = path.get(path.size() - 2);
        routeDescription.add(1, routeDescription.get(0).replace("Follow", "Head " + MapMath.compassDirection(f, t).toLowerCase() + " on"));
        routeDescription.remove(0);
    }

    /**
     * Class which holds the distance and a time to a certain node along with the cost for A-star.
     * The class is necessary to keep track of both variables as time various by the road type for cars.
     */
    private static class DistanceAndTimeEntry implements Comparable<DistanceAndTimeEntry> {
        private final double distance;
        private final double time;
        private final double cost;

        public DistanceAndTimeEntry(double distance, double time, double cost) {
            this.distance = distance;
            this.time = time;
            this.cost = cost;
        }

        @Override
        public int compareTo(DistanceAndTimeEntry o) {
            return Double.compare(cost, o.cost);
        }
    }
}