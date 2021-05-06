package bfst21.data_structures;

import bfst21.Exceptions.KDTreeEmptyException;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KDTreeTest {
    private static KDTree<Node> tree;

    @BeforeEach
    void setUp() {
        tree = new KDTree<>(2, 4);
    }

    @Test
    void searchGetNearestNodeTest() throws bfst21.Exceptions.KDTreeEmptyException {
        List<Node> list = new ArrayList<>();
        Node node1 = new Node(1, (float) 12.5972367, (float) 55.6952510);
        Node node2 = new Node(2, (float) 12.5991499, (float) 55.6951851);
        Node node3 = new Node(3, (float) 12.6002220, (float) 55.6961339);
        Node node4 = new Node(4, (float) 12.5823799, (float) 55.6685247);
        list.add(node1);
        list.add(node2);
        list.add(node3);
        list.add(node4);

        tree.addAll(list);
        tree.buildTree();


        Node node = new Node(5, (float) 12.5823800, (float) 55.6685260);
        assertEquals(node4, tree.getNearestNode(node.getxMax(), node.getyMax()));
    }

    @Test
        // throws Expection
    void throwKDTreeExpection() {
        Node node = new Node(5, (float) 12.5823800, (float) 55.6685260);
        KDTreeEmptyException e = assertThrows(KDTreeEmptyException.class, () -> {
            tree.getNearestNode(node.getxMax(), node.getyMax());
        });
    }


    @Test
    void WayAndGetNearestNodeTest() throws bfst21.Exceptions.KDTreeEmptyException {
        Node n1 = new Node(1, 1, 1);
        Node n2 = new Node(2, 2, 2);
        Node n3 = new Node(3, 3, 3);
        Node n4 = new Node(5, 5, 5);
        Way w1 = new Way(1);
        w1.addNode(n1);
        w1.addNode(n2);
        w1.addNode(n2);
        w1.addNode(n2);
        w1.addNode(n3);
        w1.addNode(n4);
        w1.addNode(n4);
        w1.addNode(n4);


        tree.addAll(w1.getNodes());
        tree.buildTree();
        Node n5 = new Node(4, 4, -4);
        assertEquals(n1, tree.getNearestNode(n5.getxMax(), n5.getyMax()));
    }

    @Test
    void GetNearestNode_isCoordinateLessThanTest() throws KDTreeEmptyException {
        List<Node> list = new ArrayList<>();
        Node n1 = new Node(1, 1, 1);
        Node n2 = new Node(2, 2, 2);
        Node n4 = new Node(5, 5, 5);
        Node n5 = new Node(6, 10, 10);
        list.add(n1);
        list.add(n2);
        list.add(n5);

        tree.addAll(list);
        tree.buildTree();

        assertEquals(n2, tree.getNearestNode(n4.getxMax(), n4.getyMax()));
    }


    @AfterAll
    static void afterAll() {
        tree = null;
    }
}
