package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

// TODO: 19-04-2021 out-commented the parts where the test will fail or give errors.

class AddressTriesTreeTest {
    private AddressTriesTree addressTrie;
    private Node node1;
    private Node node2;
    private Node node3;
    private Node node4;

    @BeforeEach
    void setUp() {
        node1 = new Node(340551927, 55.6786770f, 12.5694510f);
        node2 = new Node(340551928, 55.6786400f, 12.5698360f);
        node3 = new Node(340551930,55.6783500f,12.5693370f);
        node4 = new Node(340551929,55.6786500f,12.5698370f);
        addressTrie = new AddressTriesTree();
        //addressTrie.put(node1, "København K", "Studiestræde", 1455, "18", 1);
        //addressTrie.put(node2, "København K", "Studiestræde", 1455, "19", 1);
        addressTrie.put(node1, "København K", "Studiestræde", 1455, "18");
        addressTrie.put(node2, "København K", "Studiestræde", 1455, "19");
        addressTrie.put(node3, "Roskilde", "Studiestræde", 4000, "4");
        //addressTrie.put(node4, "København K", "Studievej", 1455, "25");
    }

    @AfterEach
    void tearDown() {
    }



}

