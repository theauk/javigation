package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serial;
import java.io.Serializable;

public abstract class Element implements Spatializable, Serializable {
    @Serial private static final long serialVersionUID = -5832865119529036880L;

    protected long id;
    protected float xMin;
    protected float xMax;
    protected float yMin;
    protected float yMax;
    protected String type;
    protected int layer;

    public Element(long id) {
        this.id = id;
        layer = 4; //default value;
    }

    public Element() {

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
        return xMax;
    }

    public float getyMin() {
        return yMin;
    }

    public float getyMax() {
        return yMax;
    }

    public float[] getCoordinates() {
        return new float[]{xMin, xMax, yMin, yMax};
    }

    public void setLayer(int lay) {
        if (lay < 5) layer = lay;
    }

    public int getLayer() {
        return layer;
    }

}
