package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DijkstraSP {
    // TODO: 4/10/21 Add restrictions 
    // TODO: 4/10/21 Improve remove min
    // TODO: 4/10/21 Is distance between nodes correct?
    // TODO: 4/15/21 Walk and bike speed in Way

    // TODO: 4/15/21 fastest and shortest for bike/walk should always be the same due to the same speed right? In that case, fastest/shortest selection does not make sense for walk/bike 

    private ElementToElementsTreeMap<Node, Way> nodeToWayMap;
    private ElementToElementsTreeMap<Node, Relation> nodeToRestriction;
    private Node from;
    private Node to;
    private HashMap<Long, Double> unitsTo;
    private HashMap<Long, Node> nodeBefore;
    private HashMap<Node, Double> pq;
    private boolean car;
    private boolean bike;
    private boolean walk;
    private boolean fastest;
    private double bikingSpeed;
    private double walkingSpeed;
    private double totalUnits;

    public DijkstraSP(ElementToElementsTreeMap<Node, Way> nodeToWayMap, ElementToElementsTreeMap<Node, Relation> nodeToRestriction) {
        this.nodeToRestriction = nodeToRestriction;
        this.nodeToWayMap = nodeToWayMap;
    }

    private void setup(Node from, Node to, boolean car, boolean bike, boolean walk, boolean fastest) {
        this.from = from;
        this.to = to;
        this.car = car;
        this.bike = bike;
        this.walk = walk;
        this.fastest = fastest;
        unitsTo = new HashMap<>();
        nodeBefore = new HashMap<>();
        pq = new HashMap<>();
        bikingSpeed = 16; // from Google Maps 16 km/h
        walkingSpeed = 5; // from Google Maps 5 km/h
        totalUnits = 0;
    }

    public ArrayList<Node> getPath(Node from, Node to, boolean car, boolean bike, boolean walk, boolean fastest) {
        setup(from, to, car, bike, walk, fastest);
        unitsTo.put(from.getId(), 0.0);
        pq.put(from, 0.0);

        Node n = null;
        while (!pq.isEmpty()) {
            n = temporaryRemoveAndGetMin();
            if (n != to) relax(n);
            else break;
        }
        if (n != to) {
            // TODO: 4/12/21 fix this / do something -> happens when a route cannot be found as the last node should be "to" node'en if it worked.
            System.err.println("Dijkstra: navigation is not possible with this from/to e.g. due to vehicle restrictions, island, etc.");
            return new ArrayList<>();
        } else {
            return getTrack(new ArrayList<>(), n);
        }
    }

    public double getTotalUnits() {
        return totalUnits; // TODO: 4/15/21 think its wrong... should be able to just do distanceto with current node 
    }

    private Node temporaryRemoveAndGetMin() { // TODO: 4/15/21 make more efficient 
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
            //System.out.println(unitsTo.get(currentNode.getId()));
            nodes.add(currentNode);
            totalUnits += unitsTo.get(currentNode.getId());
            getTrack(nodes, nodeBefore.get(currentNode.getId()));
        }
        return nodes;
    }

    private void relax(Node currentFrom) {
        ArrayList<Way> waysWithFromNode = nodeToWayMap.getWaysFromNode(currentFrom);
        ArrayList<Node> adjacentNodes = new ArrayList<>();

        for (Way w : waysWithFromNode) {

            if (car) {
                if (w.isDriveable()) {
                    if (!w.isOnewayRoad()) {
                        getPreviousNode(adjacentNodes, w, currentFrom);
                    }
                    getNextNode(adjacentNodes, w, currentFrom);
                }
            } else if (bike) { // TODO: 4/15/21 roundabouts, one-way, and bikes... (currently, the bike can go the wrong way) Also relevant for certain bike lanes [missing oneway:bicycle which is just oneway for cycleway...]
                if (w.isCycleable()) {
                    getPreviousNode(adjacentNodes, w, currentFrom);
                    getNextNode(adjacentNodes, w, currentFrom);
                }
            } else if (walk) {
                if (w.isWalkable()) {
                    getPreviousNode(adjacentNodes, w, currentFrom);
                    getNextNode(adjacentNodes, w, currentFrom);
                }
            }
            if (adjacentNodes.size() > 0) {
                for (Node currentTo : adjacentNodes) {
                    checkDistance(currentFrom, currentTo, w);
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

    private void checkDistance(Node currentFrom, Node currentTo, Way w) {
        long fromId = currentFrom.getId();
        long toId = currentTo.getId();

        double currentUnitsTo = unitsTo.get(toId) == null ? Double.POSITIVE_INFINITY : unitsTo.get(toId);
        double unitsBetweenFromTo = getDistanceBetweenTwoNodes(currentFrom, currentTo);
        if (fastest) {
            unitsBetweenFromTo = getTravelTime(unitsBetweenFromTo, w);
        }

        if (unitsTo.get(fromId) + unitsBetweenFromTo < currentUnitsTo) {
            unitsTo.put(toId, unitsTo.get(fromId) + unitsBetweenFromTo);
            nodeBefore.put(toId, currentFrom);
            pq.put(currentTo, unitsTo.get(toId)); // do not need if else because updates if it is not there and inserts if not there
        }
    }

    private double getDistanceBetweenTwoNodes(Node from, Node to) { // TODO: 4/9/21 From mapcanvas w/small changes
        //Adapted from https://www.movable-type.co.uk/scripts/latlong.html
        //Calculations need y to be before x in a point.
        double earthRadius = 6371e3; //in meters

        double lat1 = Math.toRadians(convertToGeo(from.getyMax()));
        double lat2 = Math.toRadians(convertToGeo(to.getyMax()));
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
        if (bike) {
            speed = bikingSpeed;
        } else if (walk) {
            speed = walkingSpeed;
        } else {
            speed = w.getMaxSpeed();
        }
        return distance / speed;
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
        System.out.println("DONE");
    }
}
