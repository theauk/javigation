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

    // TODO: 29-04-2021 eidt the javadoc 

    /**
     * @param node        -> a node, which contains the coordinates for the address.
     * @param city        -> the city which the address is located at. comes after the postcode when viewed - etc. postcode Jerslev sj
     * @param streetname  -> the name of the street which the address belongs to
     * @param postcode    -> The 4 digit number that tells in what part of the country the address is located.
     *                    in Denmark it's how far they are from Copenhagen
     * @param houseNumber -> the housenumber or street-number, that the address had
     */
    public void put(Node node, String city, String streetname, int postcode, String houseNumber) {
        insert(root, node, city, streetname, postcode, houseNumber);
    }


    public void insert(AddressTrieNode root, Node node, String city, String streetname, int postcode, String houseNumber) {
            streetname = streetname.toLowerCase();
            insert_address_with_streetname(root,0 , node, city, streetname, postcode, houseNumber);
        }

    /**
     * This will insert the address into the trie by using its postcode as key.
     * @param trieNode -> when called for the first time, this would be the root.
     *                 afterwards in the recursive calls inside the method will call the method with the next node (a child), and proceed
     *                 to the bottom of the trie, where the addressNode will be added to the Arraylist in that last node's arraylist.
     *                 this @param trieNode could be omitted, but then the methods needs to be iterative instead of recursive.
     *
     * @param index -> the start index is always 0, since the method will start from the root, and go down through the tree.
     *              index could be omitted as well, but the method would need to be made iterative instead of recursive.
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

    // Returns all streetnames in the trie.
    public ArrayList<AddressTrieNode> keys(){
        return searchWithPrefix("");
    }
    // Returns AddressTrieNodes with streetnames that has the given prefix
    // (taken from the algo book, however it has been modified)
    public ArrayList<AddressTrieNode> searchWithPrefix(String prefix){
        ArrayList<AddressTrieNode> queue = new ArrayList<>();
        prefix = prefix.toLowerCase();
        collect(get(root, prefix,0),prefix,queue);
        return queue;
    }

    private AddressTrieNode get(AddressTrieNode trieNode, String key, int index){
        // return node associated with key in the subtrie rooted at trienode
        if(trieNode == null) return null;
        if(index == key.length()) {
            return trieNode;
        }
        char character = key.charAt(index);
        return get(trieNode.getChildren().get(character),key, index+1);
    }

    private void collect(AddressTrieNode trieNode, String prefix, ArrayList<AddressTrieNode> queue){
        if(trieNode == null) return;
            if(trieNode.isAddress()){
                queue.add(trieNode);
            }

        for (Map.Entry<Character, AddressTrieNode> child : trieNode.getChildren().entrySet()) {
            collect(child.getValue(), prefix + child.getKey(), queue);
        }
    }

    // quick print test
    public static void main(String[] args) {
        Node node1 = new Node(340551927, 55.6786770f, 12.5694510f);
        Node node2 = new Node(340551928, 55.6786400f, 12.5698360f);
        Node node3 = new Node(340551929,55.6786500f,12.5698370f);
        Node node4 = new Node(340551930,55.6783500f,12.5693370f);

        AddressTriesTree trie = new AddressTriesTree();
        trie.put(node1, "København K", "Studiestræde", 1455, "18");
        trie.put(node2, "København K", "Studiestræde", 1455, "19");
        trie.put(node3, "København K", "Studievej", 1455, "25");
        trie.put(node4, "Roskilde", "Studiestræde", 4000, "4");


        ArrayList<AddressTrieNode> list = trie.searchWithPrefix("Studie");
        for(AddressTrieNode node : list){
            System.out.println(node.getStreetname());
        }


    }
        }
