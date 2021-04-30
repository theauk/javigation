package bfst21.utils;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static bfst21.utils.MapMath.getClosestPointOnWayAsNode;
import static org.junit.jupiter.api.Assertions.*;

class MapMathTest {
    private Way w1;

    @BeforeEach
    void setUp() {
        w1 = new Way();
        Node n1 = new Node(1, 1);
        Node n2 = new Node(5, 5);
        w1.addNode(n1);
        w1.addNode(n2);
    }

    @Test
    void intersectionClosestPointTest() {
        float x = 5;
        float y = 3;
        Node intersectionNode = MapMath.getClosestPointOnWayAsNode(x, y, w1);
        assertEquals(4f, intersectionNode.getxMax());
        assertEquals(4f, intersectionNode.getyMax());
    }

    @AfterEach
    void tearDown() {

    }
}