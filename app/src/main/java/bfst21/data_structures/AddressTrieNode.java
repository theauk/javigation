package bfst21.data_structures;

import bfst21.Osm_Elements.Node;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddressTrieNode implements Serializable {
    @Serial
    private static final long serialVersionUID = -9059402923966729263L;

    private final Map<Character, AddressTrieNode> children;
    private Map<Integer, ArrayList<HouseNumberNode>> citiesWithThisStreet;
    private String streetname;
    private Map<Integer,String> addresses;
    private Map<Integer, String> postCodesToCities;
    private boolean isAddress;

    public AddressTrieNode() {
        children = new HashMap<>();
        isAddress = false;
    }

    public void setAddress(Node node, int postcode, String streetname, String houseNumber, Map<Integer, String> postcodesToCities){
        citiesWithThisStreet = new HashMap<>();
        ArrayList<HouseNumberNode> list = new ArrayList<>();
        list.add(new HouseNumberNode(node, houseNumber));
        citiesWithThisStreet.put(postcode, list);
        this.streetname = streetname;
        isAddress = true;
        this.postCodesToCities = postcodesToCities;
    }

    public boolean isAddress(){
        return isAddress;
    }

    public void addHouseNumber(int postcode, Node node, String houseNumber){
        if(citiesWithThisStreet.containsKey(postcode)){
            citiesWithThisStreet.get(postcode).add(new HouseNumberNode(node, houseNumber));
        } else {
            ArrayList<HouseNumberNode> list = new ArrayList<>();
            list.add(new HouseNumberNode(node, houseNumber));
            citiesWithThisStreet.put(postcode, list);
        }
    }

    public Map<Character, AddressTrieNode> getChildren(){
        return children;
    }


    public Map<Integer, String> getAddresses(){
        if(addresses != null){
            return addresses;
        } else {
            addresses = new HashMap<>();
            for(int val : citiesWithThisStreet.keySet()){
                addresses.put(val, getAddressWithOutHouseNumber(val));
            }
        }
        return addresses;
    }

    private String getAddressWithOutHouseNumber(int postcode){
        return (this.streetname +  ", " + postcode + " " + postCodesToCities.get(postcode));
    }

    public HashMap<String, Node> getHouseNumbersOnStreet(int postcode){
        HashMap<String, Node> map = new HashMap<>();
        for(HouseNumberNode houseNumberNode : citiesWithThisStreet.get(postcode) ){
            map.put((this.streetname + "  " + houseNumberNode.houseNumber + ", " + postcode + " " + postCodesToCities.get(postcode)), houseNumberNode.node);
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
