package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElementToElementsTreeMapTest {
    ElementToElementsTreeMap<Node, Way> map;

    @BeforeEach
    void setUp() {
        map = new ElementToElementsTreeMap<>();
    }

    @Test
    void arrayListPutTest() {
        Node n1 = new Node(0, 0, 0);
        Node n2 = new Node(10, 0, 0);
        Way w1 = new Way(1);
        Way w2 = new Way(2);
        map.put(n1, w1);
        map.put(n1, w2);
        map.put(n2, w1);
        assertEquals(2, map.getElementsFromNode(n1).size());
    }

    @AfterEach
    void tearDown() {
    }
}