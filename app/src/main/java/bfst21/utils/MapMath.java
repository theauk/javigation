package bfst21.utils;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.NodeHolder;
import bfst21.Osm_Elements.Way;
import javafx.geometry.Point2D;

import java.util.List;

/**
 * The MapMath class is a collection of useful math operations used when working with maps.
 * It cannot be extended! (Hence final).
 */
public final class MapMath {

    private MapMath() { }

    /**
     * Calculates the cross product between the two vectors from p1 to p2 na dp2 to p3.
     *
     * @param p1 the start point.
     * @param p2 the via point.
     * @param p3 the end point.
     * @return the cross product of the two vectors V1 = (p2 - p1) and V2 = (p3 - p2).
     */
    public static double crossProduct(Point2D p1, Point2D p2, Point2D p3) {
        Point2D v1 = new Point2D((p2.getX() - p1.getX()), (p2.getY() - p1.getY()));
        Point2D v2 = new Point2D((p3.getX() - p2.getX()), (p3.getY() - p2.getY()));
        return v1.getX() * v2.getX() - v1.getY() * v2.getX();
    }

    /**
     * Calculates the dot product between the two vectors from p1 to p2 and p2 to p3.
     *
     * @param p1 the start point.
     * @param p2 the via point.
     * @param p3 the end point.
     * @return the dot product of the two vectors V1 = (p2 - p1) and V2 = (p3 - p2).
     */
    public static double dotProduct(Point2D p1, Point2D p2, Point2D p3) {
        Point2D v1 = new Point2D((p2.getX() - p1.getX()), (p2.getY() - p1.getY()));
        Point2D v2 = new Point2D((p3.getX() - p2.getX()), (p3.getY() - p2.getY()));
        return v1.getX() * v2.getX() + v1.getY() * v2.getY();
    }

    /**
     * Calculates the angle between p1 and p3 through p2 in degrees ranging from -180 to 180.
     *
     * @param p1 the start point.
     * @param p2 the via point.
     * @param p3 the end point.
     * @return the turn angle between the p1 and p3.
     */
    public static double turnAngle(Node p1, Node p2, Node p3) {
        double v1 = Math.atan2(p3.getyMax() - p1.getyMax(), p3.getxMax() - p1.getxMax());
        double v2 = Math.atan2(p2.getyMax() - p1.getyMax(), p2.getxMax() - p1.getxMax());
        double result = v1 - v2;

        if(result > Math.PI) result -= 2 * Math.PI;
        else if (result <= -Math.PI) result += 2 * Math.PI;
        return Math.toDegrees(result);
    }

    /**
     * <p>Calculates a compass direction from a bearing angle between two points.</p>
     * <img src="http://www.mathsmutt.co.uk/files/Bearings_files/bear2.gif" width="400" height="400">
     *
     * @param n1 the start point.
     * @param n2 the end point.
     * @return a String containing a compass direction ranging from NORTH, NORTH EAST, EAST and so forth or UNKNOWN if not found.
     */
    public static String compassDirection(Node n1, Node n2) {
        return compassDirection(bearing(n1, n2));
    }

    /**
     * Calculates a compass direction from a bearing angle between two points.
     *
     * @param bearing the bearing angle.
     * @return a String containing a compass direction ranging from NORTH, NORTH EAST, EAST and so forth or UNKNOWN if not found.
     */
    public static String compassDirection(double bearing) {
        if(bearing > 337.5 || bearing >= 0 && bearing < 22.5) return "NORTH";
        else if(bearing > 22.5 && bearing < 67.5) return "NORTH EAST";
        else if(bearing > 67.5 && bearing < 112.5) return "EAST";
        else if(bearing > 112.5 && bearing < 157.5) return "SOUTH EAST";
        else if(bearing > 157.5 && bearing < 202.5) return "SOUTH";
        else if(bearing > 202.5 && bearing < 247.5) return "SOUTH WEST";
        else if(bearing > 247.5 && bearing < 292.5) return "WEST";
        else if(bearing > 292.5 && bearing < 337.5) return "NORTH WEST";
        return "UNKNOWN";
    }

    /**
     * Calculates a bearing compass angle from two points.
     * A bearing angle is the horizontal angle between the direction between two objects and true north.
     *
     * @param p1 the start point.
     * @param p2 the end point.
     * @return a bearing compass angle spanning from 0 to 359.99 degrees
     */
    public static double bearing(Node p1, Node p2) {
        //Adapted from https://www.movable-type.co.uk/scripts/latlong.html
        double lat1 = Math.toRadians(convertToGeo(p1.getyMax()));
        double lat2 = Math.toRadians(convertToGeo(p2.getyMax()));
        double lon1 = Math.toRadians(p1.getxMax());
        double lon2 = Math.toRadians(p2.getxMax());
        double deltaLon = lon2 - lon1;

        double y = Math.sin(deltaLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        double bearing = Math.atan2(y, x);
        return (Math.toDegrees(bearing) + 360) % 360;
    }

    /**
     * Calculates and returns the distance between two geo points on earth using the <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a>.
     * Result is in meters.
     *
     * @param p1 the start point.
     * @param p2 the end point.
     * @return the distance between the two points in meters.
     */
    public static double distanceBetween(Point2D p1, Point2D p2) {
        //Adapted from https://www.movable-type.co.uk/scripts/latlong.html
        p1 = convertToGeoCoords(p1);
        p2 = convertToGeoCoords(p2);

        double earthRadius = 6371e3; //in meters

        //double lat1 = Math.toRadians(p1.getX()); // TODO: 4/29/21 what on earth...? Det her tror jeg virker
        //double lat2 = Math.toRadians(p2.getX());
        double lat1 = p1.getX();
        double lat2 = p2.getX();
        double lon1 = p1.getY();
        double lon2 = p2.getY();

        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    /**
     * Finds the distance between two nodes in meters.
     * @param from The from Node.
     * @param to The to Node.
     * @return The distance in meters.
     */
    public static double distanceBetweenTwoNodes(Node from, Node to) {
        Point2D p1 = new Point2D(from.getxMax(), from.getyMax());
        Point2D p2 = new Point2D(to.getxMax(), to.getyMax());
        return distanceBetween(p1, p2);
    }

    /**
     * Converts a y-coordinate to a geographical one.
     *
     * @param yCoordinate The y-coordinate to be converted.
     * @return The y-coordinate converted to a geographical coordinate.
     */
    public static double convertToGeo(double yCoordinate) {
        return -yCoordinate * 0.56f;
    }

    /**
     * Converts a screen coordinate to a geo-coordinate.
     * @param yCoordinate The coordinate as a screen coordinate.
     * @return
     */
    public static double convertToScreen(double yCoordinate) {
        return yCoordinate / -0.56f;
    }

    /**
     * Converts a point to a geographical point in (lat, lon).
     *
     * @param p The point to be converted.
     * @return A converted geographical point written as (latitude, longitude).
     */
    public static Point2D convertToGeoCoords(Point2D p) {
        return new Point2D(convertToGeo(p.getY()), p.getX());
    }

    /**
     * Rounds a number with the specified number of digits (after the comma).
     *
     * @param number the number to be rounded.
     * @param digits the number of digits to round to (after comma).
     * @return the rounded number specified
     */
    public static double round(double number, int digits) {
        double scale = Math.pow(10, digits);
        return Math.round(number * scale) / scale;
    }

    /**
     * Takes a string in the format hh:mm and converts it to total hours.
     * @param colonTime The time to be converted in the format hh:mm.
     * @return The time in hours.
     */
    public static double colonTimeToHours(String colonTime) {
        String[] timeParts = colonTime.split(":");
        double hours = Double.parseDouble(timeParts[0]);
        double minutes = Double.parseDouble(timeParts[1]);
        hours += minutes / 60;
        return hours;
    }

    /**
     * Gets the total distance between a list of nodes.
     * @param nodes A list of nodes.
     * @return The distance between all the nodes in kilometers.
     */
    public static double getTotalDistance(List<Node> nodes) {
        double distance = 0;
        for (int i = 0; i < nodes.size() - 1; i++) {
            distance += distanceBetweenTwoNodes(nodes.get(i), nodes.get(i + 1));
        }
        return distance / 1000;
    }

    /**
     * Finds the shortest distance between a point and a line.
     * @param queryX The x-coordinate for the point.
     * @param queryY The y-coordinate for the point.
     * @param nodeHolder The line.
     * @return The shortest distance.
     */
    public static double shortestDistanceToElement(float queryX, float queryY, NodeHolder nodeHolder) {
        double minDistance = Double.POSITIVE_INFINITY;
        List<Node> nodes = nodeHolder.getNodes();

        for (int i = 0; i < nodes.size() - 1; i++) {
            Point2D firstNode = new Point2D(nodes.get(i).getxMin(), nodes.get(i).getyMin());
            Point2D lastNode = new Point2D(nodes.get(i + 1).getxMin(), nodes.get(i + 1).getyMin());

            double numerator = Math.abs(((lastNode.getX() - firstNode.getX() ) * (firstNode.getY() - queryY)) - ((firstNode.getX() - queryX) * (lastNode.getY() - firstNode.getY())));
            double denominator = Math.sqrt(Math.pow(lastNode.getX() - firstNode.getX(), 2) + Math.pow(lastNode.getY() - firstNode.getY(), 2));
            double distance = numerator / denominator;
            if (distance < minDistance) minDistance = distance;
        }
        return minDistance;
    }

    /**
     * Finds the coordinates for the closest point on a way from a query point.
     * @param queryX The query point's x-coordinate.
     * @param queryY The query point's y-coordinate.
     * @param nearestWay The way to find the point on.
     * @return The point on the way as a Node with the coordinates.
     */
    public static Node getClosestPointOnWayAsNode(double queryX, double queryY, Way nearestWay) {
        Node p1NearestWay = nearestWay.getNodes().get(0);
        Node p2NearestWay = nearestWay.getNodes().get(1); // the way has max 2 nodes because of way segment split in R-tree
        double slopeNearestWay = getSlopeBetweenTwoNodes(p1NearestWay, p2NearestWay);
        double[] nearestWayStandardEquation = getStandardFormEquationFromPointAndSlope(p1NearestWay.getxMax(), p1NearestWay.getyMax(), slopeNearestWay);

        double perpendicularSlope = getReciprocalSlope(slopeNearestWay);
        double[] perpendicularStandardEquation = getStandardFormEquationFromPointAndSlope(queryX, queryY, perpendicularSlope);

        double[] coordinatesPointOnNearestWay = findIntersectionCramersRule(perpendicularStandardEquation, nearestWayStandardEquation);

        return new Node(0, (float) coordinatesPointOnNearestWay[1], (float) coordinatesPointOnNearestWay[0]); // TODO: 4/30/21 better to cast and have more precise cals or change to floats all the way through?
    }

    /**
     * Gets the slope between two Nodes.
     * @param n1 The first Node.
     * @param n2 The second Node.
     * @return The slope.
     */
    private static double getSlopeBetweenTwoNodes(Node n1, Node n2) {
        return (n2.getyMax() - n1.getyMax()) / (n2.getxMax() - n1.getxMax());
    }

    /**
     * Gets the reciprocal of a slope.
     * @param slope The slope.
     * @return The reciprocal slope.
     */
    public static double getReciprocalSlope(double slope) {
        return - (1 / slope);
    }

    /**
     * Gets the standard form of an equation (Ax + BX = C) given a point and a slope.
     * @param x The point's x-coordinate.
     * @param y The point's y-coordinate.
     * @param slope The slope.
     * @return The standard form of the equation as an array where the first index is A, the second B, and third C.
     */
    private static double[] getStandardFormEquationFromPointAndSlope(double x, double y, double slope) {
        double a = 1;
        double b = -slope;
        double c = slope * (-x) + y;
        return new double[]{a, b, c};
    }

    /**
     * Finds the intersection point between two lines.
     * @param line1 The first line in standard form as an array where the first index is A, the second B, and third C.
     * @param line2 The second line in standard form as an array where the first index is A, the second B, and third C.
     * @return The intersection point as an array where the first index is x and the second y.
     */
    private static double[] findIntersectionCramersRule(double[] line1, double[] line2) {
        double a1 = line1[0];
        double b1 = line1[1];
        double c1 = line1[2];

        double a2 = line2[0];
        double b2 = line2[1];
        double c2 = line2[2];

        double xNumerator = (c1 * b2) - (c2 * b1);
        double xDenominator = (a1 * b2) - (a2 * b1);
        double x = xNumerator / xDenominator;

        double yNumerator = (a1 * c2) - (a2 * c1);
        double yDenominator = (a1 * b2) - (a2 * b1);
        double y = yNumerator / yDenominator;

        return new double[]{x, y};
    }

    public static String formatDistance(double meters, int digits) {
        String s = "";
        if (meters < 1000) s += MapMath.round(meters, digits) + " m";
        else s += MapMath.round(meters / 1000f, digits) + " km";
        return s;
    }

    public static String formatTime(double seconds, int digits) {
        String s = "";
        if (seconds < 60) s += " , Total time: " + round(seconds, digits) + " s";
        else s += " , Total time: " + round(seconds / 60f, digits) + " min";
        return s;
    }

}
