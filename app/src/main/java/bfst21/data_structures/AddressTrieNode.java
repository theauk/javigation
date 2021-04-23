package bfst21.data_structures;

import bfst21.Osm_Elements.Node;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class AddressTrieNode implements Serializable {
    @Serial
    private static final long serialVersionUID = -9059402923966729263L;
    private HashMap<Character, AddressTrieNode> children;
    private ArrayList<AddressTrieNode> addressNodes;
    private Node node;
    private String city;
    private String streetname;
    private Integer postcode;
    private String houseNumber;
    private char character;
    private boolean isAddress;


    // for the root
    public AddressTrieNode() {
        this.children = new HashMap<>();
        this.addressNodes = new ArrayList<>();
        isAddress = false;
    }

    public AddressTrieNode(char character) {
        this.children = new HashMap<>();
        this.addressNodes = new ArrayList<>();
        this.character = character;
        isAddress = false;
    }

    // for the other trienodes
    public AddressTrieNode(Node node, String city, String streetname, int postcode, String houseNumber) {
        this.children = new HashMap<>();
        this.addressNodes = new ArrayList<>();
        this.node = node;
        this.city = city;
        this.streetname = streetname;
        this.postcode = postcode;
        this.houseNumber = houseNumber;
        isAddress = true;
    }

    public char getCharacter() {
        return character;
    }

    public ArrayList<AddressTrieNode> getAddressNodes() {
        return addressNodes;
    }

    public boolean isAddress() {
        return isAddress;
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
        isAddress = true;
    }

    public String getCity() {
        return this.city;
    }

    public String getStreetname() {
        return this.streetname;
    }

    public int getPostcode() {
        return this.postcode;
    }

    public String getHouseNumber() {
        return this.houseNumber;
    }
    public String getAddress(){
        return this.streetname + " " + this.houseNumber + ", " + this.postcode + " " + this.city;
    }

    public AddressTrieNode getAddressTrieNode(){
        return this;
    }

}
