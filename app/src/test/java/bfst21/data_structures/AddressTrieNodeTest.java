package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddressTrieNodeTest {

    private static AddressTrieNode node;
    private static AddressTriesTree addressTree;

    @BeforeAll
    public static void setUp() {
        addressTree = new AddressTriesTree();
        addressTree.put(new Node(0, 0, 0), "Olymposvej", "50", 2300, "København S");
        addressTree.put(new Node(0, 0, 0), "Olymposvej", "50", 9270, "Klarup");

        node = new AddressTrieNode();
        node.setAddress(new Node(0, 0, 0), "olymposvej", "50", 2300, "København s");
        node.addHouseNumber(new Node(1, 1, 1), "49", 2300, "københavn s");
        node.addHouseNumber(new Node(2, 2, 2), "49a", 2300, "københavn s");
        node.addHouseNumber(new Node(3, 3, 3), "48", 2300, "københavn S");
        node.addHouseNumber(new Node(4, 4, 4), "50", 9270, "klarup");
        node.addHouseNumber(new Node(5, 5, 5), "49", 9270, "klarup");
    }

    @Test
    public void testAddressSizeForHouseNumber() {
        int expected = getExpectedSizeHouseNumber("5");
        assertEquals(2, expected);

        expected = getExpectedSizeHouseNumber("4");
        assertEquals(4, expected);

        expected = getExpectedSizeHouseNumber("49");
        assertEquals(3, expected);
    }

    private int getExpectedSizeHouseNumber(String houseNumber) {
        return node.getAddressesFor(houseNumber).size();
    }

    @AfterAll
    public static void tearDown() {
        node = null;
        addressTree = null;
    }
}
