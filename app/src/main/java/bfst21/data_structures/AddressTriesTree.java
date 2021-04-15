package bfst21.data_structures;

import bfst21.Osm_Elements.Node;

import java.util.List;

/**
 * Not yet implemented
 */
// TODO: 28-03-2021 implement
public class AddressTriesTree {
    private AddressTrieNode root;
    private AddressNode addressNode;

    public AddressTriesTree(){
        root = new AddressTrieNode();
    }


    /**
     * Not yet implemented!
     *
     */
    public void put(Node node, String city, String streetname, int postcode, String houseNumber){
        addressNode = new AddressNode(node, city, streetname, postcode, houseNumber);
        insert(root, addressNode);
    }

    public void insert(AddressTrieNode root, AddressNode addressNode){
        insert_address(root,addressNode,0);

    }

    public void insert_address(AddressTrieNode trieNode, AddressNode addressNode, int index){
        var stringPostcode = Integer.toString(addressNode.getPostcode());
        if(index ==stringPostcode.length()){
            trieNode.addAddressNode(addressNode);
        } else{
            Character currentChar = stringPostcode.charAt(index);
            if(! trieNode.getChildren().containsKey(currentChar)){
                AddressTrieNode new_child = new AddressTrieNode();
                trieNode.getChildren().put(currentChar, new_child);
            }
            insert_address(trieNode.getChildren().get(currentChar),addressNode, index+1);
        }
    }
    /**
     * Not yet implemented!
     */
    public Node getAddressNode(String address){
        return null;
    }

    /**
     * Not yet Implemented
     */
    public List<Node> getPossibleAddresses(String address){
        return null;
    }

    public Object searchWithPostcode(AddressTrieNode trieNode, String postcode, int index){
            // returns NULL if there is no user going by that name
            if (index == postcode.length()){
                for(var address: trieNode.getAddressNodes()){
                    return address.getPostcode();
                }
                //return trieNode.getAddressNode().getPostcode();
            } else {
                Character current_char =  postcode.charAt(index);
                if (! trieNode.getChildren().containsKey(current_char)){
                    return null;
                }  else {
                    return searchWithPostcode(trieNode.getChildren().get(current_char), postcode, index+1);
                }
            }
        return null;
    }

    }
