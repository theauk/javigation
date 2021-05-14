package bfst21.view;

import javafx.geometry.Point2D;

/**
 * A class representing the rectangular view port of a Canvas.
 * Each CanvasBounds object is represented by the diagonal points of the rectangle from the upper left corner to the lower right.
 * It also contains useful methods for determining the width and height of the bounds and the coordinates to the center point.
 */
public class CanvasBounds
{
    private float minX;
    private float minY;
    private float maxX;
    private float maxY;

    public CanvasBounds() {

    }

    /**
     * Sets the bounding box's coordinates.
     *
     * @param minX the x-coordinate of the upper left corner of rectangle
     * @param minY the y-coordinate of the upper left corner of the rectangle.
     * @param maxX the x-coordinate of the lower right corner of the rectangle.
     * @param maxY the y-coordinate of the lower right corner of the rectangle.
     */
    public void setBounds(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    /**
     * Calculates and returns the width of the bounds.
     *
     * @return the width of the bounds
     */
    public double getWidth() {
        return Math.abs(minX - maxX);
    }

    /**
     * Calculates and returns the height of the bounds.
     *
     * @return the height of the bounds.
     */
    public double getHeight() {
        return Math.abs(minY - maxY);
    }

    /**
     * Calculates and returns a point representing the center of the bounds.
     *
     * @return a point representing the center of the bounds.
     */
    public Point2D getCenter() {
        return new Point2D((minX + maxX) / 2, (minY + maxY) / 2);
    }

    public float getMinX() {
        return minX;
    }

    public float getMinY() {
        return minY;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMaxY() {
        return maxY;
    }
}