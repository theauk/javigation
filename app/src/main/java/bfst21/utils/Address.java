package bfst21.utils;

import bfst21.Osm_Elements.Node;

public class Address {
    private String street;
    private String houseNumber;
    private int postCode;
    private String city;
    private Node node;

    public Address(String street, String houseNumber, int postCode, String city, Node node) {
        this.street = street;
        this.houseNumber = houseNumber;
        this.postCode = postCode;
        this.city = city;
        this.node = node;
    }

    public String getStreet() {
        return street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public int getPostCode() {
        return postCode;
    }

    public String getCity() {
        return city;
    }

    public Node getNode() {
        return node;
    }

    public String getFullAddress() {
        return street + " " + houseNumber + ", " + postCode + " " + city;
    }
}
