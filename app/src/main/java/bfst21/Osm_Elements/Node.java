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
        this.yMin = lat;
    }

    @Override
    public void draw(GraphicsContext gc) {

    }
}
