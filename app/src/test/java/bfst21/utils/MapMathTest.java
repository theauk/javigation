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
        Node n1 = new Node(1, 0);
        Node n2 = new Node(3, 2);
        w1.addNode(n1);
        w1.addNode(n2);
    }

    @Test
    void intersectionClosestPointTest() {
        float x = 3;
        float y = 0;
        Node intersectionNode = MapMath.getClosestPointOnWayAsNode(x, y, w1);
        assertEquals(2f, intersectionNode.getxMax());
        assertEquals(1f, intersectionNode.getyMax());
    }

    @AfterEach
    void tearDown() {

    }
}