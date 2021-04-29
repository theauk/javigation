package bfst21.data_structures;

import bfst21.Osm_Elements.Node;

import java.util.*;
import java.io.Serial;
import java.io.Serializable;


public class AddressTriesTree implements Serializable {
    @Serial
    private static final long serialVersionUID = 5713923887785799744L;

    private AddressTrieNode root;

    public AddressTriesTree() {
        root = new AddressTrieNode();
    }


    /**
     * @param node        -> contains the coordinates for the address.
     * @param city        -> the city which the address is located at.
     * @param streetname  -> the name of the street which the address belongs to.
     * @param postcode    -> The 4 digit number that tells in what part of the country the address is located.
     *                    in Denmark it's how far they are from Copenhagen
     * @param houseNumber -> the housenumber that the address has.
     *
     *                    calls insert that inserts the node into the trie via its streetname and city.
     */
    public void put(Node node, String city, String streetname, int postcode, String houseNumber) {
        insert(root, node, city, streetname, postcode, houseNumber);
    }


    public void insert(AddressTrieNode root, Node node, String city, String streetname, int postcode, String houseNumber) {
            streetname = streetname.toLowerCase();
            insert_address_with_streetname(root,0 , node, city, streetname, postcode, houseNumber);
        }

    /**
     *
     * @param trieNode -> when called for the first time, this would be the root.
     *                     afterwards in the recursive calls inside the method will call the method with the next node (a child), and proceed
     *                     to the bottom of the trie, where the addressNode will be added to the Arraylist in that last node's arraylist.
     *                     this @param trieNode could be omitted, but then the methods needs to be iterative instead of recursive.
     * @param index -> the start index is always 0, since the method will start from the root, and go down through the tree.
     *
     *                 index could be omitted as well, but the method would need to be made iterative instead of recursive.
     * @param node -> the node that contains the coordinates for the address
     * @param city -> the name of the city given by the .osm file.
     * @param streetname -> the name of the street given by the .osm file.
     * @param postcode -> the postcode of the node given by the .osm file.
     * @param houseNumber -> the house number given by the .osm file.
     */
    private void insert_address_with_streetname(AddressTrieNode trieNode, int index, Node node, String city, String streetname, int postcode, String houseNumber){
        if (index == streetname.length()) {
                if(trieNode.isAddress()) trieNode.addHouseNumber(city, node, houseNumber);
                else trieNode.setAddress(node, city, streetname, postcode, houseNumber);
                }
            else {
                Character currentChar = streetname.charAt(index);
                if (!trieNode.getChildren().containsKey(currentChar)) {
                    AddressTrieNode new_child = new AddressTrieNode();
                    trieNode.getChildren().put(currentChar, new_child);
                }
                insert_address_with_streetname(trieNode.getChildren().get(currentChar), index + 1 , node, city, streetname, postcode, houseNumber);
            }
    }

    /**
     *
     * @return -> returns all the possible streetnames in the trie.
     */
    public ArrayList<AddressTrieNode> keys(){
        return searchWithPrefix("");
    }

    /**
     * Adapted from the Algorithms book by Sedgewick & Wayne
     * @param prefix -> prefix to possible streetnames
     * @return -> a list (ArrayList) with the possible streetnames that matches the prefix with help from the help methods.
     */
    public ArrayList<AddressTrieNode> searchWithPrefix(String prefix){
        ArrayList<AddressTrieNode> queue = new ArrayList<>();
        prefix = prefix.toLowerCase();
        collect(get(root, prefix,0),prefix,queue);
        return queue;
    }

    /**
     *
     * @param trieNode -> the trienode the method is currently at. It starts from the root and goes through the tree until it
     *                 gets the node associated with key in the subtrie rooted at the trienode
     * @param key -> could also be called prefix. Calls the method recursively until index is as long as key, and then it returns the given trienode
     * @param index -> starts at 0, but increases up to the length of the key - this is so the method can be called recursively
     * @return -> returns the trienode that matches the key.
     */
    private AddressTrieNode get(AddressTrieNode trieNode, String key, int index){
        if(trieNode == null) return null;
        if(index == key.length()) {
            return trieNode;
        }
        char character = key.charAt(index);
        return get(trieNode.getChildren().get(character),key, index+1);
    }

    /**
     *
     * @param trieNode -> the trienode the method is currently at. It starts from the root and goes through the tree until it
     *                 finds a streetname that matches the given prefix and add it to the list (queue), simply returns nothing.
     * @param prefix -> help method for searchWithPrefix -> adds streetnames to the list that begins with the given prefix
     * @param queue -> the list that searchWithPrefix with possible streetnames to the given prefix
     */
    private void collect(AddressTrieNode trieNode, String prefix, ArrayList<AddressTrieNode> queue){
        if(trieNode == null) return;
            if(trieNode.isAddress()){
                queue.add(trieNode);
            }

        for (Map.Entry<Character, AddressTrieNode> child : trieNode.getChildren().entrySet()) {
            collect(child.getValue(), prefix + child.getKey(), queue);
        }
    }
}
