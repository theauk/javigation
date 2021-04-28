package bfst21.data_structures;

import bfst21.Osm_Elements.Node;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

// TODO: 25-04-2021 implement HashMap<city(String), Arraylist<AddressTrieNodes>

public class AddressTrieNode implements Serializable {
    @Serial
    private static final long serialVersionUID = -9059402923966729263L;
    private HashMap<Character, AddressTrieNode> children;
    private HashMap<String, ArrayList<HouseNumberNode>> citiesWithThisStreet;
    private String streetname;
    private int postcode;
    HashMap<String,String> addresses;
    private boolean isAddress;


    // for the root
    public AddressTrieNode() {
        this.children = new HashMap<>();
        isAddress = false;
    }


    public void setAddress(Node node, String city, String streetname, int postcode, String houseNumber){
        citiesWithThisStreet = new HashMap<>();
        ArrayList<HouseNumberNode> list = new ArrayList<>();
        list.add(new HouseNumberNode(node, houseNumber));
        citiesWithThisStreet.put(city, list);
        this.streetname = streetname;
        this.postcode = postcode;
        isAddress = true;
    }

    public boolean isAddress(){
        return isAddress;
    }

    public void addHouseNumber(String city, Node node, String houseNumber){
        if(citiesWithThisStreet.containsKey(city)){
            citiesWithThisStreet.get(city).add(new HouseNumberNode(node, houseNumber));
        } else {
            ArrayList<HouseNumberNode> list = new ArrayList<>();
            list.add(new HouseNumberNode(node, houseNumber));
            citiesWithThisStreet.put(city, list);
        }
    }

    public HashMap<Character, AddressTrieNode> getChildren(){
        return children;
    }


    public HashMap<String, String> getAddresses(){
        if(addresses != null){
            return addresses;
        } else {
            addresses = new HashMap<>();
            for(String val : citiesWithThisStreet.keySet()){
                addresses.put(val, getAddressWithOutHouseNumber(val));
            }
        }
        return addresses;
    }

    private String getAddressWithOutHouseNumber(String city){
        return (this.streetname +  ", " + this.postcode + " " + city);
    }
    private String getFullAddress(HouseNumberNode node){
        return (this.streetname + "  " + node.houseNumber + ", " + this.postcode);
    }

    public HashMap<String, Node> getHouseNumbersOnStreet(String city){
        HashMap<String, Node> map = new HashMap<>();
        for(HouseNumberNode houseNumberNode : citiesWithThisStreet.get(city) ){
            map.put((getFullAddress(houseNumberNode) + " " + city), houseNumberNode.node);
        }
        return map;
    }

    private class HouseNumberNode{
        Node node;
        String houseNumber;

        public HouseNumberNode(Node _node, String _houseNumber){
            node = _node;
            houseNumber = _houseNumber;
        }
    }

}
