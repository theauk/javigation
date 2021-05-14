package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddressTriesTreeTest {
    private static AddressTriesTree addressTrie;
    private static List<Node> nodes;
    private static List<String> streets;
    private static List<String> cities;
    private static List<Integer> postCodes;

    @BeforeAll
    public static void setUp() {
        addressTrie = new AddressTriesTree();
        nodes = new ArrayList<>();
        streets = new ArrayList<>();
        postCodes = new ArrayList<>();
        cities = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(AddressTrieNodeTest.class.getResourceAsStream("/tests/addresses.txt"), StandardCharsets.UTF_8))) {
            String line;
            int id = 0;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                String street = data[0].strip().toLowerCase();
                String number = data[1].strip().toLowerCase();
                int postCode = Integer.parseInt(data[2].strip());
                String city = data[3].strip().toLowerCase();
                Node node = new Node(id, id, id);

                if (!streets.contains(street)) streets.add(street);
                if (!postCodes.contains(postCode)) postCodes.add(postCode);
                if (!cities.contains(city)) cities.add(city);
                nodes.add(node);

                addressTrie.put(node, street, number, postCode, city);

                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(streets);
        Collections.sort(postCodes);
        Collections.sort(cities);
    }

    @Test
    public void allAddressesAreLoaded() {
        assertEquals(44, nodes.size());
    }

    @Test
    public void testAddressSearchPrefixReturnsCorrectAddress() {
        List<AddressTrieNode> nodes = getSearchResultFor("1");
        AddressTrieNode n1 = nodes.get(0);
        AddressTrieNode n2 = nodes.get(1);

        assertEquals("1. februarvej 999, 6070 christiansfeld", n1.getAddressesFor("999").get(0));
        assertEquals("10. februarvej 999, 6070 christiansfeld", n2.getAddressesFor("999").get(0));
    }

    private List<AddressTrieNode> getSearchResultFor(String prefix) {
        return addressTrie.searchWithPrefix(prefix);
    }

    @Test
    public void testPrefixSearchSizeWithNoResult() {
        assertEquals(0, getSearchResultSizeFor(" "));
        assertEquals(0, getSearchResultSizeFor("studiestræder"));
        assertEquals(0, getSearchResultSizeFor("Abenholst  Allé"));
        assertEquals(0, getSearchResultSizeFor(" falkevej"));
        assertEquals(0, getSearchResultSizeFor("falkevej "));
        assertEquals(0, getSearchResultSizeFor(" falkevej "));
    }

    @Test
    public void testPrefixSearchSizeWithAllResults() {
        assertEquals(39, getSearchResultSizeFor(""));
    }

    @Test
    public void testPrefixSearchSizeCharacterForCharacter() {
        assertEquals(3, getSearchResultSizeFor("s"));
        assertEquals(2, getSearchResultSizeFor("st"));
        assertEquals(2, getSearchResultSizeFor("stu"));
        assertEquals(2, getSearchResultSizeFor("stu"));
        assertEquals(2, getSearchResultSizeFor("stud"));
        assertEquals(2, getSearchResultSizeFor("studi"));
        assertEquals(2, getSearchResultSizeFor("studie"));
        assertEquals(1, getSearchResultSizeFor("studies"));
        assertEquals(1, getSearchResultSizeFor("studiev"));
    }

    @Test
    public void testPrefixSearchSizeWithRandomResults() {
        assertEquals(3, getSearchResultSizeFor("S"));
        assertEquals(3, getSearchResultSizeFor("s"));
        assertEquals(2, getSearchResultSizeFor("Studie"));
        assertEquals(2, getSearchResultSizeFor("studie"));
        assertEquals(2, getSearchResultSizeFor("StUdIe"));
        assertEquals(1, getSearchResultSizeFor("studies"));
        assertEquals(2, getSearchResultSizeFor("1"));
        assertEquals(1, getSearchResultSizeFor("10"));
        assertEquals(3, getSearchResultSizeFor("Æ"));
        assertEquals(1, getSearchResultSizeFor("Å"));
        assertEquals(1, getSearchResultSizeFor("gl."));
        assertEquals(1, getSearchResultSizeFor("GL."));
        assertEquals(1, getSearchResultSizeFor("gl. k"));
        assertEquals(1, getSearchResultSizeFor("Ærøvej"));
        assertEquals(1, getSearchResultSizeFor("Abenholst Allé"));
    }

    private int getSearchResultSizeFor(String prefix) {
        return addressTrie.searchWithPrefix(prefix).size();
    }

    @Test
    public void checkAllPostCodes() {
        List<Integer> triePostCodes = new ArrayList<>(AddressTriesTree.POSTCODE_TO_CITIES.keySet());
        Collections.sort(triePostCodes);

        for (int i = 0; i < postCodes.size(); i++) {
            assertEquals(postCodes.get(i), triePostCodes.get(i));
        }
    }

    @Test
    public void checkAllCities() {
        List<String> trieCities = new ArrayList<>(AddressTriesTree.POSTCODE_TO_CITIES.values());
        Collections.sort(trieCities);

        for (int i = 0; i < cities.size(); i++) {
            assertEquals(cities.get(i), trieCities.get(i));
        }
    }

    @Test
    public void checkAllStreets() {
        List<AddressTrieNode> trieStreets = addressTrie.searchWithPrefix("");

        for (int i = 0; i < streets.size(); i++) {
            assertEquals(streets.get(i), trieStreets.get(i).getStreetName());
        }
    }

    @AfterAll
    public static void tearDown() {
        addressTrie = null;
        AddressTriesTree.POSTCODE_TO_CITIES.clear();
    }
}

