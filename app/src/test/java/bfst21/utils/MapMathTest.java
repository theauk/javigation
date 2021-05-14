package bfst21.utils;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapMathTest {

    @Test
    void colonTimeHoursTest() {
        String time1 = "10:30";
        double hours1 = MapMath.colonTimeToHours(time1);
        assertEquals(10.5, hours1);

        String time2 = "01:15";
        double hours2 = MapMath.colonTimeToHours(time2);
        assertEquals(1.25, hours2);
    }

    @Test
    void getTotalDistanceTest() {
        ArrayList<Node> nodes = new ArrayList<>();
        Node n1 = new Node(1, 10.43807f, (float) (55.21723 / 0.56));
        Node n2 = new Node(2, 10.44738f, (float) (55.21665 / 0.56));
        Node n3 = new Node(3, 10.44920f, (float) (55.21656 / 0.56));
        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);
        assertEquals(0.71, MapMath.round(MapMath.getTotalDistance(nodes), 2));
    }

    @Test
    void shortestDistanceToElementTest() {
        Way way = new Way();
        Node n1 = new Node(1, 1, 1);
        Node n2 = new Node(2, 3, 1);
        way.addNode(n1);
        way.addNode(n2);
        assertEquals(1.0, MapMath.shortestDistanceToElement(2, 0, way));
    }

    @Test
    void intersectionClosestPointTest() {
        Way w1 = new Way();
        Node n1 = new Node(0, 1, 0);
        Node n2 = new Node(0, 3, 2);
        w1.addNode(n1);
        w1.addNode(n2);
        float x = 3;
        float y = 0;
        Node intersectionNode = MapMath.getClosestPointOnWayAsNode(x, y, w1);
        assertEquals(2f, intersectionNode.getxMax());
        assertEquals(1f, intersectionNode.getyMax());
    }

    @Test
    void formatDistanceTest() {
        String mTest = MapMath.formatDistance(867, 2);
        String kmTest = MapMath.formatDistance(1567, 3);
        assertEquals("867.0 m", mTest);
        assertEquals("1.567 km", kmTest);
    }

    @Test
    void formatTimeTest() {
        String secTest = MapMath.formatTime(40, 2);
        String minTest = MapMath.formatTime(225, 3);
        assertEquals("40.0 s", secTest);
        assertEquals("3.75 min", minTest);
    }

}