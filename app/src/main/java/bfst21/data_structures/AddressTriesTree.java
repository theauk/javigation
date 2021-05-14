package bfst21.data_structures;

import bfst21.Osm_Elements.Node;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class AddressTriesTree implements Serializable {
    public static final Map<Integer, String> POSTCODE_TO_CITIES = new HashMap<>();
    @Serial
    private static final long serialVersionUID = 5713923887785799744L;
    private final AddressTrieNode root;

    public AddressTriesTree() {
        root = new AddressTrieNode();
    }

    /**
     * Calls insert that inserts the node into the trie via its street name and city.
     *
     * @param node         contains the coordinates for the address.
     * @param city         the city which the address is located at.
     * @param streetName   the name of the street which the address belongs to.
     * @param postcode     The digit number that tells in what part of the country the address is located.
     *                    in Denmark it's how far they are from Copenhagen and are a 4-digit number
     * @param houseNumber the houseNumber that the address has.
     *
     */
    public void put(Node node, String streetName, String houseNumber, int postcode, String city) {
        insert(root, node, streetName, houseNumber, postcode, city);
    }

    /**
     * Sets streetName, houseNumber (some has letters in the houseNumber) and the city to lowercase before
     * calling the insert method <br>
     * Puts the post and city into a Hashmap as well to make it easier to search
     *
     * @param root the root of the TrieTree
     * @param node the node which is getting inserted
     * @param streetName the street name the given node has from the .osm file
     * @param houseNumber the house number the given node has from the .osm file
     * @param postcode the postcode the given node has from the .osm file
     * @param city the city the given node has from the .osm file
     */
    private void insert(AddressTrieNode root, Node node, String streetName, String houseNumber, int postcode, String city) {
        streetName = streetName.toLowerCase();
        houseNumber = houseNumber.toLowerCase();
        city = city.toLowerCase();
        POSTCODE_TO_CITIES.put(postcode, city);
        insertAddressWithStreetName(root, 0, node, streetName, houseNumber, postcode);
    }

    /**
     * This method is inspired by the method 'insert' from this post: https://stackoverflow.com/a/55470115 <br>
     * @param trieNode Root, otherwise a child.
     * @param index counts how many times the method has been called - stops when index is the same length as streetName
     * @param node the node that contains the coordinates for the address
     * @param streetName the name of the street given by the .osm file.
     * @param postcode the postcode of the node given by the .osm file.
     * @param houseNumber the house number given by the .osm file.
     */
    private void insertAddressWithStreetName(AddressTrieNode trieNode, int index, Node node, String streetName, String houseNumber, int postcode) {
        if (index == streetName.length()) {
            if (trieNode.isAddress()) trieNode.addHouseNumber(node, houseNumber, postcode);
            else trieNode.setAddress(node, streetName, houseNumber, postcode);
        } else {
            Character currentChar = streetName.charAt(index);
            if (!trieNode.getChildren().containsKey(currentChar)) {
                AddressTrieNode new_child = new AddressTrieNode();
                trieNode.getChildren().put(currentChar, new_child);
            }
            insertAddressWithStreetName(trieNode.getChildren().get(currentChar), index + 1, node, streetName, houseNumber, postcode);
        }
    }

    /**
     * Adapted from the Algorithms book by Sedgewick & Wayne.
     * The help methods are also adapted from the same book
     * @param prefix prefix to possible street names
     * @return  a list (ArrayList) with the possible street names that matches the prefix with help from the help methods.
     */
    public List<AddressTrieNode> searchWithPrefix(String prefix) {
        List<AddressTrieNode> queue = new ArrayList<>();
        prefix = prefix.toLowerCase();
        collect(get(root, prefix, 0), prefix, queue);
        Collections.sort(queue);
        return queue;
    }

    /**
     *
     * @param trieNode the trie node which the method is currently at.
     * @param key could also be called prefix. returns the given trie node
     * @param index counts how many times the method has been called
     * @return returns the trie node that matches the key.
     */
    private AddressTrieNode get(AddressTrieNode trieNode, String key, int index) {
        if (trieNode == null) return null;
        if (index == key.length()) {
            return trieNode;
        }
        char character = key.charAt(index);
        return get(trieNode.getChildren().get(character), key, index + 1);
    }

    /**
     *
     * The method finds a street name that matches the given prefix and add it to the list (queue).
     * @param trieNode the trie node the method is currently at.
     * @param prefix help method for searchWithPrefix adds street names to the list that begins with the given prefix
     * @param queue the list that searchWithPrefix uses that contains the possible street names
     */
    private void collect(AddressTrieNode trieNode, String prefix, List<AddressTrieNode> queue) {
        if (trieNode == null) return;
        if (trieNode.isAddress()) {
            queue.add(trieNode);
        }

        for (Map.Entry<Character, AddressTrieNode> child : trieNode.getChildren().entrySet()) {
            collect(child.getValue(), prefix + child.getKey(), queue);
        }
    }
}