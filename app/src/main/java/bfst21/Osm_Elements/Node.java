package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

public class Node extends Element {

    public Node(long id, float lon, float lat) {
        super(id);
        this.xMin = lon;
        this.xMax = lon;
        this.yMin = -lat/0.56f;
        this.yMax = -lat/0.56f;
    }

    @Override
    public void draw(GraphicsContext gc) {

    }
}
