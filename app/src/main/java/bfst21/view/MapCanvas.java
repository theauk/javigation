package bfst21.view;

import bfst21.MapData;
import bfst21.Osm_Elements.Element;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

public class MapCanvas extends Canvas {
    private MapData mapData;
    private Affine trans;
    private CanvasBounds bounds;
    private Theme theme;

    private final int ZOOM_FACTOR = 2;
    private int zoomLevel = MIN_ZOOM_LEVEL;
    public final static int MIN_ZOOM_LEVEL = 1;
    public final static int MAX_ZOOM_LEVEL = 19;

    private final StringProperty ratio = new SimpleStringProperty("- - -");

    public void init(MapData mapData, Theme theme) {
        this.mapData = mapData;
        this.theme = theme;
        trans = new Affine();
        bounds = new CanvasBounds();

        widthProperty().addListener((observable, oldValue, newValue) -> pan((newValue.floatValue() - oldValue.floatValue()) / 2, 0));
        heightProperty().addListener((observable, oldValue, newValue) -> pan(0, (newValue.floatValue() - oldValue.floatValue()) / 2));
        updateMap();
    }

    public void repaint() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save();
        gc.setTransform(new Affine());

        gc.setFill(theme.get("background").getColor().getInner());
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setTransform(trans);

        for(Element element: mapData.getMapSegment()) {
            drawElement(gc, element);
        }

        gc.setStroke(Color.RED);
        gc.setLineDashes(getStrokeStyle("normal"));
        gc.setLineWidth(StrokeFactory.getStrokeWidth(1, trans));
        gc.strokeLine(mapData.getMinX(), mapData.getMinY(), mapData.getMaxX(), mapData.getMinY());
        gc.strokeLine(mapData.getMinX(), mapData.getMaxY(), mapData.getMaxX(), mapData.getMaxY());
        gc.strokeLine(mapData.getMinX(), mapData.getMinY(), mapData.getMinX(), mapData.getMaxY());
        gc.strokeLine(mapData.getMaxX(), mapData.getMinY(), mapData.getMaxX(), mapData.getMaxY());

        gc.strokeLine(bounds.getMinX(), (bounds.getMaxY() + bounds.getMinY()) / 2, getWidth(), (bounds.getMaxY() + bounds.getMinY()) / 2);
        gc.strokeLine((bounds.getMinX() + bounds.getMaxX()) / 2, bounds.getMinY(), (bounds.getMinX() + bounds.getMaxX()) / 2, getHeight());

        gc.restore();
    }

    private void drawElement(GraphicsContext gc, Element element) {
        gc.setLineDashes(getStrokeStyle(element.getType())); //Apply stroke style

        if(theme.get(element.getType()).isTwoColored()) {
            gc.setLineWidth(getStrokeWidth(element.getType(), false));
            gc.setStroke(theme.get(element.getType()).getColor().getOuter());
            element.draw(gc);
        }

        gc.setLineWidth(getStrokeWidth(element.getType(), true));
        gc.setStroke(theme.get(element.getType()).getColor().getInner());   //Get and apply line color
        element.draw(gc);

        if(theme.get(element.getType()).fill()) {
            gc.setFill(theme.get(element.getType()).getColor().getInner());
            gc.fill();
        }
    }

    private double[] getStrokeStyle(String type) {
        double[] strokeStyle = new double[2];
        for(int i = 0; i < strokeStyle.length; i++) strokeStyle[i] = StrokeFactory.getStrokeStyle(theme.get(type).getStyle(), trans);
        return strokeStyle;
    }

    private double getStrokeWidth(String type, boolean inner) {
        if(inner) return StrokeFactory.getStrokeWidth(theme.get(type).getInnerWidth(), trans);
        return StrokeFactory.getStrokeWidth(theme.get(type).getOuterWidth(), trans);
    }

    private double getDistance(Point2D start, Point2D end) {
        //Adapted from https://www.movable-type.co.uk/scripts/latlong.html
        //Calculations need y to be before x in a point.
        double earthRadius = 6371e3; //in meters

        double lat1 = start.getY() * Math.PI / 180;
        double lat2 = end.getY() * Math.PI / 180;
        double lon1 = start.getX();
        double lon2 = end.getX();

        double deltaLat = (lat2 - lat1) * Math.PI / 180;
        double deltaLon = (lon2 - lon1) * Math.PI / 180;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        double scale = Math.pow(10, 1);
        return Math.round(distance * scale) / scale;
    }

    private void calculateRatio()
    {
        Point2D start = new Point2D(bounds.getMinX(), convertToGeo(bounds.getMinY()));
        Point2D end = new Point2D(bounds.getMaxX(), convertToGeo(bounds.getMinY()));

        double distance = getDistance(start, end);
        double pixels = getWidth();
        double dPerPixel = distance / pixels;
        //System.out.println(start + "\n" + end);
        //System.out.println("Distance: " + distance + " m");
        //System.out.println(dPerPixel + " px/m");
        //System.out.println("50 px = " + (dPerPixel * 50));
        int scale = (int) (dPerPixel * 50);
        String toDisplay = scale >= 1000 ? (scale / 1000) + " km" : scale + " m";
        ratio.set(toDisplay);
    }

    public void zoom(boolean zoomIn, Point2D center) {
        if(!validZoom(zoomIn)) return;

        if(zoomIn && zoomLevel < MAX_ZOOM_LEVEL) zoomLevel++;
        else if(!zoomIn && zoomLevel > MIN_ZOOM_LEVEL) zoomLevel--;
        zoom(zoomIn ? ZOOM_FACTOR : (float) ZOOM_FACTOR / 4, center);
    }

    private boolean validZoom(boolean zoomIn) {
        return (!zoomIn || zoomLevel != MAX_ZOOM_LEVEL) && (zoomIn || zoomLevel != MIN_ZOOM_LEVEL);
    }

    public void zoom(boolean zoomIn, int levels) {
        if(!validZoom(zoomIn)) return;
        int levelsToZoom = levels;

        if((zoomLevel + levelsToZoom) > MAX_ZOOM_LEVEL) {
            System.err.println("Warning: Trying to zoom in more than allowed! Setting to MAX zoom level.");
            levelsToZoom = MAX_ZOOM_LEVEL - zoomLevel;
            zoomLevel = MAX_ZOOM_LEVEL;
        }
        else if((zoomLevel + levelsToZoom) < MIN_ZOOM_LEVEL) {
            System.err.println("Warning: Trying to zoom out more than allowed! Setting to MIN zoom level.");
            levelsToZoom = MIN_ZOOM_LEVEL + zoomLevel;
            zoomLevel = MIN_ZOOM_LEVEL;
        }
        else zoomLevel += levels;

        double factor;
        if(zoomIn) factor = ZOOM_FACTOR;
        else factor = ZOOM_FACTOR / 4.0;

        for(int i = 0; i < Math.abs(levelsToZoom); i++) {
            zoom(factor, new Point2D(getWidth() / 2, getHeight() / 2));
        }
    }

    private void zoom(double factor, Point2D center) {
        trans.prependScale(factor, factor, center);
        updateMap();
        calculateRatio();
    }

    public void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        updateMap();
    }

    private void updateMap() {
        setBounds();
        mapData.searchInData(bounds);
        repaint();
    }

    public void reset() {
        trans = new Affine();
        zoomLevel = MIN_ZOOM_LEVEL;
        setBounds();
        resetView();
    }

    public CanvasBounds getBounds() {
        return bounds;
    }

    public void setBounds() {
        Point2D startCoords = getTransCoords(0, 0);
        bounds.setMinX((float) startCoords.getX());
        bounds.setMinY((float) startCoords.getY());

        Point2D endCoords = getTransCoords(getWidth(), getHeight());
        bounds.setMaxX((float) endCoords.getX());
        bounds.setMaxY((float) endCoords.getY());
    }

    public Point2D getTransCoords(double x, double y) {
        try {
            return trans.inverseTransform(x, y);
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Point2D getGeoCoords(double x, double y) {
        Point2D geoCoords = getTransCoords(x, y);

        return new Point2D(geoCoords.getX(), convertToGeo(geoCoords.getY()));
    }

    private double convertToGeo(double value) {
        return -value * 0.56f;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
        repaint();
    }

    public StringProperty getRatio() {
        return ratio;
    }

    private void resetView() {
        double mapWidth = mapData.getMaxX() - mapData.getMinX();             //Calculate the width of the loaded map's bounding box
        double boundsWidth = bounds.getMaxX() - bounds.getMinX();            //Calculate the width of the view's bounding box
        double minXMap = bounds.getMinX() + ((boundsWidth - mapWidth) / 2);  //Calculate the new x-coordinate of the map's bounding box centered in the view's.

        double mapHeight = mapData.getMaxY() - mapData.getMinY();
        double boundsHeight = bounds.getMaxY() - bounds.getMinY();
        double minYMap = bounds.getMinY() + ((boundsHeight - mapHeight) / 2);

        double dx = (minXMap - mapData.getMinX());                           //Calculate the difference between the two bounding boxes min x-coordinate
        double dy = (minYMap - mapData.getMinY());

        double zoom = getWidth() / (mapData.getMaxX() - mapData.getMinX()); //Get the scale for the view to show all of the map
        int levels = (int) (Math.log(zoom) / Math.log(2));                  //Calculate amount of levels to zoom in

        pan(dx, dy);
        zoom(true, levels);
    }

    public void rTreeDebugMode() {
        mapData.searchInData(bounds);
        repaint();
    }

    private static class StrokeFactory {
        private static final String NORMAL = "normal";
        private static final String DOTTED = "dotted";

        public static double getStrokeStyle(String stroke, Affine trans) {
            int pattern = 0;

            if(stroke.equals(NORMAL)) return pattern;
            else if(stroke.equals(DOTTED)) pattern = 3;
            else {
                System.err.println("Warning: Style '" + stroke + "' is not supported. Returning default.");
                return pattern;
            }

            return (pattern / Math.sqrt(trans.determinant()));
        }

        public static double getStrokeWidth(int width, Affine trans) {
            return (width / Math.sqrt(trans.determinant()));
        }
    }
}
