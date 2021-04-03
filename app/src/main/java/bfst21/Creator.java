package bfst21;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Specifik_Elements.AddressNode;
import bfst21.Osm_Elements.Specifik_Elements.TravelWay;
import bfst21.Osm_Elements.Way;
import bfst21.data_structures.BinarySearchTree;
import bfst21.file_reading.ProgressInputStream;
import javafx.concurrent.Task;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashSet;


import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
Creates Objects such as Nodes, Ways and Relations from the .osm file given from the Loader.
 */
public class Creator extends Task<Void> {
    private MapData mapData;
    private ProgressInputStream progressInputStream;
    private HashSet<String> nodesNotCreateKeys;
    private HashSet<String> nodesNotCreateValues;

    private int totalNodes, nodesNotCreateCount, notKeys, notValues; // TODO: 4/2/21  delete

    public Creator(MapData mapData, InputStream inputStream, long fileSize) {
        this.mapData = mapData;
        progressInputStream = new ProgressInputStream(inputStream);
        progressInputStream.addInputStreamListener(totalBytes -> updateProgress(totalBytes, fileSize));
        nodesNotCreateKeys = new HashSet<>();
        nodesNotCreateValues = new HashSet<>();
        setupNodesNotCreate();

        totalNodes = 0;
        nodesNotCreateCount = 0;
        notKeys = 0;
        notValues = 0;
    }

    @Override
    protected Void call() throws Exception {
        create();
        return null;
    }

    public void create() throws XMLStreamException {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new BufferedInputStream(progressInputStream));

        BinarySearchTree<Node> idToNode = new BinarySearchTree<>();
        BinarySearchTree<Way> idToWay = new BinarySearchTree<>();

        Way way = null;
        Node node = null;
        Relation relation = null;
        TravelWay travelWay = null;
        AddressNode addressNode = null;

        while (reader.hasNext()) {
            if (isCancelled()) return;   //Abort task
            else {
                switch (reader.next()) {
                    case START_ELEMENT:
                        switch (reader.getLocalName()) {
                            case "bounds":
                                mapData.setMinX(Float.parseFloat(reader.getAttributeValue(null, "minlon")));
                                mapData.setMaxX(Float.parseFloat(reader.getAttributeValue(null, "maxlon")));
                                mapData.setMaxY(Float.parseFloat(reader.getAttributeValue(null, "minlat")) / -0.56f);
                                mapData.setMinY(Float.parseFloat(reader.getAttributeValue(null, "maxlat")) / -0.56f);
                                break;
                            case "node":
                                updateMessage("Loading: Nodes");
                                var idNode = Long.parseLong(reader.getAttributeValue(null, "id"));
                                var lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                                var lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                                node = new Node(idNode, lon, lat);
                                totalNodes += 1;
                                break;
                            case "way":
                                updateMessage("Loading: Ways");
                                var idWay = Long.parseLong(reader.getAttributeValue(null, "id"));
                                way = new Way(idWay);
                                break;
                            case "relation":
                                updateMessage("Loading: Relations");
                                relation = new Relation(Long.parseLong(reader.getAttributeValue(null, "id")));
                                break;
                            case "tag":
                                var k = reader.getAttributeValue(null, "k");
                                var v = reader.getAttributeValue(null, "v");

                                if(k.equals("name") && v.equals("Ved Mønten")) {
                                    System.out.println("v: Ved Mønten");
                                }

                                if (node != null && checkNodesNotCreate(k, v)) { // TODO: 4/3/21 delete
                                    nodesNotCreateCount += 1;
                                }

                                if (k.equals("highway")) {
                                    if (checkHighWayType(way, v)) travelWay = new TravelWay(way, v);
                                    way = null;
                                    break;
                                }

                                if (node != null) {
                                    if (k.equals("addr:city")) {
                                        addressNode = new AddressNode(node);
                                        node = null;
                                    }
                                    break;
                                }


                                if (addressNode != null) {
                                    checkAddressNode(k, v, addressNode);
                                    break;
                                }

                                if (relation != null) {
                                    checkRelation(k, v, relation);
                                    break;
                                }

                                if (travelWay != null) {
                                    checkTravelWay(k, v, travelWay);
                                    break;
                                }

                                if (way != null) {
                                    checkWay(k, v, way);
                                    break;
                                }
                                break;
                            case "nd":
                                if (way != null) {
                                    var refNode = Long.parseLong(reader.getAttributeValue(null, "ref"));
                                    way.addNode(idToNode.get(refNode));
                                }
                                break;

                            case "member":
                                if (relation != null) {
                                    var type = (reader.getAttributeValue(null, "type"));
                                    var refR = Long.parseLong(reader.getAttributeValue(null, "ref"));
                                    if (type.equals("way")) {
                                        relation.addWay(idToWay.get(refR));
                                    }
                                    if (type.equals("node")) {
                                        relation.addNode(idToNode.get(refR));
                                    }
                                }
                                break;
                        }
                        break;
                    case END_ELEMENT:
                        switch (reader.getLocalName()) {
                            case "way":
                                if (way != null) {
                                    idToWay.put(way);
                                    if (way.hasType()) {
                                        mapData.addDataRTree(way);
                                    }
                                    way = null;
                                }
                                if (travelWay != null) {
                                    idToWay.put(travelWay);
                                    mapData.addRoad(travelWay);
                                    /*if(travelWay.getName().equals("Ved Mønten")) {
                                        System.out.println("Travelway insert: Ved Mønten");
                                    }*/
                                    travelWay = null;
                                }
                                break;

                            case "node":
                                if (addressNode != null) {
                                    mapData.addAddress(addressNode);
                                    addressNode = null;

                                } else if (node != null) {
                                    idToNode.put(node);
                                    node = null;
                                }

                                break;

                            case "relation":
                                if (relation != null) { // TODO: 4/3/21 fix
                                    //mapData.add(relation)
                                }
                                relation = null;
                        }
                        break;
                }
            }
        }
        idToWay = null;
        updateMessage("");
        /*System.out.println("Total Nodes: " + totalNodes);
        System.out.println("Nodes with Keys not create: " + notKeys);
        System.out.println("Nodes with Values not create: " + notValues);
        System.out.println("Total nodes not create: " + nodesNotCreateCount);
        float per = (float) nodesNotCreateCount / (float) totalNodes * 100;
        System.out.println("Percentage that can be skipped: " + per + " %");*/
        reader.close();
    }

    private void checkRelation(String k, String v, Relation relation) {
        switch (k) {
            case "type":
                if (v.equals("restriction")) relation.setType(v);
                break;
            case "restriction":
                relation.setRestriction(v);
                break;
            case "name":
                relation.setName(v);
                break;
        }
    }

    private void checkTravelWay(String k, String v, TravelWay travelWay) {
        switch (k) {
            case "oneway":
                if (v.equals("yes")) travelWay.setOnewayRoad();
                break;
            case "cycleway":
                if (!v.equals("no")) travelWay.setNotCycleable();
                break;
            case "maxspeed":
                if (v.equals("signals")) {
                    travelWay.defaultMaxSpeed();
                    // TODO: 02-04-2021 this the right thing ?
                } else {
                    try {
                        travelWay.setMaxspeed(Integer.parseInt(v));
                    } catch (NumberFormatException e) {
                        travelWay.defaultMaxSpeed();
                        System.out.println("Max speed exception: " + v);
                    }
                }

                break;
            case "name":
                travelWay.setName(v);
                break;
        }
    }

    private void checkWay(String k, String v, Way way) {
        switch (k) {
            case "natural":
                if (v.equals("coastline")) way.setType(v);
                break;

            case "building":
                if (v.equals("yes")) way.setType(v);
                break;

            case "leisure":
                if (v.equals("park")) way.setType(v);
                break;
        }
    }

    private void checkAddressNode(String k, String v, AddressNode addressNode) {
        switch (k) {
            case "addr:city" -> addressNode.setCity(v);
            case "addr:housenumber" -> addressNode.setHousenumber((v));
            case "addr:postcode" -> addressNode.setPostcode(Integer.parseInt(v.trim()));
            case "addr:street" -> addressNode.setStreet(v);
        }
    }

    private boolean checkHighWayType(Way way, String v) {
        if (way == null) return false;
        return highWayTypeHelper(v);
    }

    private boolean highWayTypeHelper(String v) {
        if (v.equals("motorway")) return true;
        if (v.equals("trunk")) return true;
        if (v.equals("primary")) return true;
        if (v.equals("secondary")) return true;
        if (v.equals("tertiary")) return true;
        if (v.equals("unclassified")) return true;
        if (v.equals("residential")) return true;
        if (v.contains("link")) return true;
        if (v.equals("living_street")) return true;
        if (v.equals("pedestrian")) return true;
        if (v.equals("road")) return true;
        if (v.equals("footway")) return true;
        if (v.equals("cycleway")) return true;

        else return false;
    }

    private void setupNodesNotCreate() { // TODO: 4/3/21 Make it delete the nodes + do not creating ways / relations with those tags either
        nodesNotCreateKeys.add("aerialway");
        nodesNotCreateKeys.add("aeroway");
        nodesNotCreateKeys.add("amenity");
        nodesNotCreateKeys.add("barrier");
        nodesNotCreateKeys.add("boundary");
        nodesNotCreateKeys.add("craft");
        nodesNotCreateKeys.add("emergency");
        nodesNotCreateKeys.add("geological");
        nodesNotCreateKeys.add("healthcare");
        nodesNotCreateKeys.add("historic");
        nodesNotCreateKeys.add("man_made");
        nodesNotCreateKeys.add("military");
        nodesNotCreateKeys.add("office");
        nodesNotCreateKeys.add("power");
        nodesNotCreateKeys.add("shop");
        nodesNotCreateKeys.add("sport");
        nodesNotCreateKeys.add("telecom");
        nodesNotCreateKeys.add("tourism");

        nodesNotCreateKeys.add("comment");
        nodesNotCreateKeys.add("email");
        nodesNotCreateKeys.add("fax");
        nodesNotCreateKeys.add("fixme");
        nodesNotCreateKeys.add("image");
        nodesNotCreateKeys.add("note");
        nodesNotCreateKeys.add("phone");
        nodesNotCreateKeys.add("source_ref");
        nodesNotCreateKeys.add("todo");
        nodesNotCreateKeys.add("url");
        nodesNotCreateKeys.add("website");
        nodesNotCreateKeys.add("wikipedia");

        nodesNotCreateValues.add("emergency_access_point");
        nodesNotCreateValues.add("give_way");
        nodesNotCreateValues.add("milestone");
        nodesNotCreateValues.add("speed_camera");
        nodesNotCreateValues.add("street_lamp");
        nodesNotCreateValues.add("stop");
        nodesNotCreateValues.add("traffic_signal");
        nodesNotCreateValues.add("depot");

        nodesNotCreateValues.add("adult_gaming_centre");
        nodesNotCreateValues.add("amusement_arcade");
        nodesNotCreateValues.add("bandstand");
        nodesNotCreateValues.add("beach_resort");
        nodesNotCreateValues.add("bird_hide");
        nodesNotCreateValues.add("common");
        nodesNotCreateValues.add("dance");
        nodesNotCreateValues.add("disc_golf_course");
        nodesNotCreateValues.add("escape_game");
        nodesNotCreateValues.add("firepit");
        nodesNotCreateValues.add("fishing");
        nodesNotCreateValues.add("fitness_centre");
        nodesNotCreateValues.add("fitness_station");
        nodesNotCreateValues.add("hackerspace");
        nodesNotCreateValues.add("miniature_golf");
        nodesNotCreateValues.add("picnic_table");
        nodesNotCreateValues.add("summer_camp");
        nodesNotCreateValues.add("tree_row");
        nodesNotCreateValues.add("tree");
        nodesNotCreateValues.add("peak");

        nodesNotCreateValues.add("canoe");
        nodesNotCreateValues.add("detour");
        nodesNotCreateValues.add("hiking");
        nodesNotCreateValues.add("horse");
        nodesNotCreateValues.add("inline_skates");
        nodesNotCreateValues.add("mtb");
        nodesNotCreateValues.add("piste");
        nodesNotCreateValues.add("running");
    }

    private boolean checkNodesNotCreate(String k, String v) {
        if (nodesNotCreateKeys.contains(k)) {
            notKeys += 1;
        } else if (nodesNotCreateValues.contains(v)) {
            notValues += 1;
        }
        return nodesNotCreateKeys.contains(k) || nodesNotCreateValues.contains(v);
    }
}