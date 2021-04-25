package bfst21.data_structures;

import bfst21.Exceptions.NoNavigationResultException;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.*;

public class RouteNavigation implements Serializable {
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
    private HashMap<Node, DistanceAndTimeEntry> unitsTo;
    private HashMap<Node, Node> nodeBefore;
    private HashMap<Node, Way> wayBefore;
    private PriorityQueue<Node> pq;
    private boolean car;
    private boolean bike;
    private boolean walk;
    private boolean fastest;
    private boolean tryAgain;
    private boolean aStar;
    private double bikingSpeed;
    private double walkingSpeed;
    private int maxSpeed;

    private Node testTo;
    private KDTree<Node> kdTree;

    public RouteNavigation(KDTree<Node> kdTree, ElementToElementsTreeMap<Node, Way> nodeToWayMap, ElementToElementsTreeMap<Node, Relation> nodeToRestriction, ElementToElementsTreeMap<Way, Relation> wayToRestriction) {
        this.nodeToRestriction = nodeToRestriction;
        this.wayToRestriction = wayToRestriction;
        this.nodeToWayMap = nodeToWayMap;
        this.maxSpeed = 130;

        this.kdTree = kdTree;
    }

    private void setup(Node from, Node to, boolean car, boolean bike, boolean walk, boolean fastest, boolean aStar) {
        this.to = to;
        this.car = car;
        this.bike = bike;
        this.walk = walk;
        this.fastest = fastest;
        this.aStar = aStar;
        tryAgain = false;
        routeDescription = new ArrayList<>();
        unitsTo = new HashMap<>();
        nodeBefore = new HashMap<>();
        wayBefore = new HashMap<>();
        pq = new PriorityQueue<>((a, b) -> Integer.compare(unitsTo.get(a).compareTo(unitsTo.get(b)), 0)); // different comparator
        bikingSpeed = 16; // from Google Maps 16 km/h
        walkingSpeed = 5; // from Google Maps 5 km/h
        pq.add(from);
        unitsTo.put(from, new DistanceAndTimeEntry(0, 0, 0));

        testTo = null;
    }


    /////////////////////TEST START

    public void createCvsFromData(ArrayList<ArrayList<String>> rows, String fileName, String firstLabel, String secondLabel) {

        try {
            FileWriter csvWriter = new FileWriter(fileName);
            csvWriter.append("FugleFlugtDistanceM");
            csvWriter.append(",");
            if (secondLabel != null) {
                csvWriter.append(secondLabel);
                csvWriter.append(",");
                csvWriter.append(firstLabel);
                csvWriter.append(",");
            }
            csvWriter.append("DijkstraTimeNanoS");
            csvWriter.append(",");
            csvWriter.append("AStarTimeNanoS");
            csvWriter.append("\n");

            for (ArrayList<String> rowData : rows) {
                csvWriter.append(String.join(",", rowData));
                csvWriter.append("\n");
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testOld() throws NoNavigationResultException {

        ArrayList<ArrayList<String>> rowsValid = new ArrayList<>();
        ArrayList<ArrayList<String>> rowsNotValid = new ArrayList<>();
        ArrayList<Node> nodes = kdTree.getNodes();

        for (int i = 0; i < 10; i++) {
            Node from = pickNode(nodes);
            Node to = pickNode(nodes);
            ArrayList<String> result = new ArrayList<>();
            result.add(String.valueOf(getDistanceBetweenTwoNodes(from, to)));

            ArrayList<Long> dijkstraTimes = runTest(from, to, false);
            if (testTo == to) result.add(String.valueOf(unitsTo.get(to).distance));
            ArrayList<Long> aStarTimes = runTest(from, to, true);

            if (testTo == to) {
                result.add(String.valueOf(unitsTo.get(to).distance));
                result.add(String.valueOf(getAverageUnits(dijkstraTimes)));
                result.add(String.valueOf(getAverageUnits(aStarTimes)));
                rowsValid.add(result);
                System.out.println("Valid " + i);
            } else {

                result.add(String.valueOf(getAverageUnits(dijkstraTimes)));
                result.add(String.valueOf(getAverageUnits(aStarTimes)));
                rowsNotValid.add(result);
                System.out.println("Not valid " + i);
                i -= 1;
            }
        }
        createCvsFromData(rowsValid, "valid.csv", "DistanceMDijkstra", "DistanceMAStar");
        createCvsFromData(rowsNotValid, "not_valid.csv", null, null);
    }


    public void test() throws NoNavigationResultException {
        LocalTime start = LocalTime.now();
        System.out.println(start);

        ArrayList<ArrayList<String>> rowsValid = new ArrayList<>();
        ArrayList<ArrayList<String>> rowsNotValid = new ArrayList<>();
        ArrayList<Node> nodes = kdTree.getNodes();

        int lowerRange = 0;
        int upperRange = 10000;
        int count = 0;
        boolean notDone = true;

        while (notDone) {

            if (upperRange == 100000 && count == 50) {
                notDone = false;
                System.out.println("done");
                continue;
            } else if (count == 50) {
                lowerRange = upperRange;
                upperRange += 10000;
                count = 0;
                continue;
            }

            Node from = pickNode(nodes);
            Node to = pickNode(nodes);
            ArrayList<String> result = new ArrayList<>();
            double distanceFlight = getDistanceBetweenTwoNodes(from, to);
            result.add(String.valueOf(distanceFlight));
            if (distanceFlight > upperRange || distanceFlight < upperRange / 2f) continue;

            ArrayList<Long> aStarTimes = runTest(from, to, true);
            if (testTo == to) {
                double distance = unitsTo.get(to).distance;
                if (distance < lowerRange || distance > upperRange) {
                    continue;
                }
                result.add(String.valueOf(unitsTo.get(to).distance));
            }
            ArrayList<Long> dijkstraTimes = runTest(from, to, false);

            if (testTo == to) {
                result.add(String.valueOf(unitsTo.get(to).distance));
                result.add(String.valueOf(getAverageUnits(dijkstraTimes)));
                result.add(String.valueOf(getAverageUnits(aStarTimes)));
                rowsValid.add(result);
                count++;
                System.out.println("Valid " + upperRange + " " + count);
            } else {
                result.add(String.valueOf(getAverageUnits(dijkstraTimes)));
                result.add(String.valueOf(getAverageUnits(aStarTimes)));
                rowsNotValid.add(result);
                System.out.println("Not valid " + upperRange + " " + count);
            }
        }
        createCvsFromData(rowsValid, "valid_bigger.csv", "DistanceMDijkstra", "DistanceMAStar");
        createCvsFromData(rowsNotValid, "not_valid_bigger.csv", null, null);
        System.out.println("DONE " + start + " " + LocalTime.now());
    }

    private ArrayList<Long> runTest(Node from, Node to, boolean aStarOn) {
        testTo = null;
        ArrayList<Long> times = new ArrayList<>();
        for (int j = 0; j < 3; j++) {
            setup(from, to, true, false, false, false, aStarOn);
            long start = System.nanoTime();
            Node n = checkNode();
            long finish = System.nanoTime();
            long timeElapsed = finish - start;
            times.add(timeElapsed);
            testTo = n;
        }
        return times;
    }

    public void testOther() throws NoNavigationResultException {
        LocalTime start = LocalTime.now();
        System.out.println(start);
        ArrayList<ArrayList<String>> rowsValid = new ArrayList<>();
        ArrayList<ArrayList<String>> rowsNotValid = new ArrayList<>();
        ArrayList<Node> nodes = kdTree.getNodes();

        boolean notDone = true;
        int[] counters = new int[10];

        while (notDone) {

            int countDone = 0;
            for (int i : counters) {
                if (i >= 25) {
                    countDone++;
                }
            }
            if (countDone == 10) {
                notDone = false;
                continue;
            }

            Node from = pickNode(nodes);
            Node to = pickNode(nodes);
            ArrayList<String> result = new ArrayList<>();
            double dis = getDistanceBetweenTwoNodes(from, to);
            if (dis > 100000) continue;
            result.add(String.valueOf(dis));

            ArrayList<Long> aStarTimes = runTest(from, to, true);
            if (testTo == to) {
                double distance = unitsTo.get(to).distance;
                if (distance > 100000) continue;
                int remainder = (int) (distance / 1000 % 10);
                int left = (int) ((distance / 1000) - remainder);
                int index = left / 10;
                counters[index]++;
                result.add(String.valueOf(unitsTo.get(to).distance));
            }
            ArrayList<Long> dijkstraTimes = runTest(from, to, false);

            if (testTo == to) {
                result.add(String.valueOf(unitsTo.get(to).distance));
                result.add(String.valueOf(getAverageUnits(dijkstraTimes)));
                result.add(String.valueOf(getAverageUnits(aStarTimes)));
                rowsValid.add(result);
                System.out.println("Valid " + Arrays.toString(counters));
            } else {
                result.add(String.valueOf(getAverageUnits(dijkstraTimes)));
                result.add(String.valueOf(getAverageUnits(aStarTimes)));
                rowsNotValid.add(result);
                System.out.println("N-val " + Arrays.toString(counters));
            }
        }
        createCvsFromData(rowsValid, "valid_bigger.csv", "DistanceMDijkstra", "DistanceMAStar");
        createCvsFromData(rowsNotValid, "not_valid_bigger.csv", null, null);
        System.out.println("DONE " + start + " " + LocalTime.now());
    }

    private long getAverageUnits(ArrayList<Long> times) {
        long total = 0;

        for (Long time : times) {
            total += time;
        }
        return total / times.size();
    }

    private Node pickNode(ArrayList<Node> nodes) {
        int size = kdTree.getSize();
        Random generator = new Random();
        int number = generator.nextInt(size);
        return nodes.get(number);
    }


    /////////////////////TEST END


    public ArrayList<Node> getPath(Node from, Node to, boolean car, boolean bike, boolean walk, boolean fastest, boolean aStar) throws NoNavigationResultException {
        setup(from, to, car, bike, walk, fastest, aStar);

        long start = System.nanoTime(); // TODO: 4/23/21 remove
        Node n = checkNode();
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        System.out.println(timeElapsed);

        if (n != to) {
            setup(from, to, car, bike, walk, fastest, aStar);
            tryAgain = true; // TODO: 4/19/21 really not the most beautiful thing...
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

    public double getTotalDistance() {
        if (unitsTo.get(to) != null) return unitsTo.get(to).distance; // TODO: 4/23/21 change these two back?
        else return 0;
    }

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

    private Node checkNode() {
        Node n = null;
        while (!pq.isEmpty()) {
            n = pq.poll();
            if (n != to) relax(n);
            else break;
        }
        return n;
    }

    private ArrayList<Node> getTrack(ArrayList<Node> nodes, Node currentNode) {
        if (currentNode != null) {
            nodes.add(currentNode);
            getTrack(nodes, nodeBefore.get(currentNode));
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
                    //System.out.println(fromWay.getName() + " " + viaNode.getId() + " " + toWay.getName());
                }
            }
        }
        if (fromWay != null) { // TODO: 4/19/21 re-organize
            ArrayList<Relation> restrictionsViaWay = wayToRestriction.getElementsFromNode(fromWay);
            if (restrictionsViaWay != null) {
                for (Relation restriction : restrictionsViaWay) {
                    if (restriction.getRestriction().contains("no_") && restriction.getViaWay() == fromWay && restriction.getTo() == toWay) {
                        Node beforeNode = nodeBefore.get(viaNode);

                        while (wayBefore.get(beforeNode) == fromWay) { // while we are "walking back" on the same Way
                            beforeNode = nodeBefore.get(beforeNode);
                        }

                        if (wayBefore.get(beforeNode) == restriction.getFrom()) { // walk back until you reach a different Way – then check if that is the from way
                            if (tryAgain)
                                unitsTo.put(viaNode, new DistanceAndTimeEntry(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
                            return true;
                        }
                    } //else if (restriction.getRestriction().contains("only_")) { // TODO: 4/20/21 ja....

                    //}
                }
            }
        }
        return false;
    }

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