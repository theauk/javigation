package bfst21.data_structures;

import bfst21.osm_elements.Node;
import bfst21.osm_elements.Way;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RTreeTest {

    @Test
    void searchTest() {
        RTree rTree = new RTree(1, 1, 2);

        Way e1 = new Way();
        Node n1 = new Node(1, 1, 2);
        Node n2 = new Node(2, 3, 4);
        e1.addNode(n1);
        e1.addNode(n2);

        Way e2 = new Way();
        Node n3 = new Node(3, 6, 4);
        Node n4 = new Node(4, 9, 1);
        e2.addNode(n3);
        e2.addNode(n4);

        Way e3 = new Way();
        Node n5 = new Node(5, 3, 1);
        Node n6 = new Node(6, 9, 4);
        e3.addNode(n5);
        e3.addNode(n6);

        RTreeNode r1 = new RTreeNode(e1.getCoordinates(), false, 0,  1, null);
        RTreeNode r2 = new RTreeNode(e2.getCoordinates(), false, 0, 1, r1);
        r1.addChild(r2);
        RTreeNode r3 = new RTreeNode(e3.getCoordinates(), true, 0, 1, r2);
        r2.addChild(r3);

        rTree.setRoot(r1);

        r1.addNodeHolderEntry(e1);
        r2.addNodeHolderEntry(e2);
        r3.addNodeHolderEntry(e3);

        System.out.println(rTree.search(6, 10, 0, 5).size());
        /*assertEquals(2, rTree.search(6, 10, 0, 5).size());
        assertEquals(3, rTree.search(0, 10, 0, 10).size());
        assertEquals(0, rTree.search(10, 20, 10, 20).size());
        assertEquals(1, rTree.search(0,2, 2, 3).size());
        assertEquals(1, rTree.search(1,3, 2, 4).size());
        assertEquals(0, rTree.search(1,3, 4, 9).size());*/
    }
}