package bfst21.view;

import bfst21.MapData;
import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;
import bfst21.utils.MapMath;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.FillRule;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

import java.util.Map;

public class MapCanvas extends Canvas {
    public final static byte MIN_ZOOM_LEVEL = 1;
    public final static byte MAX_ZOOM_LEVEL = 19;
    public static Map<String, Byte> zoomMap;
    private final int ZOOM_FACTOR = 2;
    private final CanvasBounds bounds;
    private final StringProperty ratio;
    private final Affine trans;
    private byte zoomLevel = MIN_ZOOM_LEVEL;
    private MapData mapData;
    private Theme theme;
    private boolean initialized;

    public MapCanvas() {
        trans = new Affine();
        ratio = new SimpleStringProperty("- - -");
        bounds = new CanvasBounds();
    }

    public void init(MapData mapData) {
        this.mapData = mapData;

        if (!initialized) {
            widthProperty().addListener((observable, oldValue, newValue) -> pan((newValue.doubleValue() - oldValue.doubleValue()) / 2, 0));
            heightProperty().addListener((observable, oldValue, newValue) -> pan(0, (newValue.doubleValue() - oldValue.doubleValue()) / 2));
            initialized = true;
        }

        updateMap();
    }

    /**
     * Initializes the default theme and creates a zoom map for each theme element.
     * This method may only be called once!
     *
     * @param theme the default theme to create a zoom map from.
     */
    public void initTheme(Theme theme) {
        this.theme = theme;
        zoomMap = theme.createZoomMap();
    }

    public void repaint() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save();
        gc.setTransform(new Affine());

        gc.setFill(theme.get("background").getColor().getInner());
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setTransform(trans);
        fillCoastLines(gc);

        int layers = mapData.getMapSegment().size();
        for (int layer = 0; layer < layers - 1; layer++) { // layers - 1 since top layer is drawn below
            for (Element element : mapData.getMapSegment().get(layer)) {
                drawElement(gc, element);
            }
        }
        for (Element element : mapData.getMapSegment().get(mapData.getMapSegment().size() - 1)) {   //top layer is only text
            drawText(gc, element);
        }

        for (Element route : mapData.getCurrentRoute()) {
            if (theme.get(route.getType()).isNode()) drawRoundNode(gc, route);
            else drawElement(gc, route);
        }

        for (Node point : mapData.getUserAddedPoints()) {
            drawRectangleNode(gc, point);
        }

        if (mapData.getUserSearchResult() != null) {
            drawRectangleNode(gc, mapData.getUserSearchResult());
        }

        gc.restore();
    }

    private void fillCoastLines(GraphicsContext gc) {
        drawElement(gc, mapData.getCoastlines());
    }

    private void drawElement(GraphicsContext gc, Element element) {
        Theme.ThemeElement themeElement = theme.get(element.getType());

        gc.setLineDashes(getStrokeStyle(themeElement)); //Apply stroke style

        if (themeElement.isTwoColored()) {
            drawOuterElement(gc, element, themeElement);
        }
        drawInnerElement(gc, element, themeElement);

        if (themeElement.fill()) {
            fillElement(gc, themeElement);
        }
    }

    private void drawText(GraphicsContext gc, Element element) {
        Theme.ThemeElement themeElement = theme.get(element.getType());
        String text = mapData.getTextFromElement(element);
        gc.setTextAlign(TextAlignment.CENTER);
        Font font = new Font(themeElement.getOuterWidth() / Math.sqrt(trans.determinant()));
        gc.setFont(font);

        gc.setFill(themeElement.getColor().getInner());
        gc.fillText(text, element.getxMax(), element.getyMax());
    }

    private void drawRectangleNode(GraphicsContext gc, Node point) {
        Theme.ThemeElement themeElement = theme.get(point.getType());
        double length = (themeElement.getInnerWidth() / Math.sqrt(trans.determinant()));
        gc.setFill(themeElement.getColor().getInner());
        gc.fillRect(point.getxMax(), point.getyMax(), length, length);
        gc.setStroke(themeElement.getColor().getOuter());
        gc.strokeRect(point.getxMax(), point.getyMax(), length, length);

    }

    private void drawRoundNode(GraphicsContext gc, Element element) {
        Theme.ThemeElement themeElement = theme.get(element.getType());
        double innerRadius = (themeElement.getInnerWidth() / Math.sqrt(trans.determinant()));
        double outerRadius = (themeElement.getOuterWidth() / Math.sqrt(trans.determinant()));
        gc.setFill(themeElement.getColor().getInner());
        gc.fillOval(element.getxMax(), element.getyMax(), innerRadius, innerRadius);
        gc.setStroke(themeElement.getColor().getOuter());
        gc.strokeOval(element.getxMax(), element.getyMax(), outerRadius, outerRadius);
    }

    private void drawOuterElement(GraphicsContext gc, Element element, Theme.ThemeElement themeElement) {
        gc.setLineWidth(getStrokeWidth(false, themeElement));
        gc.setStroke(themeElement.getColor().getOuter());
        gc.beginPath();
        element.draw(gc);
        gc.stroke();
    }

    private void drawInnerElement(GraphicsContext gc, Element element, Theme.ThemeElement themeElement) {
        gc.setLineWidth(getStrokeWidth(true, themeElement));
        gc.setStroke(themeElement.getColor().getInner());   //Get and apply line color
        gc.beginPath();
        element.draw(gc);
        gc.stroke();
    }

    private void fillElement(GraphicsContext gc, Theme.ThemeElement themeElement) {
        gc.setFill(themeElement.getColor().getInner());
        gc.setFillRule(FillRule.EVEN_ODD);
        gc.fill();
    }

    private double[] getStrokeStyle(Theme.ThemeElement themeElement) {
        double[] strokeStyle = new double[2];
        for (int i = 0; i < strokeStyle.length; i++)
            strokeStyle[i] = StrokeFactory.getStrokeStyle(themeElement.getStyle(), trans);
        return strokeStyle;
    }

    private double getStrokeWidth(boolean inner, Theme.ThemeElement themeElement) {
        if (inner) return StrokeFactory.getStrokeWidth(themeElement.getInnerWidth(), trans);
        return StrokeFactory.getStrokeWidth(themeElement.getOuterWidth(), trans);
    }

    private void calculateRatio() {
        Point2D start = new Point2D(bounds.getMinX(), bounds.getMinY());
        Point2D end = new Point2D(bounds.getMaxX(), bounds.getMinY());

        double distance = MapMath.distanceBetween(start, end);
        double pixels = getWidth();
        double dPerPixel = distance / pixels;
        int scale = (int) (dPerPixel * 50);
        String toDisplay = (scale >= 1000) ? (scale / 1000) + " km" : scale + " m";
        ratio.set(toDisplay);
    }

    public void zoom(boolean zoomIn, Point2D center) {
        if (!isValidZoom(zoomIn)) return;
        if (zoomIn) zoomLevel++;
        else zoomLevel--;
        zoom(zoomIn ? ZOOM_FACTOR : ZOOM_FACTOR / 4.0, center);
        updateMap();
    }

    public void zoom(boolean zoomIn, int levels) {
        if (!isValidZoom(zoomIn) || levels == 0) return;
        int levelsToZoom = levels;

        if ((zoomLevel + levelsToZoom) > MAX_ZOOM_LEVEL) {
            System.err.println("Warning: Trying to zoom in more than allowed! Setting to MAX zoom level.");
            levelsToZoom = getLevelsToZoomForLevel(MAX_ZOOM_LEVEL);
            zoomLevel = MAX_ZOOM_LEVEL;
        } else if ((zoomLevel + levelsToZoom) < MIN_ZOOM_LEVEL) {
            System.err.println("Warning: Trying to zoom out more than allowed! Setting to MIN zoom level.");
            levelsToZoom = getLevelsToZoomForLevel(MIN_ZOOM_LEVEL);
            zoomLevel = MIN_ZOOM_LEVEL;
        } else zoomLevel += levels;

        double factor = zoomIn ? ZOOM_FACTOR : ZOOM_FACTOR / 4.0;

        for (int i = 0; i < Math.abs(levelsToZoom); i++) {
            zoom(factor, new Point2D(getWidth() / 2, getHeight() / 2));
        }

        updateMap();
    }

    private boolean isValidZoom(boolean zoomIn) {
        return (!zoomIn || zoomLevel != MAX_ZOOM_LEVEL) && (zoomIn || zoomLevel != MIN_ZOOM_LEVEL);
    }

    private void zoom(double factor, Point2D center) {
        trans.prependScale(factor, factor, center);
    }

    public void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        repaint();
    }

    public void updateMap() {
        setBounds();
        mapData.searchInRTree(bounds, zoomLevel);
        calculateRatio();
        repaint();
    }

    public void reset() {
        double mapWidth = Math.abs(mapData.getMinX() - mapData.getMaxX());                       //Calculate the width of the loaded map's bounding box
        double boundsWidth = bounds.getWidth();                                                  //Calculate the width of the view's bounding box
        double minXMap = bounds.getMinX() + ((boundsWidth - mapWidth) / 2);                      //Calculate the new x-coordinate of the map's bounding box centered in the view's.

        double mapHeight = Math.abs(mapData.getMinY() - mapData.getMaxY());
        double boundsHeight = bounds.getHeight();
        double minYMap = bounds.getMinY() + ((boundsHeight - mapHeight) / 2);

        double dx = (minXMap - mapData.getMinX()) * Math.sqrt(trans.determinant());              //Calculate the difference between the two bounding boxes min x-coordinate
        double dy = (minYMap - mapData.getMinY()) * Math.sqrt(trans.determinant());

        double zoom = getWidth() / (mapData.getMaxX() - mapData.getMinX()); //Get the scale for the view to show all of the map;
        if (mapHeight > mapWidth) zoom = getHeight() / (mapData.getMaxY() - mapData.getMinY());

        int levels = (int) (Math.log(zoom) / Math.log(ZOOM_FACTOR));        //Calculate amount of levels to zoom in

        pan(dx, dy);
        zoomToLevel(levels + 1);
    }

    public CanvasBounds getBounds() {
        return bounds;
    }

    public void setBounds() {
        Point2D startCoords = getTransCoords(0, 0);
        Point2D endCoords = getTransCoords(getWidth(), getHeight());
        bounds.setBounds((float) startCoords.getX(), (float) startCoords.getY(), (float) endCoords.getX(), (float) endCoords.getY());
    }

    public Point2D getTransCoords(double x, double y) {
        try {
            return trans.inverseTransform(x, y);
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }

        return null;
    }

    public byte getZoomLevel() {
        return zoomLevel;
    }

    public Theme getTheme() {
        return theme;
    }

    public void changeTheme(Theme theme) {
        if (initialized) {
            this.theme = theme;
            repaint();
        } else this.theme = theme;
    }

    public StringProperty getRatio() {
        return ratio;
    }

    private byte getLevelsToZoomForLevel(int level) {
        if (level > MAX_ZOOM_LEVEL || level < MIN_ZOOM_LEVEL || zoomLevel == level) return 0;
        return (byte) (level - zoomLevel);
    }

    private void zoomToLevel(int levels) {
        int levelToZoomTo = getLevelsToZoomForLevel(levels);
        if (levelToZoomTo != 0) zoom(levelToZoomTo > 0, levelToZoomTo);
        else updateMap();
    }

    public void panToRoute(float[] boundingBoxRouteCoordinates) {
        double mapWidth = Math.abs(boundingBoxRouteCoordinates[1] - boundingBoxRouteCoordinates[0]);
        double boundsWidth = bounds.getWidth();
        double minXMap = bounds.getMinX() + ((boundsWidth - mapWidth) / 2);

        double mapHeight = Math.abs(boundingBoxRouteCoordinates[3] - boundingBoxRouteCoordinates[2]);
        double boundsHeight = bounds.getHeight();
        double minYMap = bounds.getMinY() + ((boundsHeight - mapHeight) / 2);

        double dx = (minXMap - boundingBoxRouteCoordinates[0]) * Math.sqrt(trans.determinant());
        double dy = (minYMap - boundingBoxRouteCoordinates[2]) * Math.sqrt(trans.determinant());

        double zoom = getWidth() / mapWidth;
        if (mapHeight > mapWidth) zoom = getHeight() / mapHeight;
        int levels = (int) (Math.log(zoom) / Math.log(ZOOM_FACTOR));

        pan(dx, dy);
        zoomToLevel(levels + 1);
    }

    public void centerOnPoint(double x, double y) {
        Point2D centerPoint = bounds.getCenter();

        double dx = (centerPoint.getX() - x) * Math.sqrt(trans.determinant());
        double dy = (centerPoint.getY() - y) * Math.sqrt(trans.determinant());

        pan(dx, dy);
        updateMap();
    }

    public void rTreeDebugMode() {
        mapData.searchInRTree(bounds, zoomLevel);
        repaint();
    }

    private static class StrokeFactory {
        private static final String NORMAL = "normal";
        private static final String DOTTED = "dotted";

        public static double getStrokeStyle(String stroke, Affine trans) {
            int pattern = 0;

            if (stroke.equals(NORMAL)) return pattern;
            else if (stroke.equals(DOTTED)) pattern = 3;
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