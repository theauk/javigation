package bfst21.data_structures;

import bfst21.Exceptions.NoNavigationResultException;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class RouteNavigation implements Serializable {
    @Serial
    private static final long serialVersionUID = -488598808136557757L;
    // TODO: 4/10/21 Add restrictions
    // TODO: 4/19/21 Hide fastest for bike/walk in the view.

    private Node to;
    private ElementToElementsTreeMap<Node, Way> nodeToWayMap;
    private ElementToElementsTreeMap<Node, Relation> nodeToRestriction;
    private ElementToElementsTreeMap<Way, Relation> wayToRestriction;
    private ArrayList<Node> path;
    private ArrayList<String> routeDescription;
    private HashMap<Node, DistanceAndTimeEntry> unitsTo;
    private HashMap<Node, Node> nodeBefore;
    private HashMap<Node, Way> wayBefore;
    private PriorityQueue<Node> pq;
    private boolean car;
    private boolean bike;
    private boolean walk;
    private boolean fastest;
    private boolean needToCheckUTurns;
    private boolean aStar;
    private double bikingSpeed;
    private double walkingSpeed;
    private int maxSpeed;

    public RouteNavigation(ElementToElementsTreeMap<Node, Way> nodeToWayMap, ElementToElementsTreeMap<Node, Relation> nodeToRestriction, ElementToElementsTreeMap<Way, Relation> wayToRestriction) {
        this.nodeToRestriction = nodeToRestriction;
        this.wayToRestriction = wayToRestriction;
        this.nodeToWayMap = nodeToWayMap;
        this.maxSpeed = 130;
    }

    private void setup(Node from, Node to, boolean car, boolean bike, boolean walk, boolean fastest, boolean aStar) {
        this.to = to;
        this.car = car;
        this.bike = bike;
        this.walk = walk;
        this.fastest = fastest;
        this.aStar = aStar;
        needToCheckUTurns = false;
        routeDescription = new ArrayList<>();
        unitsTo = new HashMap<>();
        nodeBefore = new HashMap<>();
        wayBefore = new HashMap<>();
        pq = new PriorityQueue<>((a, b) -> Integer.compare(unitsTo.get(a).compareTo(unitsTo.get(b)), 0)); // different comparator
        bikingSpeed = 16; // from Google Maps 16 km/h
        walkingSpeed = 5; // from Google Maps 5 km/h
        pq.add(from);
        unitsTo.put(from, new DistanceAndTimeEntry(0, 0, 0));
    }

    /**
     * Gets either the fastest or the shortest path between two Nodes.
     * @param from The from Node.
     * @param to The to Node.
     * @param car True if travelling by car. Otherwise, false.
     * @param bike True if travelling by bike. Otherwise, false.
     * @param walk True if walking. Otherwise, false.
     * @param fastest True if fastest route needs to be found. False if shortest route should be found.
     * @param aStar True if the A* algorithm should be used. False if Dijkstra should be used.
     * @return An ArrayList with Nodes that make up the path in reverse order.
     * @throws NoNavigationResultException If no route can be found.
     */
    public ArrayList<Node> getPath(Node from, Node to, boolean car, boolean bike, boolean walk, boolean fastest, boolean aStar) throws NoNavigationResultException {
        setup(from, to, car, bike, walk, fastest, aStar);
        Node n = checkNode();

        if (n != to) {
            setup(from, to, car, bike, walk, fastest, aStar);
            needToCheckUTurns = true; // TODO: 4/19/21 really not the most beautiful thing...
            n = checkNode();

            if (n != to) throw new NoNavigationResultException();
            else {
                path = getTrack(new ArrayList<>(), n);
                return path;
            }

        } else {
            path = getTrack(new ArrayList<>(), n);
            //getRouteDescription();
            return path;
        }
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

    /*public String getRouteDescription() {
        for (int i = path.size() - 1; i >= 2; i--) {
            Node from = path.get(i);
            Node via = path.get(i - 1);
            Node to = path.get(i - 2);

            double distanceFromAndVia = getDistanceBetweenTwoNodes(from, via);
            double distanceViaAndTo = getDistanceBetweenTwoNodes(via, to);
            double distanceFromAndTo = getDistanceBetweenTwoNodes(from, to);

            double cosTurnAngle = (Math.pow(distanceViaAndTo, 2) + Math.pow(distanceFromAndVia, 2) - Math.pow(distanceFromAndTo, 2)) / (2 * distanceViaAndTo * distanceFromAndVia);
            double turnAngle = Math.acos(cosTurnAngle);

            double result = Math.atan2(to.getyMax() - via.getyMax(), to.getxMax() - via.getxMax()) - Math.atan2(from.getyMax() - via.getyMax(), from.getxMax() - via.getxMax());


            double v1x = from.getxMax() - via.getxMax();
            double v1y = from.getyMax() - via.getyMax();
            double v2x = to.getxMax() - via.getxMax();
            double v2y = to.getyMax() - via.getyMax();

            double angle = Math.atan2(v1x, v1y) - Math.atan2(v2x, v2y);
            double degreeAngle = Math.toDegrees(angle);

            //System.out.println(turnAngle * (180f / Math.PI));

            if (degreeAngle > 0) {
                if (degreeAngle < 175 || degreeAngle > 185) {
                    System.out.println("You turned right, by: " + degreeAngle + " " + Math.toDegrees(result));
                }
            } else {
                if (degreeAngle > -175 || degreeAngle < -185) {
                    System.out.println("You turned left, by: " + degreeAngle + " " + Math.toDegrees(result));
                }
            }
        }
        System.out.println("");
        return "";
    }*/

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
    private ArrayList<Node> getTrack(ArrayList<Node> nodes, Node currentNode) {
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
        ArrayList<Way> waysWithFromNode = nodeToWayMap.getElementsFromNode(currentFrom);

        for (Way w : waysWithFromNode) {
            ArrayList<Node> adjacentNodes = new ArrayList<>();

            if (car) {
                if (w.isDriveable()) {
                    if (!w.isOnewayRoad()) {
                        getPreviousNode(adjacentNodes, w, currentFrom);
                    }
                    getNextNode(adjacentNodes, w, currentFrom);
                }
            } else if (bike) {
                if (w.isCycleable()) {
                    if (!w.isOneWayForBikes()) {
                        getPreviousNode(adjacentNodes, w, currentFrom);
                    }
                    getNextNode(adjacentNodes, w, currentFrom);
                }
            } else {
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
    private void getPreviousNode(ArrayList<Node> adjacentNodes, Way w, Node currentFrom) {
        Node previousNode = w.getPreviousNode(currentFrom);
        if (previousNode != null) adjacentNodes.add(previousNode);
    }

    /**
     * Gets the next Node after a specified Node on a certain Way.
     * @param adjacentNodes The list of adjacent Nodes to the current from Node.
     * @param w The current Way.
     * @param currentFrom The current from Node.
     */
    private void getNextNode(ArrayList<Node> adjacentNodes, Way w, Node currentFrom) {
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
        ArrayList<Relation> restrictionsViaNode = nodeToRestriction.getElementsFromNode(viaNode);
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
        ArrayList<Relation> restrictionsViaWay = wayToRestriction.getElementsFromNode(fromWay);
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
        if (bike) speed = bikingSpeed;
        else if (walk) speed = walkingSpeed;
        else speed = w.getMaxSpeed();
        return distance / (speed * (5f / 18f));
    }

    /**
     * Class which holds the distance and a time to a certain node along with the cost for A-star.
     * The class is necessary to keep track of both variables as time various by the road type for cars.
     */
    private class DistanceAndTimeEntry implements Comparable<DistanceAndTimeEntry> {
        private double distance, time, cost;

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