package bfst21.data_structures;

import bfst21.Osm_Elements.Node;

import java.util.ArrayList;
import java.util.HashMap;

public class AddressTrieNode {
    private HashMap<Character, AddressTrieNode> children;
    private ArrayList<AddressNode> addressNodes;

    public AddressTrieNode(){
        this.children = new HashMap<>();
        this.addressNodes = null;
    }

    public ArrayList<AddressNode> getAddressNodes() {
        return addressNodes;
    }

    public HashMap<Character, AddressTrieNode> getChildren(){
        return children;
    }

    // does this trienode contain an address?
    public boolean hasNode(){
        return this.addressNodes !=null;
    }

    public void addAddressNode(AddressNode addressNode){
        addressNodes.add(addressNode);
    }
}
