package bfst21.view;

import bfst21.MapData;
import bfst21.Osm_Elements.Element;
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
    private boolean rTreeDebug;

    public void init(MapData mapData, Theme theme) {
        this.mapData = mapData;
        this.theme = theme;
        trans = new Affine();

        bounds = new CanvasBounds();

        widthProperty().addListener(((observable, oldValue, newValue) -> {
            setBounds();
            repaint();
        }));
        heightProperty().addListener((observable, oldValue, newValue) -> {
            setBounds();
            repaint();
        });

        mapData.searchInData(bounds);

        repaint();
    }

    public void repaint() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save();
        gc.setTransform(new Affine());

        gc.setFill(theme.get("background"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setTransform(trans);
        gc.setLineWidth(1 / Math.sqrt(trans.determinant()));

        for (Element element : mapData.getMapSegment()) {
            gc.setStroke(theme.get("coastline"));
            element.draw(gc);
        }

        gc.setStroke(Color.RED);
        gc.strokeLine(bounds.getMinX(), (bounds.getMaxY() + bounds.getMinY()) / 2, getWidth(), (bounds.getMaxY() + bounds.getMinY()) / 2);
        //gc.strokeLine((bounds.getMinX() + bounds.getMaxX()) / 2, bounds.getMinY(), (bounds.getMinX() + bounds.getMaxX()) / 2, getHeight());

        gc.restore();
    }

    public void zoom(double factor, Point2D center) {
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
        //pan(0, 0);
        startup();
        setBounds();
        mapData.searchInData(bounds);
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
}
