package bfst21.Osm_Elements.Specifik_Elements;

import bfst21.Osm_Elements.Node;

public class AddressNode{
    private Node node;
    private String housenumber;
    private String city;
    private int postcode;
    private String street;

    public AddressNode(Node node, String city) {
        this.node = node;
        this.city = city;
        //TODO Auto-generated constructor stub
    }

    public void setHousenumber(String housenumber) {
        this.housenumber = housenumber;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPostcode(int postcode) {
        this.postcode = postcode;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHousenumber() {
        return housenumber;
    }

    public String getCity() {
        return city;
    }

    public int getPostcode() {
        return postcode;
    }

    public String getStreet() {
        return street;
    }

    public Node getNode() {
        return node;
    }
}