package bfst21;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;
import bfst21.data_structures.*;
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


    public Creator(MapData mapData, InputStream inputStream, long fileSize) {
        this.mapData = mapData;
        progressInputStream = new ProgressInputStream(inputStream);
        progressInputStream.addInputStreamListener(totalBytes -> updateProgress(totalBytes, fileSize));
        nodesNotCreateKeys = new HashSet<>();
        nodesNotCreateValues = new HashSet<>();
        setupNodesNotCreate();
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

        KDTree<Node> highWayRoadNodes = new KDTree<>(2,4);
        RTree rTree = new RTree(1, 30, 4);
        RoadGraph roadGraph = new RoadGraph();
        AddressTriesTree addressTree = new AddressTriesTree();

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
                                break;

                            case "way":

                                var idWay = Long.parseLong(reader.getAttributeValue(null, "id"));
                                way = new Way(idWay);
                                idToWay.put(way);
                                break;

                            case "relation":

                                relation = new Relation(Long.parseLong(reader.getAttributeValue(null, "id")));
                                break;

                            case "tag":
                                var k = reader.getAttributeValue(null, "k");
                                var v = reader.getAttributeValue(null, "v");

                                if(node != null){
                                    // TODO: 09-04-2021 out commented node deletion 
                                    //if(checkNodesNotCreate(k,v)) node = null;
                                    checkAddressNode(k,v,node);
                                    break;
                                }

                                if (way != null) {
                                    checkWay(k, v, way);
                                    break;
                                }

                                if (relation != null) {
                                    checkRelation(k, v, relation);
                                    break;
                                }

                                break;

                            case "nd":
                                if(way != null) {
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
                            case "node":
                                updateMessage("Loading: Nodes");
                                if (node != null) {
                                    if(node.isAddress()){
                                        addressTree.put(node);
                                    } else{
                                        idToNode.put(node);
                                    }
                                    node = null;
                                }
                                break;

                            case "way":
                                updateMessage("Loading: Ways");
                                if (way != null) {
                                    idToWay.put(way);
                                    if (way.hasType()) {
                                        rTree.insert(way);
                                    }
                                    if(way.isHighWay() && way.hasName()){
                                        highWayRoadNodes.addAll(way.getNodes());
                                    }
                                    way = null;
                                }
                                break;


                            case "relation":
                                updateMessage("Loading: Relations");
                                if (relation != null) {
                                    if(relation.hasType()){
                                        if(relation.getType().equals("restriction")){
                                            roadGraph.addRestriction(relation);
                                        }else rTree.insert(relation);
                                    }
                                }
                                relation = null;
                                break;
                        }
                        break;
                }
            }
        }
        mapData.addDataTrees(highWayRoadNodes, rTree, roadGraph, addressTree);
        updateMessage("");
        reader.close();
    }

    private void checkRelation(String k, String v, Relation relation) {
        switch (k) {

            case "restriction":
                relation.setType(k);
                relation.setRestriction(v);
                break;
            case "name":
                relation.setName(v);
                break;
            case "bridge":
                relation.setType("bridge");
                break;
            case "type":
                if(v.equals("multipolygon")) relation.isMultiPolygon();
                break;
            case "building":
                relation.setType(k);
                break;
            case "natural":
                if(v.equals("water")) relation.setType(v);
                break;
            // TODO: 07-04-2021 park green areas; 
        }
    }


    private void checkWay(String k, String v, Way way) {
        switch (k) {
            case "natural":
                if (v.equals("coastline")) way.setType(v);
                break;

            case "building":
                if(v.equals("yes")) way.setType(v);
                break;

            case "leisure":
                if (v.equals("park")) way.setType(v);
                break;
            case "highway":
                checkHighWayType(way,v);
                break;



            case "name":
                way.setName(v);
                break;
        }
        checkHighWayAttributes(k,v,way);
    }

    private void checkHighWayAttributes(String k, String v, Way way) {
        switch(k) {
            case "oneway":
                if (v.equals("yes")) way.setOnewayRoad();
                break;
            case "cycleway":
                if (!v.equals("no")) way.setNotCycleable();
                break;

            case "maxspeed":
                try {
                    way.setMaxspeed(Integer.parseInt(v));
                } catch (NumberFormatException e) {
                }
                break;

            case "source:maxspeed":
                if (v.equals("DK:urban")) way.setMaxspeed(50);
                if (v.equals("DK:rural")) way.setMaxspeed(80);
                if (v.equals("DK:motorway")) way.setMaxspeed(130);
                break;

                case "junction":
                    if(v.equals("roundabout")) way.setOnewayRoad();
                    // TODO: 06-04-2021 rundkørsel, what to do about that.
                    break;

            case "bicycle_road":
                way.setNotDriveable();
                way.setNotWalkable();
                break;

            case "turn":
                //The key turn can be used to specify the direction in which a way or a lane will lead.
                // TODO: 06-04-2021 could be usefull? unsure
        }
    }

    private void checkAddressNode(String k, String v, Node addressNode) {
        switch (k) {
            case "addr:city" -> addressNode.setCity(v);
            case "addr:housenumber" -> addressNode.setHousenumber((v));
            case "addr:postcode" -> addressNode.setPostcode(Integer.parseInt(v.trim()));
            case "addr:street" -> addressNode.setStreet(v);
        }
    }

    private void checkHighWayType(Way way, String v) {

        if(v.equals("motorway")){
            way.setType(v,true);
            way.setMaxspeed(130);
            return;
        }

        if(v.equals("living_street")){
            way.setType(v,true);
            way.setMaxspeed(15);
            return;
        }

        if(v.equals("unclassified")){
            way.setType(v,true);
            way.setMaxspeed(50);
            return;
        }

        if(v.equals("residential")){
            way.setType(v,true);
            way.setMaxspeed(50);
            return;
        }

        if(v.contains("trunk")){
            //motortrafikvej
            way.setType(v,true);
            way.setMaxspeed(80);
            return;
        }

        if(restOfHighWays(v)) way.setType(v,true);
    }

    public boolean restOfHighWays(String v){
        if(v.equals("primary")) return true;

        if(v.contains("secondary")) return true;

        if(v.contains("link")) return true;

        if(v.contains("tertiary")) return true;

        if(v.equals("pedestrian") || v.equals("footway") || v.equals("cycleway"))
            return true;
        else  return false;
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
        return nodesNotCreateKeys.contains(k) || nodesNotCreateValues.contains(v);
    }
}