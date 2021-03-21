package bfst21.osm_elements;

public class Node extends Element {
    private float x, y;
    private transient long id;

    public Node(long id, float lon, float lat) {
        this.id = id;
        this.x = lon;
        //this.y = -lat/0.56f;
        this.y = lat;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public long getID() {
        return id;
    }

}
