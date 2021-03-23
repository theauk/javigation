package bfst21.view;

import bfst21.Map;
import bfst21.Osm_Elements.Element;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

public class MapCanvas extends Canvas
{
    private Map map;
    private Affine trans;
    private CanvasBounds bounds;
    private Theme theme;

    public void init(Map map, Theme theme)
    {
        this.map = map;
        this.theme = theme;
        trans = new Affine();

        bounds = new CanvasBounds();

        repaint();
    }

    public void repaint()
    {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save();
        gc.setTransform(new Affine());

        gc.setFill(theme.get("background"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setTransform(trans);
        gc.setLineWidth(1 / Math.sqrt(trans.determinant()));

        for(Element element: map.getMapData())
        {
            gc.setStroke(theme.get("coastline"));
            element.draw(gc);
        }

        gc.restore();
    }

    public void zoom(double factor, Point2D center)
    {
        trans.prependScale(factor, factor, center);
        setBounds();
        repaint();
    }

    public void pan(double dx, double dy)
    {
        trans.prependTranslation(dx, dy);
        setBounds();
        repaint();
    }

    public void reset()
    {
        trans = new Affine();
        pan(0, 0);
    }

    public CanvasBounds getBounds()
    {
        return bounds;
    }

    public void setBounds()
    {
        try {
            Point2D startCoords = getTransCoords(0, 0);
            bounds.setMinX(startCoords.getX());
            bounds.setMinY(startCoords.getY());

            Point2D endCoords = getTransCoords(0 + getWidth(), 0 + getHeight());
            bounds.setMaxX(endCoords.getX());
            bounds.setMaxY(endCoords.getY());
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }
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

    public void setTheme(Theme theme)
    {
        this.theme = theme;
        repaint();
    }

    public class CanvasBounds
    {
        private double minX;
        private double minY;
        private double maxX;
        private double maxY;

        public double getMinX()
        {
            return minX;
        }

        public void setMinX(double minX)
        {
            this.minX = minX;
        }

        public double getMinY()
        {
            return minY;
        }

        public void setMinY(double minY)
        {
            this.minY = minY;
        }

        public double getMaxX()
        {
            return maxX;
        }

        public void setMaxX(double maxX)
        {
            this.maxX = maxX;
        }

        public double getMaxY()
        {
            return maxY;
        }

        public void setMaxY(double maxY)
        {
            this.maxY = maxY;
        }
    }
}
