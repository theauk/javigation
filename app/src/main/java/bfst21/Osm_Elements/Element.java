package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serial;
import java.io.Serializable;

/**
 * Abstract class for the type elements that the map divides into.
 */
public abstract class Element implements Spatializable, Serializable {
    @Serial
    private static final long serialVersionUID = -5832865119529036880L;

    protected long id;
    protected float xMin;
    protected float yMin;

    protected String type;
    protected int layer;

    public Element() {

    }

    public Element(long id) {
        this.id = id;
        layer = 4; //default value;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setType(String type, int lay) {
        this.type = type;
        if (lay < 5) layer = lay;
    }

    public boolean hasType() {
        return type != null;

    }

    public abstract void draw(GraphicsContext gc);

    public float getxMin() {
        return xMin;
    }

    public float getxMax() {
        return xMin;
    }

    public float getyMin() {
        return yMin;
    }

    public float getyMax() {
        return yMin;
    }

    public float[] getCoordinates() {
        return new float[]{xMin, xMin, yMin, yMin};
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int lay) {
        if (lay < 5) layer = lay;
    }

}
