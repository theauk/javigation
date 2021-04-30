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
import javafx.geometry.Point2D;

import java.util.*;

public class RouteNavigation extends Service<List<Element>> {
    // TODO: 4/10/21 Add restrictions
    // TODO: 4/19/21 Hide fastest for bike/walk in the view.

    private ElementToElementsTreeMap<Node, Way> nodeToWayMap;
    private ElementToElementsTreeMap<Node, Relation> nodeToRestriction;
    private ElementToElementsTreeMap<Way, Relation> wayToRestriction;

    private Node from, to;
    private VehicleType vehicleType;
    private boolean fastest;
    private boolean aStar;

    private List<Node> path;
    private List<String> routeDescription;

    private Map<Node, DistanceAndTimeEntry> unitsTo;
    private Map<Node, Node> nodeBefore;
    private Map<Node, Way> wayBefore;
    private PriorityQueue<Node> pq;

    private boolean needToCheckUTurns;
    private final int maxSpeed;

    public RouteNavigation() {
        this.maxSpeed = 130;
    }

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
                List<Node> path = createRoute();
                List<Element> currentRoute = new ArrayList<>();

                if (path.size() > 0) {
                    Way route = new Way();
                    Node start = path.get(0);
                    Node end = path.get(path.size() - 1);

                    route.setType("navigation");
                    start.setType("start_route_note");
                    end.setType("end_route_note");

                    for (int i = 0; i < path.size() - 1; i++) { //TODO SKIPS LAST INDEX?
                        route.addNode(path.get(i));
                    }

                    currentRoute.add(route);
                    currentRoute.add(start);
                    currentRoute.add(end);
                }

                return currentRoute;
            }
        };
    }

    private void setup() {
        needToCheckUTurns = false;
        routeDescription = new ArrayList<>();
        unitsTo = new HashMap<>();
        nodeBefore = new HashMap<>();
        wayBefore = new HashMap<>();
        pq = new PriorityQueue<>((a, b) -> Integer.compare(unitsTo.get(a).compareTo(unitsTo.get(b)), 0)); // different comparator
        pq.add(from);
        unitsTo.put(from, new DistanceAndTimeEntry(0, 0, 0));
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
            needToCheckUTurns = true; // TODO: 4/19/21 really not the most beautiful thing...
            n = checkNode();

            if (n != to) throw new NoNavigationResultException();
            else {
                path = getTrack(new ArrayList<>(), n);
                return path;
            }

        } else {
            path = getTrack(new ArrayList<>(), n);
            getRouteDescription();
            return path;
        }
    }


    /**
     * Sets up the planned route to use the specified criteria.
     *
     * @param to the to Node.
     * @param vehicleType the selected vehicle type.
     * @param fastest true if fastest route needs to be found. False if shortest route should be found.
     * @param aStar true if the A* algorithm should be used. False if Dijkstra should be used.
     */
    public void setupRoute(Node from, Node to, VehicleType vehicleType, boolean fastest, boolean aStar) {
        this.from = from;
        this.to = to;
        this.vehicleType = vehicleType;
        this.fastest = fastest;
        this.aStar = aStar;
    }

    /**
     * Get the total distance for the path.
     * @return The total distance.
     */
    public double getTotalDistance() {
        if (unitsTo.get(to) != null) return unitsTo.get(to).distance; // TODO: 4/26/21 back to exception instead
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
     * @return A list of the Nodes making up the path.
     */
    private List<Node> getTrack(List<Node> nodes, Node currentNode) {
        if (currentNode != null) {
            nodes.add(currentNode);
            getTrack(nodes, nodeBefore.get(currentNode));
        }
        return nodes;
    }

    /**
     * Relaxes the Nodes adjacent to the current Node.
     * @param currentFrom The current Node to examine.
     */
    private void relax(Node currentFrom) {
        List<Way> waysWithFromNode = nodeToWayMap.getElementsFromNode(currentFrom);

        for (Way w : waysWithFromNode) {
            List<Node> adjacentNodes = new ArrayList<>();

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
                    boolean[] doAdjacentNodesHaveRestrictions = new boolean[adjacentNodes.size()]; // TODO: 4/23/21 delete?
                    if (!isThereARestriction(wayBefore.get(currentFrom), currentFrom, w)) {
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
     * @param w The current Way.
     * @param currentFrom The current from Node.
     */
    private void getPreviousNode(List<Node> adjacentNodes, Way w, Node currentFrom) {
        Node previousNode = w.getPreviousNode(currentFrom);
        if (previousNode != null) adjacentNodes.add(previousNode);
    }

    /**
     * Gets the next Node after a specified Node on a certain Way.
     * @param adjacentNodes The list of adjacent Nodes to the current from Node.
     * @param w The current Way.
     * @param currentFrom The current from Node.
     */
    private void getNextNode(List<Node> adjacentNodes, Way w, Node currentFrom) {
        Node nextNode = w.getNextNode(currentFrom);
        if (nextNode != null) adjacentNodes.add(nextNode);
    }

    /**
     * Checks if is a _no restriction (e.g. no left-turn).
     * @param fromWay The Way the path is coming from.
     * @param viaNode The Node the path is trying to go via.
     * @param toWay The Way the path is trying to go to.
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
     * @param toWay The Way the path is trying to go to.
     * @return True if there is a restriction. False if not.
     */
    private boolean checkRestrictionViaNode(Way fromWay, Node viaNode, Way toWay) {
        List<Relation> restrictionsViaNode = nodeToRestriction.getElementsFromNode(viaNode);
        if (restrictionsViaNode != null) {
            for (Relation restriction : restrictionsViaNode) {
                if (restriction.getRestriction().contains("no_") && restriction.getFrom() == fromWay && restriction.getViaNode() == viaNode && restriction.getTo() == toWay) { // TODO: 4/19/21 er check med viaNode nødvendigt grundet nodeToorest lookup? same nedenunder for viaWay
                    return true;
                } else if (restriction.getRestriction().contains("only_")) { // TODO: 4/20/21 FIX
                    //System.out.println(fromWay.getName() + " " + viaNode.getId() + " " + toWay.getName());
                }
            }
        }
        return false;
    }

    /**
     * Checks if there is a _no restriction via a specified Way.
     * @param fromWay The Way the path is coming from.
     * @param viaNode The Node the path is trying to go via.
     * @param toWay The Way the path is trying to go to.
     * @return True if there is a restriction. False if not.
     */
    private boolean checkRestrictionViaWay(Way fromWay, Node viaNode, Way toWay) {
        List<Relation> restrictionsViaWay = wayToRestriction.getElementsFromNode(fromWay);
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
                } //else if (restriction.getRestriction().contains("only_")) { // TODO: 4/20/21 ja....

                //}
            }
        }
        return false;
    }

    /**
     * Find the cost between two Nodes to update the priority queue.
     * @param currentFrom The from Node.
     * @param currentTo The to Node.
     * @param w The Way between the two Nodes.
     */
    private void checkDistanceAStar(Node currentFrom, Node currentTo, Way w) {
        double currentCost = unitsTo.get(currentTo) == null ? Double.POSITIVE_INFINITY : unitsTo.get(currentTo).cost;

        double distanceBetweenFromTo = getDistanceBetweenTwoNodes(currentFrom, currentTo);
        double timeBetweenFromTo = getTravelTime(distanceBetweenFromTo, w);

        if (fastest) {
            double unitsToCurrentTo = unitsTo.get(currentFrom).time + timeBetweenFromTo;
            double unitsCurrentToToFinalTo = getDistanceBetweenTwoNodes(currentTo, to) / maxSpeed;
            double newCost = unitsToCurrentTo + unitsCurrentToToFinalTo;
            if (newCost < currentCost) {
                updateMapsAndPQ(currentTo, currentFrom, w, distanceBetweenFromTo, timeBetweenFromTo, newCost);
            }
        } else {
            double unitsToCurrentTo = unitsTo.get(currentFrom).distance + distanceBetweenFromTo;
            double unitsCurrentToToFinalTo = getDistanceBetweenTwoNodes(currentTo, to);
            double newCost = unitsToCurrentTo + unitsCurrentToToFinalTo;
            if (newCost < currentCost) {
                updateMapsAndPQ(currentTo, currentFrom, w, distanceBetweenFromTo, timeBetweenFromTo, newCost);
            }
        }
    }

    /**
     * Find the units between two Nodes to update the priority queue.
     * @param currentFrom The from Node.
     * @param currentTo The to Node.
     * @param w The Way between the Nodes.
     */
    private void checkDistanceDijkstra(Node currentFrom, Node currentTo, Way w) {
        double currentDistanceTo = unitsTo.get(currentTo) == null ? Double.POSITIVE_INFINITY : unitsTo.get(currentTo).distance;
        double currentTimeTo = unitsTo.get(currentTo) == null ? Double.POSITIVE_INFINITY : unitsTo.get(currentTo).time;

        double distanceBetweenFromTo = getDistanceBetweenTwoNodes(currentFrom, currentTo);
        double timeBetweenFromTo = getTravelTime(distanceBetweenFromTo, w);

        if (fastest) {
            double newCost = unitsTo.get(currentFrom).time + timeBetweenFromTo;
            if (newCost < currentTimeTo) {
                updateMapsAndPQ(currentTo, currentFrom, w, distanceBetweenFromTo, timeBetweenFromTo, newCost); // TODO: 4/23/21 better way to do the last variable?
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
     * @param timeBetweenFromTo The travelling time between the two Nodes.
     * @param newCost The cost between the two Nodes.
     */
    private void updateMapsAndPQ(Node currentTo, Node currentFrom, Way w, double distanceBetweenFromTo, double timeBetweenFromTo, double newCost) {
        nodeBefore.put(currentTo, currentFrom);
        wayBefore.put(currentTo, w);
        if (unitsTo.containsKey(currentTo))
            pq.remove(currentTo); //TODO: 4/23/21 før var check + tilføj til pq O(1) fordi det var HM. NU: check er O(1) mens remove og add er log
        unitsTo.put(currentTo, new DistanceAndTimeEntry(unitsTo.get(currentFrom).distance + distanceBetweenFromTo, unitsTo.get(currentFrom).time + timeBetweenFromTo, newCost));
        pq.add(currentTo);
    }

    private double getDistanceBetweenTwoNodes(Node from, Node to) {
        //Adapted from https://www.movable-type.co.uk/scripts/latlong.html
        //Calculations need y to be before x in a point.
        double earthRadius = 6371e3; //in meters

        double lat1 = convertToGeo(from.getyMax());
        double lat2 = convertToGeo(to.getyMax());
        double lon1 = from.getxMax();
        double lon2 = to.getxMax();

        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    private double convertToGeo(double value) {
        return -value * 0.56f;
    }

    /**
     * Gets the travel time for a given distance and speed.
     * @param distance The distance to be used for the calculation.
     * @param w The Way used to find the speed if travelling by car.
     * @return The travelling time.
     */
    private double getTravelTime(double distance, Way w) {
        double speed;

        if(vehicleType == VehicleType.CAR) speed = w.getMaxSpeed();
        else speed = vehicleType.speed();
        return distance / (speed * (5f / 18f));
    }

    public void dumpPath() {
        for(int j = 0; j < path.size(); j++) {
            System.out.println("(" + path.get(j).getxMax() + ", " + convertToGeo(path.get(j).getyMax()) + ")");
        }
    }

    private String lastDirection;
    private String currentDirection;
    private double turnAngleThreshold = 5.0;

    private String getDirection(Point2D from, Point2D via, Point2D to) {
        double angle = MapMath.turnAngle(from, via, to);

        //System.out.println("Angle: " + angle);

        //LEFT NEGATIVE
        //RIGHT POSITIVE
        if(angle > turnAngleThreshold) return "RIGHT";
        if(angle < -turnAngleThreshold) return "LEFT";
        else return "STRAIGHT";
    }

    public void calculateResult(Point2D from, Point2D via, Point2D to) {
        //System.out.println("Calculate Result");

        //System.out.println("F = (" + from.getX() + ", " + from.getY() + ")");
        //System.out.println("V = (" + via.getX() + ", " + via.getY() + ")");
        //System.out.println("T = (" + to.getX() + ", " + to.getY() + ")");

        currentDirection = getDirection(from, via, to);

        if(!currentDirection.equals(lastDirection)) {
            //if(lastDirection == null) System.out.println("Go " + MapMath.compassDirection(from, via));   //START POINT
        }

        if(currentDirection.equals("LEFT")) {
            //System.out.println("Go LEFT");
        }
        else if(currentDirection.equals("RIGHT")) {
            //System.out.println("Go RIGHT");
        }
        else if(currentDirection.equals("STRAIGHT")) {
            //System.out.println("Go STRAIGHT");
        }

        lastDirection = currentDirection;
    }

    public void getRouteDescription() {
        //System.err.println("Size: " + path.size());
        //if(path.size() % 3 != 0) System.err.println("Warning can't do 3 each time!");
        lastDirection = null;

        for (int i = path.size() - 1; i >= 0; i--) {
            if(i - 2 < 0) {
                //System.err.println("SKIPPING 2");
                break;
            }

            Node f = path.get(i);
            Node v = path.get(i - 1);
            Node t = path.get(i - 2);

            Point2D from = MapMath.convertToGeoCoords(new Point2D(f.getxMax(), f.getyMax()));
            Point2D via = MapMath.convertToGeoCoords(new Point2D(v.getxMax(), v.getyMax()));
            Point2D to = MapMath.convertToGeoCoords(new Point2D(t.getxMax(), t.getyMax()));

            calculateResult(from, via, to);

            //System.out.println();
        }
    }

    public void setNodeToWayMap(ElementToElementsTreeMap<Node, Way> nodeToWayMap) {
        this.nodeToWayMap = nodeToWayMap;
    }

    public void setNodeToRestriction(ElementToElementsTreeMap<Node, Relation> nodeToRestriction) {
        this.nodeToRestriction = nodeToRestriction;
    }

    public void setWayToRestriction(ElementToElementsTreeMap<Way, Relation> wayToRestriction) {
        this.wayToRestriction = wayToRestriction;
    }

    /**
     * Class which holds the distance and a time to a certain node along with the cost for A-star.
     * The class is necessary to keep track of both variables as time various by the road type for cars.
     */
    private class DistanceAndTimeEntry implements Comparable<DistanceAndTimeEntry> {
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