package bfst21.utils;

import bfst21.Osm_Elements.Node;
import bfst21.data_structures.AddressTrieNode;
import bfst21.data_structures.AddressTriesTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for filtering, validation and suggestion making for a single String containing a Danish address.
 * Primary usage is that it can find an address within a {@link AddressTrieNode} in a {@link AddressTriesTree}.
 * Moreover, it contains several important methods for retrieving a {@link Node} to be able to get the
 * coordinates of an address.
 */
public class AddressFilter {

    private AddressTriesTree addressTree;
    private List<String> suggestions;
    private Address matchedAddress;

    private final String addressRegex = "^ *(?<street>[A-Za-zÆØÅæøå0-9 \\-.]+?),? *(?<number>\\d{1,3}[a-zæøå]?)?,? *(?<postCode>\\d{1,4})?(?: (?<city>[A-Za-zÆØÅæøå]+?|[A-Za-zÆØÅæøå]+? *[A-Za-zÆØÅæøå]+)?)? *$";
    private final Pattern pattern = Pattern.compile(addressRegex);
    private Matcher matcher;

    public AddressFilter() {
        suggestions = new ArrayList<>();
    }

    /**
     * Filters the given prefix and splits it up in address components such as street, number, postcode etc.
     * If the String matches a real address it simply sets the {@link AddressFilter#matchedAddress} to the found one.
     * Else it makes a List of suggestions containing possible addresses depending on the prefix.
     *
     * @param prefix the prefix String to be searched for.
     */
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

            List<AddressTrieNode> searchResult = addressTree.searchWithPrefix(street);
            if(searchResult.size() == 0) return;
            validateInput(searchResult, houseNumber, postCode, city);
        }
    }

    /**
     * Checks whether the address is valid or makes a
     * list of suggestions based on the parameters.
     *
     * @param searchResult a List of {@link AddressTrieNode}s.
     * @param houseNumber a house number.
     * @param postCode a post code.
     * @param city a city.
     */
    private void validateInput(List<AddressTrieNode> searchResult, String houseNumber, int postCode, String city) {
        makeSuggestions(searchResult, houseNumber, postCode, city);

        if(isMatch(searchResult.get(0), houseNumber, postCode, city)) {
            AddressTrieNode result = searchResult.get(0);
            matchedAddress = new Address(result.getStreetName(), houseNumber, postCode, city, result.findNode(houseNumber, postCode));
        }
    }

    /**
     * Creates a List of possible suggestions based on the parameters and sorts it.
     *
     * @param searchResult a List of {@link AddressTrieNode}s.
     * @param houseNumber the house number to make suggestions from.
     * @param postCode the post code to make suggestions from.
     * @param city the city to make suggestions from.
     */
    private void makeSuggestions(List<AddressTrieNode> searchResult, String houseNumber, int postCode, String city) {
        suggestions = filter(searchResult, houseNumber, postCode, city);
        Collections.sort(suggestions);
    }

    /**
     * Filters the given list of {@link AddressTrieNode}s based on
     * how the {@link AddressFilter#addressRegex}'s groups have been matched.
     *
     * @param searchResult a List of {@link AddressTrieNode}s.
     * @param houseNumber the house number to filter from.
     * @param postCode the post code to filter from.
     * @param city the city to filter from.
     * @return a List of Strings containing suggestions.
     */
    private List<String> filter(List<AddressTrieNode> searchResult, String houseNumber, int postCode, String city) {
        if(!houseNumber.isBlank() && postCode != 0 && !city.isBlank()) return getAddressesWithNumberPostCodeAndCity(searchResult.get(0), houseNumber, postCode, city);
        else if(!houseNumber.isBlank() && postCode == 0 && city.isBlank()) return getAddressesWithNumber(searchResult, houseNumber);
        return getAddresses(searchResult);
    }

    /**
     * Checks if the given address exists in the given {@link AddressTrieNode}.
     *
     * @param node the {@link AddressTrieNode} to check.
     * @param houseNumber the house number to check.
     * @param postCode the post code to check.
     * @param city the city to check-
     * @return true if the specified address exists in the {@link AddressTrieNode} else false.
     */
    private boolean isMatch(AddressTrieNode node, String houseNumber, int postCode, String city) {
        return node.isValidAddress(houseNumber, postCode, city);
    }

    /**
     * Creates and returns a List of possible addresses for the given street in
     * the given {@link AddressTrieNode}s in the format of a post code and a city.
     *
     * @param searchResult the List of {@link AddressTrieNode} to get the addresses from.
     * @return a List of Strings containing possible street names to a post code and city.
     */
    private List<String> getAddresses(List<AddressTrieNode> searchResult) {
        List<String> list = new ArrayList<>();

        for(AddressTrieNode node: searchResult) {
            list.addAll(node.getAddressesOnStreet());
        }

        return list;
    }

    /**
     * Creates and returns a List of possible addresses for the given house number.
     * It will then create all addresses that have the house number regardless of the post code or city.
     *
     * @param searchResult the list of {@link AddressTrieNode}s to be handled.
     * @param houseNumber the house number to be searched for.
     * @return a List of Strings containing possible addresses.
     */
    private List<String> getAddressesWithNumber(List<AddressTrieNode> searchResult, String houseNumber) {
        List<String> list = new ArrayList<>();

        for(AddressTrieNode node: searchResult) {
            list.addAll(node.getAddressFor(houseNumber));
        }

        return list;
    }

    /**
     * Creates and returns a List of possible addresses (primarily house numbers) within a given street based on the
     * house number, post code and city.
     *
     *
     * @param node the {@link AddressTrieNode} to be searched.
     * @param houseNumber the house number to look for.
     * @param postCode the post code to look for.
     * @param city the city to look for.
     * @return a List of Strings containing possible addresses.
     */
    private List<String> getAddressesWithNumberPostCodeAndCity(AddressTrieNode node, String houseNumber, int postCode, String city) {
        return new ArrayList<>(node.getAddressesFor(houseNumber, postCode, city));
    }

    /**
     * Checks if the given capture group from the {@link AddressFilter#addressRegex} has been matched.
     *
     * @param group the capture group to check.
     * @return true if the group has been matched else false.
     */
    private boolean matches(String group) {
        return matcher.group(group) != null;
    }

    public void setAddressTree(AddressTriesTree addressTree) {
        this.addressTree = addressTree;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public Address getMatchedAddress() {
        return matchedAddress;
    }
}
