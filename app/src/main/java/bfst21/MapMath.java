package bfst21;

import bfst21.Osm_Elements.Node;
import javafx.geometry.Point2D;

public class MapMath {

    private MapMath() {

    }

    /**
     * Calculates the cross product between the two vectors from p1 to p2 na dp2 to p3.
     *
     * @param p1 the start point.
     * @param p2 the via point.
     * @param p3 the end point.
     * @return the cross product of the two vectors V1 = (p2 - p1) and V2 = (p3 - p2).
     */
    public static double crossProduct(Node p1, Node p2, Node p3) {
        Point2D v1 = new Point2D((p2.getxMax() - p1.getxMax()), (p2.getyMax() - p1.getyMax()));
        Point2D v2 = new Point2D((p3.getxMax() - p2.getxMax()), (p3.getyMax() - p2.getyMax()));
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
    public static double dotProduct(Node p1, Node p2, Node p3) {
        Point2D v1 = new Point2D((p2.getxMax() - p1.getxMax()), (p2.getyMax() - p1.getyMax()));
        Point2D v2 = new Point2D((p3.getxMax() - p2.getxMax()), (p3.getyMax() - p2.getyMax()));
        return v1.getX() * v2.getX() + v1.getY() * v2.getY();
    }

    /**
     * Calculates the angle between p1 and p3 through p2 in degrees.
     *
     * @param p1 the start point.
     * @param p2 the via point.
     * @param p3 the end point.
     * @return
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
     * Calculates a compass direction from a bearing angle between two points.
     *
     * @param n1 the start point.
     * @param n2 the end point.
     * @return a String containing a compass direction ranging from NORTH, NORTH EAST, EAST and so forth or UNKNOWN if not found.
     */
    public static String compassDirection(Node n1, Node n2) {
        return compassDirection(bearing(n1, n2));
    }

    public static double distanceBetween(Point2D p1, Point2D p2) {
        //Adapted from https://www.movable-type.co.uk/scripts/latlong.html
        double earthRadius = 6371e3; //in meters

        double lat1 = Math.toRadians(p1.getX());
        double lat2 = Math.toRadians(p2.getX());
        double lon1 = p1.getY();
        double lon2 = p2.getY();

        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    public static double convertToGeo(double yCoordinate) {
        return -yCoordinate * 0.56f;
    }

    public static double convertToScreen(double yCoordinate) {
        return yCoordinate / -0.56f;
    }

    public static Point2D convertToGeoCoords(Point2D p) {
        return new Point2D(convertToGeo(p.getY()), p.getX());
    }

    public static double round(double number, int digits) {
        double scale = Math.pow(10, digits);
        return Math.round(number * scale) / scale;
    }
}
