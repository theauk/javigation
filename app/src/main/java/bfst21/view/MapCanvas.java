package bfst21.view;

import bfst21.MapData;
import bfst21.Osm_Elements.Element;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

public class MapCanvas extends Canvas {
    private MapData mapData;
    private Affine trans;
    private CanvasBounds bounds;
    private Theme theme;

    public void init(MapData mapData, Theme theme) {
        this.mapData = mapData;
        this.theme = theme;
        trans = new Affine();

        bounds = new CanvasBounds();

        widthProperty().addListener(((observable, oldValue, newValue) -> {
            setBounds();
            repaint();
            pan((newValue.floatValue() - oldValue.floatValue())/2,0);
        }));
        heightProperty().addListener((observable, oldValue, newValue) -> {
            setBounds();
            repaint();
            pan(0,(newValue.floatValue() - oldValue.floatValue())/2);
        });

        repaint();
    }

    public void repaint() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save();
        gc.setTransform(new Affine());

        gc.setFill(theme.get("background").getColor().getInner());
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setTransform(trans);

        for(Element element: mapData.getMapSegment())
        {
            drawElement(gc, element);
        }

        //gc.setStroke(Color.RED);
        //gc.strokeLine(bounds.getMinX(), (bounds.getMaxY() + bounds.getMinY()) / 2, getWidth(), (bounds.getMaxY() + bounds.getMinY()) / 2);
        //gc.strokeLine((bounds.getMinX() + bounds.getMaxX()) / 2, bounds.getMinY(), (bounds.getMinX() + bounds.getMaxX()) / 2, getHeight());

        gc.restore();
    }

    private void drawElement(GraphicsContext gc, Element element)
    {
        gc.setLineDashes(getStrokeStyle(element.getType())); //Apply stroke style

        if(theme.get(element.getType()).isTwoColored())
        {
            gc.setLineWidth(getStrokeWidth(element.getType(), false));
            gc.setStroke(theme.get(element.getType()).getColor().getOuter());
            element.draw(gc);
        }

        gc.setLineWidth(getStrokeWidth(element.getType(), true));
        gc.setStroke(theme.get(element.getType()).getColor().getInner());   //Get and apply line color
        element.draw(gc);

        if(theme.get(element.getType()).fill())
        {
            gc.setFill(theme.get(element.getType()).getColor().getInner());
            gc.fill();
        }
    }

    private double[] getStrokeStyle(String type)
    {
        double[] strokeStyle = new double[2];
        for(int i = 0; i < strokeStyle.length; i++) strokeStyle[i] = StrokeFactory.getStrokeStyle(theme.get(type).getStyle(), trans);
        return strokeStyle;
    }

    private double getStrokeWidth(String type, boolean inner)
    {
        if(inner) return StrokeFactory.getStrokeWidth(theme.get(type).getInnerWidth(), trans);
        return StrokeFactory.getStrokeWidth(theme.get(type).getOuterWidth(), trans);
    }

    public void zoom(double factor, Point2D center)
    {
        trans.prependScale(factor, factor, center);
        setBounds();
        mapData.searchInData(bounds);
        repaint();
    }

    public void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        setBounds();
        mapData.searchInData(bounds);
        repaint();
    }

    public void reset() {
        trans = new Affine();
        startup();
        setBounds();
        mapData.searchInData(bounds);
    }

    public void loadFile(MapData mapData) { // TODO: 4/1/21 Delete if not creating new MapData on load
        this.mapData = mapData;
        reset();
    }

    public CanvasBounds getBounds() {
        return bounds;
    }

    public void setBounds() {
        try {
            Point2D startCoords = getTransCoords(0, 0);
            bounds.setMinX((float) startCoords.getX());
            bounds.setMinY((float) startCoords.getY());

            Point2D endCoords = getTransCoords(0 + getWidth(), 0 + getHeight());
            bounds.setMaxX((float) endCoords.getX());
            bounds.setMaxY((float) endCoords.getY());
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    public Point2D getTransCoords(double x, double y) throws NonInvertibleTransformException {
        return trans.inverseTransform(x, y);
    }

    public Point2D getGeoCoords(double x, double y) throws NonInvertibleTransformException {
        Point2D geoCoords = getTransCoords(x, y);

        return new Point2D(geoCoords.getX(), -geoCoords.getY() * 0.56f);
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
        repaint();
    }

    public void startup() {
        pan(-mapData.getMinX(), -mapData.getMinY());
        zoom((getWidth() - 200) / (mapData.getMaxX() - mapData.getMinX()), new Point2D(-0.009127, -0.010532));
    }

    public void rTreeDebugMode() {
        mapData.searchInData(bounds);
        repaint();
    }

    private static class StrokeFactory
    {
        private static final String NORMAL = "normal";
        private static final String DOTTED = "dotted";

        public static double getStrokeStyle(String stroke, Affine trans)
        {
            int pattern = 0;

            if(stroke.equals(NORMAL)) return pattern;
            else if(stroke.equals(DOTTED)) pattern = 3;
            else
            {
                System.err.println("Warning: Style '" + stroke + "' is not supported. Returning default.");
                return pattern;
            }

            return (pattern / Math.sqrt(trans.determinant()));
        }

        public static double getStrokeWidth(int width, Affine trans)
        {
            return (width / Math.sqrt(trans.determinant()));
        }
    }
}
