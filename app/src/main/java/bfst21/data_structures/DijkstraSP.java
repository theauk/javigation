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

    // TODO: 4/16/21 Maybe wipe maps after use?

    // TODO: 4/15/21 fastest and shortest for bike/walk should always be the same due to the same speed right? In that case, fastest/shortest selection does not make sense for walk/bike

    // TODO: 4/19/21 ændrer tilbage til ikke object pq. Find ud af hvad der gælder med vej – er det fordi det er alle nodes der. På H er den lille via vej kun de to nodes det gælder. Så skal man se kigge mere tilbage i way to?

    private ElementToElementsTreeMap<Node, Way> nodeToWayMap;
    private ElementToElementsTreeMap<Node, Relation> nodeToRestriction;
    private ElementToElementsTreeMap<Way, Relation> wayToRestriction;
    private Node from;
    private Node to;
    private HashMap<Long, Double> unitsTo;
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
    private double totalUnits;

    public DijkstraSP(ElementToElementsTreeMap<Node, Way> nodeToWayMap, ElementToElementsTreeMap<Node, Relation> nodeToRestriction, ElementToElementsTreeMap<Way, Relation> wayToRestriction) {
        this.nodeToRestriction = nodeToRestriction;
        this.wayToRestriction = wayToRestriction;
        this.nodeToWayMap = nodeToWayMap;
    }

    private void setup(Node from, Node to, boolean car, boolean bike, boolean walk, boolean fastest) {
        this.from = from;
        this.to = to;
        this.car = car;
        this.bike = bike;
        this.walk = walk;
        this.fastest = fastest;
        tryAgain = false;
        unitsTo = new HashMap<>();
        nodeBefore = new HashMap<>();
        wayBefore = new HashMap<>();
        pq = new HashMap<>();
        bikingSpeed = 16; // from Google Maps 16 km/h
        walkingSpeed = 5; // from Google Maps 5 km/h
        totalUnits = 0;
        unitsTo.put(from.getId(), 0.0);
        pq.put(from, 0.0);
    }

    public ArrayList<Node> getPath(Node from, Node to, boolean car, boolean bike, boolean walk, boolean fastest) {
        setup(from, to, car, bike, walk, fastest);
        Node n = checkN();

        if (n != to) {

            setup(from, to, car, bike, walk, fastest);
            tryAgain = true; // TODO: 4/19/21 really not the most beautiful thing...
            n = checkN();

            if (n != to) {
                // TODO: 4/12/21 fix this / do something -> happens when a route cannot be found as the last node should be "to" node'en if it worked.
                System.err.println("Dijkstra: navigation is not possible with this from/to e.g. due to vehicle restrictions, island, etc.");
                return new ArrayList<>();
            } else {
                return getTrack(new ArrayList<>(), n);
            }
        } else {
            ArrayList<Node> nodes = getTrack(new ArrayList<>(), n);
            System.out.println("Units: " + getTotalUnits());
            return nodes;
        }
    }

    private Node checkN() {
        Node n = null;
        while (!pq.isEmpty()) {
            n = temporaryRemoveAndGetMin();
            if (n != to) relax(n);
            else break;
        }
        return n;
    }

    public double getTotalUnits() {
        return unitsTo.get(to.getId()); // TODO: 4/15/21 think its wrong... should be able to just do distanceto with current node
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
            //System.out.println(unitsTo.get(currentNode.getId()));
            nodes.add(currentNode);
            totalUnits += unitsTo.get(currentNode.getId());
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
            if (adjacentNodes.size() > 0) {
                for (Node n : adjacentNodes) {
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
                if (restriction.getFrom() == fromWay && restriction.getViaNode() == viaNode && restriction.getTo() == toWay) { // TODO: 4/19/21 er check med viaNode nædvendigt grundet nodeToorest lookup? same nedenunder for viaWay
                    return true;
                }
            }
        }
        if (fromWay != null) {
            ArrayList<Relation> restrictionsViaWay = wayToRestriction.getElementsFromNode(fromWay);
            if (restrictionsViaWay != null) {
                for (Relation restriction : restrictionsViaWay) {
                    if (restriction.getViaWay() == fromWay && restriction.getTo() == toWay) {
                        Node beforeNode = nodeBefore.get(viaNode.getId());

                        while (wayBefore.get(beforeNode.getId()) == fromWay) { // while we are "walking back" on the same Way
                            beforeNode = nodeBefore.get(beforeNode.getId());
                        }

                        if (wayBefore.get(beforeNode.getId()) == restriction.getFrom()) { // walk back until you reach a different Way – then check if that is the from way
                            if (tryAgain) unitsTo.put(viaNode.getId(), Double.POSITIVE_INFINITY);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
            wayBefore.put(toId, w);
            pq.put(currentTo, unitsTo.get(toId));
        }
    }

    private double getDistanceBetweenTwoNodes(Node from, Node to) { // TODO: 4/9/21 From mapcanvas w/small changes
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
    }
}
