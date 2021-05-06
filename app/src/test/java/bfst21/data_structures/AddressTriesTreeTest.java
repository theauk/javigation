package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO: 19-04-2021 out-commented the parts where the test will fail or give errors.
//Test for indsættelse af duplikerede adresser.
//Test for indsættelse af empty string.

class AddressTriesTreeTest {
    private AddressTriesTree addressTrie;
    private List<Node> nodes;
    private List<String> streets;
    private List<String> cities;
    private List<Integer> postCodes;

    @BeforeEach
    void setUp() {
        addressTrie = new AddressTriesTree();
        nodes = new ArrayList<>();
        streets = new ArrayList<>();
        postCodes = new ArrayList<>();
        cities = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/tests/addresses.txt")))) {
            String line;
            int id = 0;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String street = data[0].strip().toLowerCase();
                String number = data[1].strip().toLowerCase();
                int postCode = Integer.parseInt(data[2].strip());
                String city = data[3].strip().toLowerCase();
                Node node = new Node(id, id, id);

                streets.add(street);
                postCodes.add(postCode);
                cities.add(city);
                nodes.add(node);

                addressTrie.put(node, city, street, postCode, number);

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
    public void testSearchResultSize() {
        List<AddressTrieNode> suggestions = addressTrie.searchWithPrefix("S");
        assertEquals(4, suggestions.size());
    }

    @Test
    public void testSearchPrefixNoResult() {
        List<AddressTrieNode> suggestions = addressTrie.searchWithPrefix("studiestræder");
        assertEquals(0, suggestions.size());
    }

    @Test
    public void testSearchPrefix() {
        List<AddressTrieNode> suggestions = addressTrie.searchWithPrefix("S");

        for(AddressTrieNode node: suggestions) {

        }
    }

    @Test
    public void testNodeForSearchResult() {

    }

    @Test
    public void allPostCodesAreLoaded() {
        for(int i = 0; i < postCodes.size(); i++) {
            assertEquals(postCodes.get(i), addressTrie.getPostCodes().get(i));
        }
    }

    @Test
    public void allCitiesAreLoaded() {
        for(int i = 0; i < cities.size(); i++) {
            assertEquals(cities.get(i), addressTrie.getCities().get(i));
        }
    }

    @AfterEach
    void tearDown() {
        addressTrie = null;
    }
}

