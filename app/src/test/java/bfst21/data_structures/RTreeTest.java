package bfst21.data_structures;

import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RTreeTest {

    private static RTree rTree;
    private Way w1, w2, w3, w4, w5, w6, w7;
    private Node n;

    @BeforeEach
    void setUp() {

        rTree = new RTree(1, 3, 4);

        w1 = new Way(1);
        Node n1 = new Node(1, 2, 3.5f);
        Node n2 = new Node(2, 4, 3.5f);
        Node n3 = new Node(3, 4, 2);
        w1.addNode(n1);
        w1.addNode(n2);
        w1.addNode(n3);
        w1.setAsHighWay();
        w1.setName("w1");

        w2 = new Way(2);
        Node n4 = new Node(4, 1.5f, 3);
        Node n5 = new Node(5, 4, 1);
        w2.addNode(n4);
        w2.addNode(n5);
        w2.setAsHighWay();
        w2.setName("w2");

        w3 = new Way(3);
        n = new Node(5, 3, 2.5f);
        Node n6 = new Node(6, 9, 4);
        w3.addNode(n);
        w3.addNode(n6);

        w4 = new Way(4);
        Node n7 = new Node(7, 10, 3);
        Node n8 = new Node(8, 20, 14);
        w4.addNode(n7);
        w4.addNode(n8);

        w5 = new Way(5);
        Node n9 = new Node(9, 5, 7);
        Node n10 = new Node(10, 14, 14);
        w5.addNode(n9);
        w5.addNode(n10);

        w6 = new Way(6);
        Node n11 = new Node(11, 12, 12);
        Node n12 = new Node(12, 10, 10);
        w6.addNode(n11);
        w6.addNode(n12);

        w7 = new Way(7);
        Node n13 = new Node(13, 9, 9);
        Node n14 = new Node(12, 9, 7);
        w7.addNode(n13);
        w7.addNode(n14);

        for (int i = -10; i < 0; i++) {
            for (int j = -10; j < 0; j++) {
                Node node = new Node(0, i, j);
                Way way = new Way();
                way.addNode(node);
                rTree.insert(way);
            }
        }
        rTree.insert(w1);
        rTree.insert(w5);
        rTree.insert(w2);
        rTree.insert(w6);
        rTree.insert(w4);
        rTree.insert(w3);
    }

    @Test
    void getNearestRoad() {
        RTree.NearestRoadPriorityQueueEntry w = rTree.getNearestRoad(n.getxMax(), n.getyMax(), null);
        assertEquals("w2", w.getWay().getName());
    }

    @Test
    void mapSegmentTest() {
        ArrayList<ArrayList<Element>> result = rTree.search(1, 10, 1, 6, false, getCleanArrayList());
        assertEquals(3, elementsInList(result));
    }

    @Test
    void mapSegmentDebug() {
        ArrayList<ArrayList<Element>> result = rTree.search(1, 10, 1, 6, true, getCleanArrayList());

        int numberOfDebugRectangleWays = 4;
        int numberOfElements = 3;
        int numberOfBoundingRectangleWaysToElement = 4;
        int amount = numberOfDebugRectangleWays + numberOfElements + numberOfElements * numberOfBoundingRectangleWaysToElement;

        assertEquals(amount, elementsInList(result));
    }

    @Test
    void emptyMapSegment() {
        ArrayList<ArrayList<Element>> result = rTree.search(13, 14, 1, 2, false, getCleanArrayList());
        assertEquals(0, elementsInList(result));
    }

    private ArrayList<ArrayList<Element>> getCleanArrayList() {
        ArrayList<ArrayList<Element>> results = new ArrayList<>();
        while (results.size() <= 19) {
            results.add(new ArrayList<>());
        }
        return results;
    }

    private int elementsInList(ArrayList<ArrayList<Element>> lists) {
        int i = 0;
        for (ArrayList<Element> list : lists) {
            i += list.size();
        }
        return i;
    }

    @AfterAll
    static void afterAll() {
        rTree = null;
    }

}