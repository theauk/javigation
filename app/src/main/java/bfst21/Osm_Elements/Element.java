package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

public abstract class Element implements Spatializable {
    protected Long id;
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

    public void setType(String type){
        this.type = type;
    }

    public String getType(){
        return type;
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
