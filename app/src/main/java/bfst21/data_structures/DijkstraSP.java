package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DijkstraSP {
    // TODO: 4/10/21 Add vehicle type 
    // TODO: 4/10/21 Fix road types travel (like no bike lanes for car) 
    // TODO: 4/10/21 Add shortest + fastest 
    // TODO: 4/10/21 Add restrictions 
    // TODO: 4/10/21 Improve remove min 
    // TODO: 4/10/21 Consider distTo and Edgefrom types => what makes sense? 
    // TODO: 4/10/21 Is distance between nodes correct? 

    private NodeToWayMap nodeToWayMap;
    private Node from;
    private Node to;
    private String vehicleType;
    private String fastestOrShortest;
    private HashMap<Long, Double> distTo; // TODO: 4/9/21 node?
    private HashMap<Long, Node> nodeBefore;
    private HashMap<Node, Double> pq;


    public DijkstraSP(NodeToWayMap nodeToWayMap, Node from, Node to, String vehicleType, String fastestOrShortest) { // TODO: 4/9/21 right now you need to wipe to create new route
        this.nodeToWayMap = nodeToWayMap;
        this.from = from;
        this.to = to;
        this.vehicleType = vehicleType;
        this.fastestOrShortest = fastestOrShortest;
        distTo = new HashMap<>();
        nodeBefore = new HashMap<>();
        pq = new HashMap<>();
    }

    public ArrayList<Node> getPath() {
        distTo.put(from.getId(), 0.0);
        pq.put(from, 0.0);

        Node n = null;
        while (!pq.isEmpty()) {
            n = temporaryRemoveAndGetMin();
            if (n != to) relax(n);
            else break;
        }
        if (n != to) {
            System.out.println("island"); // TODO: 4/12/21 fix this / do something -> happens when a route cannot be found as the last node should be "to" node'en if it worked. 
        }

        return getTrack(new ArrayList<>(), n);
    }

    private Node temporaryRemoveAndGetMin() {
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

    private ArrayList<Node> getTrack(ArrayList<Node> nodes, Node currentNode) {
        if (currentNode != null) {
            nodes.add(currentNode);
            getTrack(nodes, nodeBefore.get(currentNode.getId()));
        }
        return nodes;
    }

    private void relax(Node from) {

        ArrayList<Way> waysWithFromNode = nodeToWayMap.getWaysFromNode(from);
        ArrayList<Node> adjacentNodes = new ArrayList<>();
        for(Way w : waysWithFromNode) { // TODO: 4/14/21 handle one-way
            Node nextNode = w.getNextNode(from);
            if (nextNode != null) adjacentNodes.add(nextNode);

            Node previousNode = w.getPreviousNode(from);
            if (previousNode != null) adjacentNodes.add(previousNode);

        }

        if (adjacentNodes.size() > 0) {
            for (Node to : adjacentNodes) {
                double distanceBetweenFromTo = getDistanceBetweenTwoNodes(from, to);
                long fromId = from.getId();
                long toId = to.getId();

                double distanceTo = distTo.get(toId) == null ? Double.POSITIVE_INFINITY : distTo.get(toId);

                if (distanceTo > distTo.get(fromId) + distanceBetweenFromTo) {
                    distTo.put(toId, distTo.get(fromId) + distanceBetweenFromTo);
                    nodeBefore.put(toId, from);
                    pq.put(to, distTo.get(toId)); // do not need if else because updates if it is not there and inserts if not there
                }
            }
        }
    }

    private double getDistanceBetweenTwoNodes(Node from, Node to) { // TODO: 4/9/21 From mapcanvas w/small changes
        //Adapted from https://www.movable-type.co.uk/scripts/latlong.html
        //Calculations need y to be before x in a point.
        double earthRadius = 6371e3; //in meters

        double lat1 = from.getyMax() * Math.PI / 180;
        double lat2 = to.getyMax() * Math.PI / 180;
        double lon1 = from.getxMax();
        double lon2 = to.getxMax();

        double deltaLat = (lat2 - lat1) * Math.PI / 180;
        double deltaLon = (lon2 - lon1) * Math.PI / 180;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        double scale = Math.pow(10, 1);
        return (distance * scale) / scale; // TODO: 4/9/21 scale cancels – why is it there?
    }
}
