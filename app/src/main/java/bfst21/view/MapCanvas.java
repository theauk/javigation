package bfst21.view;

import bfst21.Map;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

public class MapCanvas extends Canvas
{
    private Map map;
    private Affine trans;

    public void init(Map map)
    {
        this.map = map;
        trans = new Affine();

        widthProperty().addListener(observable -> repaint());
        heightProperty().addListener(observable -> repaint());

        repaint();
    }

    public void repaint()
    {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save();
        gc.setTransform(new Affine());

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setTransform(trans);
        gc.setLineWidth(1 / Math.sqrt(trans.determinant()));

        for (var element: map.getMapData()) {
            gc.setStroke(Color.BLACK);
            element.draw(gc);
        }

        gc.restore();
    }

    public void zoom(double factor, Point2D center)
    {
        trans.prependScale(factor, factor, center);
        repaint();
    }

    public void pan(double dx, double dy)
    {
        trans.prependTranslation(dx, dy);
        repaint();
    }

    public void reset()
    {
        trans = new Affine();
        pan(0, 0);
    }

    public Point2D getTransCoords(double x, double y) throws NonInvertibleTransformException
    {
        return trans.inverseTransform(x, y);
    }

    public Point2D getGeoCoords(double x, double y) throws NonInvertibleTransformException
    {
        Point2D geoCoords = getTransCoords(x, y);

        return new Point2D(geoCoords.getX(), -geoCoords.getY() * 0.56f);
    }
}
