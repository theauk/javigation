package bfst21.view;

import bfst21.utils.MapMath;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;

public class CanvasBounds
{
    private float minX;
    private float minY;
    private float maxX;
    private float maxY;

    public void setBounds(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public double getWidth() {
        return Math.abs(minX - maxX);
    }

    public double getHeight() {
        return Math.abs(minY - maxY);
    }

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