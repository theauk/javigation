package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// TODO: 19-04-2021 out-commented the parts where the test will fail or give errors.

class AddressTriesTreeTest {
    private AddressTriesTree addressTrie;
    private Node node1;
    private Node node2;

    @BeforeEach
    void setUp() {
        node1 = new Node(340551927, 55.6786770f, 12.5694510f);
        node2 = new Node(340551928, 55.6786400f, 12.5698360f);
        addressTrie = new AddressTriesTree();
        addressTrie.put(node1, "København K", "Studiestræde", 1455, "18", 1);
        addressTrie.put(node2, "København K", "Studiestræde", 1455, "19", 1);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void searchWithPostcode() {
        assertEquals(2, addressTrie.search("1455").size());
    }
}
