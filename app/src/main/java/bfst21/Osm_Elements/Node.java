package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

public class Node extends Element {
    List<String> roadNames;

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

    public void addRoadname(String name) {
        if (roadNames == null) {
            roadNames = new ArrayList<>();
        }
        if (!roadNames.contains(name)) {
            // TODO: 30-03-2021 det her kan godt være en tidsluger
            roadNames.add(name);
        }

    }

    public List<String> getName() {
        return roadNames;
    }
}
