package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serial;
import java.io.Serializable;

public class Node extends Element implements Serializable {
    @Serial
    private static final long serialVersionUID = -2738011707251247970L;

    public Node(long id, float lon, float lat) {
        super(id);
        this.xMin = lon;
        this.xMax = lon;
        this.yMin = -lat / 0.56f;
        this.yMax = this.yMin;
    }

    public Node(long id, float lon, float lat, boolean nodeFromNode) {
        super(id);
        if (nodeFromNode) {
            this.xMin = lon;
            this.xMax = lon;
            this.yMin = lat;
            this.yMax = lat;
        }
    }

    public Node(float lon, float lat) { // TODO: 3/28/21 for Rtree debug mode where the y should not be converted
        super(0);
        this.xMin = lon;
        this.xMax = lon;
        this.yMin = lat;
        this.yMax = lat;
    }

    @Override
    public void draw(GraphicsContext gc) {

    }

    public void drawCircle() {

    }

    public void textOutput() {

    }
}
