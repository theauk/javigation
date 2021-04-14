package bfst21.data_structures;


import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class NodeToWayMap {
    private TreeMap<Long, ArrayList<Way>> map;

    public NodeToWayMap() {
        map = new TreeMap<>();
    }

    public void put(Node n, Way way) {
        ArrayList<Way> newWays = new ArrayList<>();
        newWays.add(way);
        ArrayList<Way> result = map.put(n.getId(), newWays);

        if (result != null) {
            newWays.addAll(result);
            //result.add(way);
            //map.put(n.getId(), result);
        }
    }

    public void putAll(List<Node> nodes, Way way) {
        for(Node node: nodes){
            put(node,way);
        }
    }

    public ArrayList<Way> getWaysFromNode(Node node){
        return map.get(node.getId());
    }


}
