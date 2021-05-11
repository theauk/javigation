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
        addressTree.put(new Node(0, 0, 0), "København S", "Olymposvej", 2300, "50");
        addressTree.put(new Node(0, 0, 0), "Klarup", "Olymposvej", 9270, "50");

        node = new AddressTrieNode();
        node.setAddress(new Node(0, 0, 0), 2300, "olymposvej", "50");
        node.addHouseNumber(2300, new Node(1, 1, 1), "49");
        node.addHouseNumber(2300, new Node(2, 2, 2), "49a");
        node.addHouseNumber(2300, new Node(3, 3, 3), "48");
        node.addHouseNumber(9270, new Node(4, 4, 4), "50");
        node.addHouseNumber(9270, new Node(5, 5, 5), "49");
    }

    @Test
    public void testCreateAddressTrieNode() {

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

    @Test
    public void testAddressSizeForAddress() {
        int expected = node.getAddressesFor("5", 2300, "københavn s".toLowerCase()).size();
        assertEquals(1, expected);

        expected = node.getAddressesFor("4", 2300, "københavn s".toLowerCase()).size();
        assertEquals(3, expected);
    }

    private int getExpectedSizeHouseNumber(String houseNumber) {
        return node.getAddressFor(houseNumber).size();
    }

    @AfterAll
    public static void tearDown() {
        addressTree = null;
    }
}
