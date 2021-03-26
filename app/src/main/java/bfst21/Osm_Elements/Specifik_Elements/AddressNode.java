package bfst21.Osm_Elements.Specifik_Elements;

import bfst21.Osm_Elements.Node;

public class AddressNode {
    private Node node;
    private String housenumber;
    private String city;
    private int postcode;
    private String street;

    public AddressNode(Node node, String city) {
        this.node = node;
        this.city = city;

    }

    public String getHousenumber() {
        return housenumber;
    }

    public void setHousenumber(String housenumber) {
        this.housenumber = housenumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getPostcode() {
        return postcode;
    }

    public void setPostcode(int postcode) {
        this.postcode = postcode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Node getNode() {
        return node;
    }
}