package bfst21.data_structures;

import bfst21.Exceptions.NoNavigationResultException;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DijkstraSP implements Serializable {
    @Serial
    private static final long serialVersionUID = -488598808136557757L;
    // TODO: 4/10/21 Add restrictions 
    // TODO: 4/10/21 Improve remove min
    // TODO: 4/19/21 Hide fastest for bike/walk in the view.

    private Node to;
    private ElementToElementsTreeMap<Node, Way> nodeToWayMap;
    private ElementToElementsTreeMap<Node, Relation> nodeToRestriction;
    private ElementToElementsTreeMap<Way, Relation> wayToRestriction;
    private ArrayList<Node> path;
    private ArrayList<String> routeDescription;
    private HashMap<Long, DistanceAndTimeEntry> unitsTo;
    private HashMap<Long, Node> nodeBefore;
    private HashMap<Long, Way> wayBefore;
    private HashMap<Node, Double> pq;
    private boolean car;
    private boolean bike;
    private boolean walk;
    private boolean fastest;
    private boolean tryAgain;
    private double bikingSpeed;
    private double walkingSpeed;

    public DijkstraSP(ElementToElementsTreeMap<Node, Way> nodeToWayMap, ElementToElementsTreeMap<Node, Relation> nodeToRestriction, ElementToElementsTreeMap<Way, Relation> wayToRestriction) {
        this.nodeToRestriction = nodeToRestriction;
        this.wayToRestriction = wayToRestriction;
        this.nodeToWayMap = nodeToWayMap;
    }

    private void setup(Node from, Node to, boolean car, boolean bike, boolean walk, boolean fastest) {
        this.to = to;
        this.car = car;
        this.bike = bike;
        this.walk = walk;
        this.fastest = fastest;
        tryAgain = false;
        routeDescription = new ArrayList<>();
        unitsTo = new HashMap<>();
        nodeBefore = new HashMap<>();
        wayBefore = new HashMap<>();
        pq = new HashMap<>();
        bikingSpeed = 16; // from Google Maps 16 km/h
        walkingSpeed = 5; // from Google Maps 5 km/h
        unitsTo.put(from.getId(), new DistanceAndTimeEntry(0.0, 0.0));
        pq.put(from, 0.0);
    }

    public ArrayList<Node> getPath(Node from, Node to, boolean car, boolean bike, boolean walk, boolean fastest) throws NoNavigationResultException {
        setup(from, to, car, bike, walk, fastest);
        Node n = checkNode();

        if (n != to) {
            setup(from, to, car, bike, walk, fastest);
            tryAgain = true; // TODO: 4/19/21 really not the most beautiful thing...
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

    public double getTotalDistance() {
        return unitsTo.get(to.getId()).distance;
    }

    public double getTotalTime() {
        return unitsTo.get(to.getId()).time;
    }

    public String getRouteDescription() {
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
    }

    private Node checkNode() {
        Node n = null;
        while (!pq.isEmpty()) {
            n = temporaryRemoveAndGetMin();
            if (n != to) relax(n);
            else break;
        }
        return n;
    }

    private Node temporaryRemoveAndGetMin() { // TODO: 4/15/21 make more efficient – probably tree
        double minValue = Double.POSITIVE_INFINITY;
        Node minNode = null;

        for (Map.Entry<Node, Double> longDoubleEntry : pq.entrySet()) {
            if (longDoubleEntry.getValue() < minValue) {
                minValue = longDoubleEntry.getValue();
                minNode = longDoubleEntry.getKey();
            }
        }
        pq.remove(minNode);
        return minNode;
    }

    private ArrayList<Node> getTrack(ArrayList<Node> nodes, Node currentNode) {
        if (currentNode != null) {
            nodes.add(currentNode);
            getTrack(nodes, nodeBefore.get(currentNode.getId()));
        }
        return nodes;
    }

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
            } else if (walk) {
                if (w.isWalkable()) {
                    getPreviousNode(adjacentNodes, w, currentFrom);
                    getNextNode(adjacentNodes, w, currentFrom);
                }
            }
            if (!adjacentNodes.isEmpty()) {
                for (Node n : adjacentNodes) {
                    boolean[] doAdjacentNodesHaveRestrictions = new boolean[adjacentNodes.size()];
                    if (!isThereARestriction(wayBefore.get(currentFrom.getId()), currentFrom, w)) {
                        checkDistance(currentFrom, n, w);
                    }
                }
            }
        }
    }

    private void getPreviousNode(ArrayList<Node> adjacentNodes, Way w, Node currentFrom) {
        Node previousNode = w.getPreviousNode(currentFrom);
        if (previousNode != null) adjacentNodes.add(previousNode);
    }

    private void getNextNode(ArrayList<Node> adjacentNodes, Way w, Node currentFrom) {
        Node nextNode = w.getNextNode(currentFrom);
        if (nextNode != null) adjacentNodes.add(nextNode);
    }

    private boolean isThereARestriction(Way fromWay, Node viaNode, Way toWay) {
        ArrayList<Relation> restrictionsViaNode = nodeToRestriction.getElementsFromNode(viaNode);

        if (restrictionsViaNode != null) {
            for (Relation restriction : restrictionsViaNode) {
                if (restriction.getRestriction().contains("no_") && restriction.getFrom() == fromWay && restriction.getViaNode() == viaNode && restriction.getTo() == toWay) { // TODO: 4/19/21 er check med viaNode nødvendigt grundet nodeToorest lookup? same nedenunder for viaWay
                    return true;
                } else if (restriction.getRestriction().contains("only_")) { // TODO: 4/20/21 ja....
                    System.out.println(fromWay.getName() + " " + viaNode.getId() + " " + toWay.getName());
                }
            }
        }
        if (fromWay != null) { // TODO: 4/19/21 re-organize
            ArrayList<Relation> restrictionsViaWay = wayToRestriction.getElementsFromNode(fromWay);
            if (restrictionsViaWay != null) {
                for (Relation restriction : restrictionsViaWay) {
                    if (restriction.getRestriction().contains("no_") && restriction.getViaWay() == fromWay && restriction.getTo() == toWay) {
                        Node beforeNode = nodeBefore.get(viaNode.getId());

                        while (wayBefore.get(beforeNode.getId()) == fromWay) { // while we are "walking back" on the same Way
                            beforeNode = nodeBefore.get(beforeNode.getId());
                        }

                        if (wayBefore.get(beforeNode.getId()) == restriction.getFrom()) { // walk back until you reach a different Way – then check if that is the from way
                            if (tryAgain)
                                unitsTo.put(viaNode.getId(), new DistanceAndTimeEntry(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
                            return true;
                        }
                    } else if (restriction.getRestriction().contains("only_")) { // TODO: 4/20/21 ja....

                    }
                }
            }
        }
        return false;
    }

    private void checkDistance(Node currentFrom, Node currentTo, Way w) {
        long fromId = currentFrom.getId();
        long toId = currentTo.getId();

        double currentDistanceTo = unitsTo.get(toId) == null ? Double.POSITIVE_INFINITY : unitsTo.get(toId).distance;
        double currentTimeTo = unitsTo.get(toId) == null ? Double.POSITIVE_INFINITY : unitsTo.get(toId).time;

        double distanceBetweenFromTo = getDistanceBetweenTwoNodes(currentFrom, currentTo);
        double timeBetweenFromTo = getTravelTime(distanceBetweenFromTo, w);

        if (fastest) {
            if (unitsTo.get(fromId).time + timeBetweenFromTo < currentTimeTo) {
                updateMaps(toId, fromId, w, currentFrom, distanceBetweenFromTo, timeBetweenFromTo);
                pq.put(currentTo, unitsTo.get(toId).time);
            }
        } else {
            if (unitsTo.get(fromId).distance + distanceBetweenFromTo < currentDistanceTo) {
                updateMaps(toId, fromId, w, currentFrom, distanceBetweenFromTo, timeBetweenFromTo);
                pq.put(currentTo, unitsTo.get(toId).distance);
            }
        }
    }

    private void updateMaps(long toId, long fromId, Way w, Node currentFrom, double distanceBetweenFromTo, double timeBetweenFromTo) {
        unitsTo.put(toId, new DistanceAndTimeEntry(unitsTo.get(fromId).distance + distanceBetweenFromTo, unitsTo.get(fromId).time + timeBetweenFromTo));
        nodeBefore.put(toId, currentFrom);
        wayBefore.put(toId, w);
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

    private double getTravelTime(double distance, Way w) {
        double speed;
        if (bike) speed = bikingSpeed;
        else if (walk) speed = walkingSpeed;
        else speed = w.getMaxSpeed();
        return distance / (speed * (5f / 18f));
    }

    private void printResult(ArrayList<Node> result) {
        int counter = 1;
        for (int i = result.size() - 1; i >= 0; i--) {
            System.out.println("");
            System.out.println("Node: " + counter + ", id: " + result.get(i).getId() + ", coordinates: " + Arrays.toString(result.get(i).getCoordinates()));
            System.out.println("Street(s) referenced:");
            System.out.println("");
            counter++;
        }
    }

    /**
     * Class which holds the distance and a time to a certain node. Necessary to keep track of both variables
     * as time various by the road type for cars.
     */
    private class DistanceAndTimeEntry {
        private double distance;
        private double time;

        public DistanceAndTimeEntry(double distance, double time) {
            this.distance = distance;
            this.time = time;
        }
    }
}
