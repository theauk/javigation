package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BinarySearchTreeTest {

    private static BinarySearchTree<Node> binarySearchTree;

    @BeforeAll
    static void beforeAll() {
        binarySearchTree = new BinarySearchTree<>();
        Node node1 = new Node(5,150,250);
        Node node2 = new Node(6,153,230);
        Node node3 = new Node(7,1560, 1560);
        Node node4 = new Node(19,143,144);
        binarySearchTree.put(node1);
        binarySearchTree.put(node2);
        binarySearchTree.put(node3);
        binarySearchTree.put(node4);
}


@Test
void get() {
        Node node5 = new Node(24,155,144);
        binarySearchTree.put(node5);
    assertEquals(binarySearchTree.get(24), node5);
}
@Test
void getAnother(){
    binarySearchTree.get(5);
    Node node6 = new Node(20,143,144);
    binarySearchTree.put(node6);
        assertEquals(binarySearchTree.get(20), node6);
}

@Test
void NullPointerGet(){
        assertNull(binarySearchTree.get(4)); // there is not a node in the BST with the id of 4.

}

    @AfterAll
    static void afterAll() {
        binarySearchTree = null;
    }
    }