package bfst21;

import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Specifik_Elements.AddressNode;
import bfst21.Osm_Elements.Specifik_Elements.TravelWay;
import bfst21.Osm_Elements.Way;
import bfst21.data_structures.AlternateBinarySearchTree;
import bfst21.file_reading.ProgressInputStream;
import javafx.concurrent.Task;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/*
Creates Objects such as Nodes, Ways and Relations from the .osm file given from the Loader.
 */
public class Creator extends Task<Void> {
    private MapData mapData;
    private ProgressInputStream progressInputStream;


    public Creator(MapData mapData, InputStream inputStream, long fileSize) {
        this.mapData = mapData;

        progressInputStream = new ProgressInputStream(inputStream);
        progressInputStream.addInputStreamListener(totalBytes -> updateProgress(totalBytes, fileSize));

    }

    @Override
    protected Void call() throws Exception {
        create();
        return null;
    }

    public void create() throws XMLStreamException {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new BufferedInputStream(progressInputStream));

        AlternateBinarySearchTree<Long, Node> idToNode = new AlternateBinarySearchTree<>();
        AlternateBinarySearchTree<Long, Way> idToWay = new AlternateBinarySearchTree<>();

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
                                var idNode = Long.parseLong(reader.getAttributeValue(null, "id"));
                                var lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                                var lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                                node = new Node(idNode, lon, lat);
                                idToNode.put(idNode, node);
                                break;

                            case "way":
                                var idWay = Long.parseLong(reader.getAttributeValue(null, "id"));
                                way = new Way(idWay);
                                idToWay.put(idWay, way);
                                break;

                            case "relation":
                                relation = new Relation(Long.parseLong(reader.getAttributeValue(null, "id")));
                                break;

                            case "tag":
                                var k = reader.getAttributeValue(null, "k");
                                var v = reader.getAttributeValue(null, "v");

                                if (k.equals("highway")) {
                                    if (checkHighWayType(way, v)) travelWay = new TravelWay(way,v);
                                    way = null;
                                }

                                if (k.equals("addr:city")) {
                                    if(node !=null) {
                                        addressNode = new AddressNode(node);
                                        node = null;
                                    }
                                }

                                if (addressNode != null) {
                                    checkAddressNode(k, v, addressNode);
                                }

                                if (relation != null) {
                                    checkRelation(k, v, relation);
                                }

                                if (travelWay != null) {
                                    checkTravelWay(k, v, travelWay);
                                }

                                if (way != null) {
                                    checkWay(k, v, way);
                                }
                                break;
                            case "nd":
                                var refNode = Long.parseLong(reader.getAttributeValue(null, "ref"));
                                way.addNode(idToNode.get(refNode));
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
                                    idToWay.put(way.getId(), way);
                                    if (way.hasType()) {
                                        mapData.addData(way);
                                    }
                                    way = null;
                                }
                                if (travelWay != null) {
                                    idToWay.put(travelWay.getId(), travelWay);
                                    mapData.addRoad(travelWay);
                                    travelWay = null;
                                }
                                break;

                            case "node":
                                if (addressNode != null) {
                                    mapData.addAddress(addressNode);
                                    addressNode = null;
                                }
                                node = null;
                                break;

                            case "relation":
                                if (relation != null) {
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
                travelWay.setMaxspeed(Integer.parseInt(v));
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
                if (v.equals("yes")) way.setType(k);
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
        if (v.equals("motorway"))  return true;
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
}

