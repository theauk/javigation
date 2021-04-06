package bfst21.Osm_Elements.Specifik_Elements;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import javafx.scene.canvas.GraphicsContext;

/**
 * A TravelWay is walkable, cycleable and driveable.
 */
public class TravelWay extends Way {


    public TravelWay(Way way, String roadType) {
        super(way.getId());
        super.addAllNodes(way.getNodes());
        super.setType(roadType);
        // default????

    }




}
