package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

public class Node extends Element{
    private List<Way> partOfWays;

    public Node(long id, float lon, float lat) {
        super(id);
        this.xMin = lon;
        this.xMax = lon;
        this.yMin = -lat/0.56f;
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
    public void addReferenceToHighWay(Way way){
        if(partOfWays == null){
         partOfWays = new ArrayList<>();
        }
        partOfWays.add(way);
    }

    public List<Way> getReferencedHighWays(){
        return partOfWays;
    }

    @Override
    public void draw(GraphicsContext gc) {

    }

}
