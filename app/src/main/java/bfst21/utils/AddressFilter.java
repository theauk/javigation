package bfst21.utils;

import bfst21.Osm_Elements.Node;
import bfst21.data_structures.AddressTrieNode;
import bfst21.data_structures.AddressTriesTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressFilter implements Filter {

    private AddressTriesTree addressTree;
    private List<String> suggestions;

    //TODO FIX CITY KAN KUN TAGE ET BOGSTAV TIL SIDST
    private final String regex = "^ *(?<street>[a-zæøå0-9 \\-.]+?),? *(?<number>\\d{1,3}[a-zæøå]?)?,? *(?<postCode>\\d{1,4})?(?: (?<city>[a-zæøå]+?|[a-zæøå]+? *[a-zæøå])?)? *$";
    private final Pattern pattern = Pattern.compile(regex);
    private Matcher matcher;

    public AddressFilter() {
        suggestions = new ArrayList<>();
    }

    public void search(String prefix) {
        suggestions = new ArrayList<>();
        String street = "";
        String houseNumber = "";
        int postCode = 0;
        String city = "";

        matcher = pattern.matcher(prefix);
        if(matcher.matches()) {
            if(matches("street")) street = matcher.group("street");
            if(matches("number")) houseNumber = matcher.group("number");
            if(matches("postCode")) postCode = Integer.parseInt(matcher.group("postCode"));
            if(matches("city")) city = matcher.group("city");

            suggestions = filter(addressTree.searchWithPrefix(street), street, houseNumber, postCode, city);
            Collections.sort(suggestions);
        } else suggestions.add("No matches!");
    }

    private List<String> filter(List<AddressTrieNode> searchResult, String street, String houseNumber, int postCode, String city) {

        if(!street.isBlank() && !houseNumber.isBlank() && validPostCode(postCode) && validCity(city)) getNode(searchResult.get(0), houseNumber, postCode);
        if(!street.isBlank() && !houseNumber.isBlank() && postCode == 0) return getAddressesWithNumber(searchResult, houseNumber);
        //else if(!street.isBlank() && !houseNumber.isBlank() && validPostCode(postCode)) getNode(searchResult.get(0), houseNumber, postCode);


        return getAddresses(searchResult);
    }

    private boolean validPostCode(int postCode) {
        return Collections.binarySearch(addressTree.getPostCodes(), postCode) >= 0;
    }

    private boolean validCity(String city) {
        List<String> cities = addressTree.getCities();
        System.out.println("BINARY ON: " + city);
        int index = Collections.binarySearch(cities, city);
        if(index >= 0) System.out.println("RESULT: i = " + index + " : " + cities.get(index));

        return Collections.binarySearch(addressTree.getCities(), city) >= 0;
    }

    private boolean validHouseNumber(String houseNumber) {
        return true;
    }

    private List<String> getAddresses(List<AddressTrieNode> searchResult) {
        List<String> list = new ArrayList<>();

        for(AddressTrieNode node: searchResult) {
            list.addAll(node.getAddressesOnStreet());
        }

        return list;
    }

    private List<String> getAddressesWithNumber(List<AddressTrieNode> searchResult, String houseNumber) {
        List<String> list = new ArrayList<>();

        for(AddressTrieNode node: searchResult) {
            list.addAll(node.getAddressFor(houseNumber));
        }

        return list;
    }

    private void getNode(AddressTrieNode result, String houseNumber, int postCode) {
        if(result.isValidAddress(houseNumber, postCode)) {
            Node node = result.findNode(houseNumber, postCode);
            System.out.println("PERFECT MATCH");
        } else System.err.println("Invalid address!");
    }

    private boolean matches(String group) {
        return matcher.group(group) != null;
    }

    public void setAddressTree(AddressTriesTree addressTree) {
        this.addressTree = addressTree;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}
