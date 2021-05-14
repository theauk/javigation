package bfst21.data_structures;

import bfst21.Osm_Elements.Node;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class AddressTrieNode implements Comparable<AddressTrieNode>, Serializable {
    @Serial
    private static final long serialVersionUID = -9059402923966729263L;
    private final Map<Character, AddressTrieNode> children;
    private String streetName;
    private boolean isAddress;
    private Map<Integer, List<HouseNumberNode>> citiesWithThisStreet;

    /**
     * Creates a default AddressTrieNode which is not an address.
     */
    public AddressTrieNode() {
        children = new HashMap<>();
        isAddress = false;
    }

    /**
     * Sets this AddressTrieNode to be an address linked to a {@link Node} with a
     * specified name, house number and post code.
     *
     * @param node        the node from which an address will be created.
     * @param streetName  the street name of the address.
     * @param houseNumber the house number of the address.
     * @param postcode    the post code of the address.
     */
    public void setAddress(Node node, String streetName, String houseNumber, int postcode) {
        citiesWithThisStreet = new HashMap<>();
        List<HouseNumberNode> list = new ArrayList<>();
        list.add(new HouseNumberNode(node, houseNumber));
        citiesWithThisStreet.put(postcode, list);
        this.streetName = streetName;
        isAddress = true;
    }

    /**
     * Adds a house number to the street in the given city (determined by the post code).
     *
     * @param node        the node to be added.
     * @param houseNumber the house number to be added.
     * @param postcode    the post code to be added.
     */
    public void addHouseNumber(Node node, String houseNumber, int postcode) {
        if (citiesWithThisStreet.containsKey(postcode)) {
            citiesWithThisStreet.get(postcode).add(new HouseNumberNode(node, houseNumber));
            Collections.sort(citiesWithThisStreet.get(postcode));
        } else {
            List<HouseNumberNode> list = new ArrayList<>();
            list.add(new HouseNumberNode(node, houseNumber));
            citiesWithThisStreet.put(postcode, list);
        }
    }

    /**
     * Creates and returns a list of all the cities with this street in the format:
     * "&lt;streetName&gt;, &lt;post code&gt; &lt;city&gt;"
     *
     * @return a list of Strings containing the possible addresses for this street.
     */
    public List<String> getAddressesOnStreet() {
        List<String> list = new ArrayList<>();

        for (int postCode : citiesWithThisStreet.keySet()) {
            String address = streetName + ", " + postCode + " " + AddressTriesTree.POSTCODE_TO_CITIES.get(postCode);
            list.add(address);
        }

        return list;
    }

    /**
     * Searches for and returns all streets that start with a specified house number in each city.
     *
     * @param houseNumber the house number prefix for the addresses
     * @return a list of addresses starting with the specified house number.
     */
    public List<String> getAddressesFor(String houseNumber) {
        List<String> list = new ArrayList<>();

        for (Map.Entry<Integer, List<HouseNumberNode>> entry : citiesWithThisStreet.entrySet()) {    //Get the key/value set for postcode to list of house nodes
            for (HouseNumberNode node : entry.getValue()) {                                           //Run through each house number node to check if the specified address is present
                if (node.houseNumber.startsWith(houseNumber)) {
                    String address = streetName + " " + node.houseNumber + ", " + entry.getKey() + " " + AddressTriesTree.POSTCODE_TO_CITIES.get(entry.getKey());
                    list.add(address);
                }
            }
        }

        return list;
    }

    /**
     * Creates and returns a List of Strings, containing addresses that starts with the house number
     * in the specified city (post code).
     *
     * @param houseNumber the house number to be searched for.
     * @param postCode    the post code to be a parameter of the search.
     * @return a List containing the addresses found given the parameters.
     */
    private List<String> getAddressesFor(String houseNumber, int postCode) {
        List<String> list = new ArrayList<>();

        List<HouseNumberNode> nodes = citiesWithThisStreet.get(postCode);
        for (HouseNumberNode node : nodes) {
            if (node.houseNumber.startsWith(houseNumber)) {
                String address = streetName + " " + node.houseNumber + ", " + postCode + " " + AddressTriesTree.POSTCODE_TO_CITIES.get(postCode);
                list.add(address);
            }
        }

        return list;
    }

    /**
     * Gets addresses which starts with a certain postcode and house number.
     *
     * @param houseNumber The house number to search for.
     * @param postCode    The post code to search for.
     * @return A list of address matches.
     */
    public List<String> getAddressesStartingWithPostCode(String houseNumber, int postCode) {
        List<String> list = new ArrayList<>();

        for (Integer code : getAvailablePostCodesStartingWith(postCode)) {
            list.addAll(getAddressesFor(houseNumber, code));
        }

        return list;
    }

    /**
     * Searches for and returns a Node given a house number and a post code.
     *
     * @param houseNumber the house number the node contains.
     * @param postCode    the post code the node is located in.
     * @return a {@link Node} from the specified criteria or null if not found.
     */
    public Node findNode(String houseNumber, int postCode) {
        List<HouseNumberNode> nodes = citiesWithThisStreet.get(postCode);
        return nodes.get(getHouseNumberIndex(nodes, houseNumber)).node;
    }

    /**
     * Validates whether the given parameters equals to an address on this street.
     *
     * @param houseNumber the house number to validate.
     * @param postCode    the post code to validate.
     * @param city        the city to validate.
     * @return true if a complete match exists or false if not.
     */
    public boolean isValidAddress(String houseNumber, int postCode, String city) {
        if (!isValidCity(postCode, city)) return false;
        List<HouseNumberNode> nodes = citiesWithThisStreet.get(postCode);

        return getHouseNumberIndex(nodes, houseNumber) != -1;
    }

    /**
     * Validates whether a city exists from the given post code.
     *
     * @param postCode the post code associated with the city.
     * @param city     the city to be validated.
     * @return true if the city exists else false.
     */
    private boolean isValidCity(int postCode, String city) {
        String realCity = AddressTriesTree.POSTCODE_TO_CITIES.get(postCode);
        if (realCity == null) return false;
        return realCity.equals(city);
    }

    private List<Integer> getAvailablePostCodesStartingWith(int postCode) {
        List<Integer> list = new ArrayList<>();

        for (Integer i : citiesWithThisStreet.keySet()) {
            if (String.valueOf(i).startsWith(String.valueOf(postCode))) list.add(i);
        }

        return list;
    }

    /**
     * A binary search implementation for searching for a {@link HouseNumberNode} from a given house number.
     *
     * @param nodes       the List to be searched.
     * @param houseNumber the house number to search for.
     * @return the index of the {@link HouseNumberNode} if found or -1 if not.
     */
    private int getHouseNumberIndex(List<HouseNumberNode> nodes, String houseNumber) {
        int left = 0;
        int right = nodes.size() - 1;

        while (left <= right) {
            int m = left + (right - left) / 2;

            int res = houseNumber.compareTo(nodes.get(m).houseNumber);

            if (res == 0) return m;
            if (res > 0) left = m + 1;
            else right = m - 1;
        }

        return -1;
    }

    public String getStreetName() {
        return streetName;
    }

    public boolean isAddress() {
        return isAddress;
    }

    public Map<Character, AddressTrieNode> getChildren() {
        return children;
    }

    @Override
    public int compareTo(AddressTrieNode o) {
        return this.streetName.compareTo(o.streetName);
    }

    /**
     * Innerclass for AddressTrieNode, used to map a house number on a given street to specific location with a node.
     */
    private static class HouseNumberNode implements Comparable<HouseNumberNode>, Serializable {
        @Serial
        private static final long serialVersionUID = -2565597371430349023L;

        private final Node node;
        private final String houseNumber;

        public HouseNumberNode(Node node, String houseNumber) {
            this.node = node;
            this.houseNumber = houseNumber;
        }

        @Override
        public int compareTo(HouseNumberNode o) {
            return this.houseNumber.compareTo(o.houseNumber);
        }
    }
}
