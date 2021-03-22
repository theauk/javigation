package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.NodeHolder;
import bfst21.Osm_Elements.Way;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static java.lang.Float.NaN;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RTreeTest {

    private RTree rTree;
    private Way w1, w2, w3, w4, w5, w6;

    @BeforeEach
    void setUp() {

        rTree = new RTree(1, 2, 4);

        w1 = new Way(1);
        Node n1 = new Node(1, -1, 2);
        //Node n2 = new Node(2, 3, 4);
        w1.addNode(n1);
        //w1.addNode(n2);

        w2 = new Way(2);
        Node n3 = new Node(3, 6, 4);
        //Node n4 = new Node(4, 12, 1);
        w2.addNode(n3);
        //w2.addNode(n4);

        w3 = new Way(3);
        Node n5 = new Node(5, 3, 1);
        //Node n6 = new Node(6, 9, 4);
        w3.addNode(n5);
        //w3.addNode(n6);

        w4 = new Way(4);
        Node n7 = new Node(7, 10, 3);
        //Node n8 = new Node(8, 20, 14);
        w4.addNode(n7);
        //w4.addNode(n8);

        w5 = new Way(5);
        Node n9 = new Node(9, -5, 7);
        //Node n10 = new Node(10, 20, 14);
        w5.addNode(n9);
        //w4.addNode(n10);

        w6 = new Way(6);
        Node n11 = new Node(11, 17, 35);
        //Node 12 = new Node(12, 20, 14);
        w6.addNode(n11);
        //w4.addNode(n12);

    }

    @Test
    void searchTest() {

        RTreeNode r1 = new RTreeNode(w1.getCoordinates(), false, 0, 1, null, 0);
        RTreeNode r2 = new RTreeNode(w2.getCoordinates(), false, 0, 1, r1, 1);
        r1.addChild(r2);
        RTreeNode r3 = new RTreeNode(w3.getCoordinates(), true, 0, 1, r2, 2);
        r2.addChild(r3);

        rTree.setRoot(r1);

        r1.addNodeHolderEntry(w1);
        r2.addNodeHolderEntry(w2);
        r3.addNodeHolderEntry(w3);

        System.out.println(rTree.search(6, 10, 0, -5 / 0.56f).size());
        /*assertEquals(2, rTree.search(6, 10, 0, -5/0.56f).size());
        assertEquals(3, rTree.search(0, 10, 0, -10/0.56f).size());
        assertEquals(0, rTree.search(10, 20, -10/0.56f, -20/0.56f).size());
        assertEquals(1, rTree.search(0,2, -2/0.56f, -3/0.56f).size());
        assertEquals(1, rTree.search(1,3, -2/0.56f, -4/0.56f).size());
        assertEquals(0, rTree.search(1,3, -4/0.56f, -9/0.56f).size());*/
    }

    @Test
    void insertNoOverFlowRootTest() {

        rTree.insert(w1);
        System.out.println("root cor: " + Arrays.toString(rTree.getRoot().getCoordinates()));

        System.out.println("");
        System.out.println("--SECOND--");
        rTree.insert(w2);
        System.out.println("Current root coordinates after insert: " + Arrays.toString(rTree.getRoot().getCoordinates()));
        System.out.println(rTree.getRoot().getChildren().size());

        System.out.println("");
        System.out.println("--THIRD--");
        rTree.insert(w3);
        System.out.println("root cor: " + Arrays.toString(rTree.getRoot().getCoordinates()));
        System.out.println(rTree.getRoot().getChildren().size());

        System.out.println("");
        System.out.println("--FOURTH--");
        rTree.insert(w4);

        rTree.printTree();

        System.out.println("");
        System.out.println("--FIFTH--");
        rTree.insert(w5);

        /*System.out.println("");
        System.out.println("--SIXTH--");
        rTree.insert(w6);*/

        /*float[] rootCoordinates = rTree.getRoot().getCoordinates();
        assertEquals(1.0, rootCoordinates[0]);
        assertEquals(10.0, rootCoordinates[1]);
        assertEquals(2, rTree.getRoot().getChildren().size());*/

        System.out.println("");
        System.out.println("Final root coordinates: " + Arrays.toString(rTree.getRoot().getCoordinates()));

        System.out.println("");

        rTree.printTree();
    }
}