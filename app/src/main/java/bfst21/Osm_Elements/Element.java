package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

public abstract class Element implements Spatializable {
    protected long id;
    protected float xMin;
    protected float xMax;
    protected float yMin;
    protected float yMax;
    private String type;

    public Element(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

}
