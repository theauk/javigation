package bfst21.data_structures;

import bfst21.Osm_Elements.Element;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * TreeMap which places Values with the same Key in the same Value ArrayList.
 * Sorts the tree based on the key ID.
 *
 * @param <Key>   Key that extends element.
 * @param <Value> Value that extends element
 */
public class ElementToElementsTreeMap<Key extends Element, Value extends Element> implements Serializable {
    @Serial
    private static final long serialVersionUID = 7599232655577398280L;

    private final TreeMap<Long, ArrayList<Value>> map;

    public ElementToElementsTreeMap() {
        map = new TreeMap<>();
    }

    public void put(Key key, Value val) {
        ArrayList<Value> newWays = new ArrayList<>();
        newWays.add(val);
        ArrayList<Value> result = map.put(key.getId(), newWays);

        if (result != null) {
            newWays.addAll(result);
        }
    }

    public void putAll(List<Key> keys, Value val) {
        for (Key key : keys) {
            put(key, val);
        }
    }

    public ArrayList<Value> getElementsFromKeyElement(Key key) {
        return map.get(key.getId());
    }

}
