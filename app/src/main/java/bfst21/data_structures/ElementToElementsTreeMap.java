package bfst21.data_structures;


import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ElementToElementsTreeMap<Key extends Element,Value extends Element> {
    private TreeMap<Long, ArrayList<Value>> map;

    public ElementToElementsTreeMap() {
        map = new TreeMap<>();
    }

    public void put(Key key, Value val) {
        ArrayList<Value> newWays = new ArrayList<>();
        newWays.add(val);
        ArrayList<Value> result = map.put(key.getId(), newWays);

        if (result != null) {
            newWays.addAll(result);
            //result.add(way);
            //map.put(n.getId(), result);
        }
    }

    public void putAll(List<Key> keys, Value val) {
        for(Key key: keys){
            put(key,val);
        }
    }

    public ArrayList<Value> getWaysFromNode(Key key){
        return map.get(key.getId());
    }


}
