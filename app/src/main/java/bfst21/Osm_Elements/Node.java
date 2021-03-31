package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

public class Node extends Element {

    public Node(long id, float lon, float lat) {
        super(id);
        this.xMin = lon;
        this.xMax = lon;
        this.yMin = -lat / 0.56f;
        this.yMax = -lat / 0.56f;

    }

    public Node(long id, float lon, float lat, boolean nodeFromNode){
        super(id);
        if(nodeFromNode){
            this.xMin = lon;
            this.xMax = lon;
            this.yMin = lat;
            this.yMax = lat;
        }
    }

    @Override
    public void draw(GraphicsContext gc) {

    }
}
