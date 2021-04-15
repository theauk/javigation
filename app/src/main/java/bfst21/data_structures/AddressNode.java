package bfst21.data_structures;

import bfst21.Osm_Elements.Node;

public class AddressNode {
    private Node node;
    private String city;
    private String streetname;
    private Integer postcode;
    private String houseNumber;

    public AddressNode(Node node, String city, String streetname, Integer postcode, String houseNumber){
        this.node = node;
        this.city = city;
        this.streetname = streetname;
        this.postcode = postcode;
        this.houseNumber = houseNumber;
    }

    public Node getNode() {
        return node;
    }

    public String getCity() {
        return city;
    }

    public String getStreetname() {
        return streetname;
    }

    public Integer getPostcode() {
        return postcode;
    }

    public String getHouseNumber() {
        return houseNumber;
    }
}
