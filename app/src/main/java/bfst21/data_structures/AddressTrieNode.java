package bfst21.data_structures;

import bfst21.Osm_Elements.Node;

import java.util.ArrayList;
import java.util.HashMap;

// TODO: 16-04-2021 Works, but is anything but optimal in memory usage 
// TODO: 16-04-2021 The 5 fields that has something to do with an address can be moved to a private class or something, so AddressTrieNode only holds one reference instead of 5 
public class AddressTrieNode {
    private HashMap<Character, AddressTrieNode> children;
    private ArrayList<AddressTrieNode> addressNodes;
    private AddressNode addressNode;

    // for the root
    public AddressTrieNode(){
        this.children = new HashMap<>();
        this.addressNodes = new ArrayList<>();
    }
    // for the other trienodes
    public AddressTrieNode(Node node, String city, String streetname, int postcode, String houseNumber){
        this.children = new HashMap<>();
        this.addressNodes = new ArrayList<>();
        addressNode = new AddressNode(node,city, streetname, postcode, houseNumber);

    }

    public ArrayList<AddressTrieNode> getAddressNodes() {
        return addressNodes;
    }

    public HashMap<Character, AddressTrieNode> getChildren(){
        return children;
    }

    // does this trienode contain an address?
    public boolean hasNode(){
        return this.addressNodes !=null;
    }

    public void addAddressNode(AddressTrieNode addressTrieNode){
        addressNodes.add(addressTrieNode);
    }

    public String getCity() {
        return addressNode.city;
    }

    public String getStreetname() {
        return addressNode.streetname;
    }

    public int getPostcode() {
        return addressNode.postcode;
    }

    public String getHouseNumber() {
        return addressNode.houseNumber;
    }
    public String getAddress(){
        return addressNode.streetname + " " + addressNode.houseNumber + ", " + addressNode.postcode + " " + addressNode.city;
    }

    private class AddressNode{
        private Node node;
        private String city;
        private String streetname;
        private Integer postcode;
        private String houseNumber;

        public AddressNode(Node node, String city, String streetname, int postcode, String houseNumber){
            this.node = node;
            this.city = city;
            this.streetname = streetname;
            this.postcode = postcode;
            this.houseNumber = houseNumber;
        }

    }
}
