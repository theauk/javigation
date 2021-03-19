package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

public class Node extends Element {
    private float x, y;

    public Node(long id, float lon, float lat) {
        super(id);
        this.x = lon;
        this.y = -lat/0.56f;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override // Need this for drawing stuff for relation (etc. busstop) or a single tree.
    public void draw(GraphicsContext gc) {

    }
}
