package bfst21;

import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Relation;
import bfst21.Osm_Elements.Way;
import bfst21.data_structures.*;
import bfst21.file_io.ProgressInputStream;
import bfst21.utils.MapMath;
import javafx.concurrent.Task;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Creates a complete MapData object containing Nodes, Ways and Relations from an InputStream containing either a .osm file or a binary {@link MapData}.
 */
public class Creator extends Task<MapData> {
    private final ProgressInputStream progressInputStream;
    private final boolean[] touched = new boolean[3];
    private final boolean binary;
    private MapData mapData;
    private HashSet<String> nodesNotCreateKeys;
    private HashSet<String> nodesNotCreateValues;
    private String city, streetName, houseNumber, name;
    private Integer postcode;
    private int bottomLayer, layerTwo, layerThree, layerFour, topLayer;
    private HashMap<String, Integer> typeToLayer;
    private Relation coastLines;
    private HashMap<Element, String> elementToText;
    private boolean isFoot = false;
    private boolean motorWayJunctionNode = false;
    private HashMap<Node, String> destinationInfoMap = new HashMap<>();
    private String motorwayExitInfo;

    public Creator(InputStream inputStream, long fileSize, boolean binary) {
        mapData = new MapData();
        progressInputStream = new ProgressInputStream(inputStream);
        progressInputStream.addInputStreamListener(totalBytes -> updateProgress(totalBytes, fileSize));
        this.binary = binary;
        nodesNotCreateKeys = new HashSet<>();
        nodesNotCreateValues = new HashSet<>();
        bottomLayer = 0;
        layerTwo = 1;
        layerThree = 2;
        layerFour = 3;
        topLayer = 4;
        typeToLayer = new HashMap<>();
        coastLines = new Relation(0);
        coastLines.setType("coastlines");
        elementToText = new HashMap<>();

        setupNodesNotCreate();
        setUpTypeToLayer();
    }

    @Override
    protected MapData call() throws Exception {
        if (!binary) createMapData();
        else createBinaryMapData();

        return mapData;
    }

    /**
     * Creates a MapData object from a binary MapData file.
     *
     * @throws IOException            if the file is not found or the process is interrupted.
     * @throws ClassNotFoundException if the serialized object recreation process can't find the corresponding class.
     */
    private void createBinaryMapData() throws IOException, ClassNotFoundException {
        updateMessage("Loading...");
        ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(progressInputStream));
        mapData = (MapData) objectInputStream.readObject();
        updateMessage("Finalizing...");
        objectInputStream.close();
    }

    private void createMapData() throws XMLStreamException {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new BufferedInputStream(progressInputStream));

        BinarySearchTree<Node> idToNode = new BinarySearchTree<>();
        BinarySearchTree<Way> idToWay = new BinarySearchTree<>();

        Way way = null;
        Node node = null;
        Relation relation = null;

        KDTree<Node> highWayRoadNodes = new KDTree<>(2, 4);
        RTree rTree = new RTree(1, 100, 4, topLayer); //remove nodes
        AddressTriesTree addressTree = new AddressTriesTree();
        ElementToElementsTreeMap<Node, Way> nodeToWayMap = new ElementToElementsTreeMap<>();
        ElementToElementsTreeMap<Node, Relation> nodeToRestriction = new ElementToElementsTreeMap<>();
        ElementToElementsTreeMap<Way, Relation> wayToRestriction = new ElementToElementsTreeMap<>();

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

                                if (!touched[0]) {
                                    touched[0] = true;
                                    updateMessage("Loading: Nodes");
                                }
                                break;

                            case "way":
                                var idWay = Long.parseLong(reader.getAttributeValue(null, "id"));
                                way = new Way(idWay);
                                idToWay.put(way);

                                if (!touched[1]) {
                                    updateMessage("Loading: Ways");
                                    touched[1] = true;
                                }
                                break;

                            case "relation":
                                relation = new Relation(Long.parseLong(reader.getAttributeValue(null, "id")));

                                if (!touched[2]) {
                                    updateMessage("Loading: Relations");
                                    touched[2] = true;
                                }
                                break;

                            case "tag":
                                var k = reader.getAttributeValue(null, "k");
                                var v = reader.getAttributeValue(null, "v");

                                if (node != null) {
                                    // TODO: 09-04-2021 out commented node deletion
                                    //if(checkNodesNotCreate(k,v)) node = null;
                                    checkMotorWayExitNode(k, v);
                                    checkAddressNode(k, v, node);
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
                                        if (idToWay.get(refR) != null) {
                                            relation.addWay(idToWay.get(refR)); // TODO: 4/14/21 fix
                                            relation.addAllNodes(idToWay.get(refR).getNodes());
                                        }
                                    }
                                    if (type.equals("node")) {
                                        relation.addNode(idToNode.get(refR));
                                    }
                                    var role = (reader.getAttributeValue(null, "role"));
                                    if (idToWay.get(refR) != null) {
                                        if (role.equals("outer")) {
                                            relation.addInnerOuterWay(idToWay.get(refR), false);
                                        }
                                        if (role.equals("inner")) {
                                            relation.addInnerOuterWay(idToWay.get(refR), true);
                                        }
                                        if (role.equals("to")) {
                                            relation.setTo(idToWay.get(refR));
                                        }
                                        if (role.equals("from")) {
                                            relation.setFrom(idToWay.get(refR));
                                        }

                                    }
                                    if (role.equals("via")) {
                                        if (idToNode.get(refR) != null) relation.setViaNode(idToNode.get(refR));
                                        else if (idToWay.get(refR) != null) relation.setViaWay(idToWay.get(refR));
                                    }
                                }
                                break;
                        }
                        break;
                    case END_ELEMENT:
                        switch (reader.getLocalName()) {
                            case "node":
                                if (node != null) {
                                    if (motorWayJunctionNode) {
                                        motorWayJunctionNode = false;
                                        destinationInfoMap.put(node, motorwayExitInfo);
                                        motorwayExitInfo = null;
                                    }

                                    if(isAddress()){
                                        addressTree.put(node, city, streetName, postcode, houseNumber);
                                        node.setLayer(4);
                                        nullifyAddress();
                                    } else {
                                        if (node.hasType()) rTree.insert(node);
                                        idToNode.put(node);
                                    }
                                    node = null;
                                }
                                break;

                            case "way":
                                if (way != null) {
                                    idToWay.put(way);
                                    if (way.hasType()) {
                                        rTree.insert(way);

                                    }
                                    if (way.isHighWay()) {
                                        nodeToWayMap.putAll(way.getNodes(), way);
                                        if (way.hasName()) {
                                            highWayRoadNodes.addAll(way.getNodes());
                                        }
                                    }
                                    way = null;
                                }
                                break;

                            case "relation":
                                if (relation != null) {
                                    if (relation.hasType()) {
                                        if (relation.getType().equals("restriction")) {
                                            if (relation.getViaNode() != null)
                                                nodeToRestriction.put(relation.getViaNode(), relation);
                                            else if (relation.getViaWay() != null)
                                                wayToRestriction.put(relation.getViaWay(), relation);
                                        } else {
                                            rTree.insert(relation);
                                        }
                                    }
                                }
                                relation = null;
                                break;
                        }
                        break;
                }
            }
        }
        coastLines.mergeWays();
        mapData.setCoastlines(coastLines);
        mapData.setElementToText(elementToText);
        updateMessage("Finalizing...");
        mapData.addDataTrees(highWayRoadNodes, rTree, nodeToRestriction, wayToRestriction, addressTree, nodeToWayMap);
        reader.close();
    }

    private void checkMotorWayExitNode(String k, String v) {
        if (k.equals("highway") && v.equals("motorway_junction")) {
            motorWayJunctionNode = true;
        } else if (motorWayJunctionNode) {
            if (k.equals("name")) {
                motorwayExitInfo = v;
            } else if (k.equals("ref")) {
                if (motorwayExitInfo != null) motorwayExitInfo = "Exit " + v + "-" + motorwayExitInfo;
                else motorwayExitInfo = "Exit " + v;
            }
        }
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
            case "building":
                relation.setType((k), typeToLayer.get(k));
                break;
            case "type":
                if (v.equals("multipolygon")) relation.setIsMultiPolygon();
                break;
            case "natural":
                if (v.equals("water")) {
                    relation.setType((v), typeToLayer.get(v));
                    break;
                }
                if (v.equals("scrub")) {
                    relation.setType(("dark_green"), typeToLayer.get("dark_green"));
                    break;
                }
                break;
            case "leisure":
                if (v.equals("park")) {
                    relation.setType((v), typeToLayer.get(v));
                }
                break;

            case "landuse":
                if (v.equals("recreation_ground")) {
                    relation.setType(("light_green"), typeToLayer.get("light_green"));
                    break;
                }
                if (v.equals("grass")) {
                    relation.setType(("park"), typeToLayer.get("park"));
                    break;
                }
                break;
        }
    }

    private void checkWay(String k, String v, Way way) {
        switch (k) {
            case "amenity":
                if (v.equals("parking")) {
                    way.setType("asphalt", typeToLayer.get("asphalt"));
                }
                break;

            case "natural":
                if (v.equals("water") || v.equals("wetland")) {
                    way.setType((v), typeToLayer.get(v));
                    break;
                }
                if (v.equals("coastline")) {
                    //way.setType((v),typeToLayer.get(v));
                    coastLines.addWay(way);
                    break;
                }
                if (v.equals("scrub") || v.equals("wood")) {
                    way.setType(("dark_green"), typeToLayer.get("dark_green"));
                    break;
                }
                break;

            case "building":
                way.setType(k, typeToLayer.get(k));
                break;

            case "man_made":
                if (v.equals("pier") || v.equals("bridge")) {
                    way.setType(k, typeToLayer.get(k));
                    break;
                }
                break;

            case "leisure":
                if (v.equals("park") || v.equals("garden") || v.equals("playground")) {
                    way.setType("park", typeToLayer.get("park"));
                    break;
                }
                break;

            case "landuse":
                if (v.equals("forest")) {
                    way.setType(("dark_green"), typeToLayer.get("dark_green"));
                    break;
                }
                if (v.equals("grass") || v.equals("meadow")) {
                    way.setType(("park"), typeToLayer.get("park"));
                    break;
                }
                if (v.equals("farmland")) {
                    way.setType(v, typeToLayer.get(v));
                    break;
                }
                break;

            case "highway":
                checkHighWayType(way, v);
                break;

            case "name":
                way.setName(v);
                break;

            case "railway":
                way.setType(v, typeToLayer.get("railway"));
                break;

            case "ref":
                if (way.getName() == null && way.getType() != null && !way.getType().equals("roundabout")) way.setName(fixNumberWayName(v));
                break;

            case "route":
                if (v.equals("ferry")) {
                    way.setType("ferry", typeToLayer.get("ferry"));
                    way.setAsHighWay();
                }
                break;

            case "tunnel":
                if (v.equals("yes")) way.setType(null);
                break;

        }
        checkHighWayAttributes(k, v, way);
    }

    private String fixNumberWayName(String v) {
        String[] wayNumbers = v.split(";");
        String[] names = new String[wayNumbers.length];

        for (int i = 0; i < wayNumbers.length; i++) {
            if (wayNumbers[i].length() < 3) names[i] = "Main Road " + wayNumbers[i];
            else if (wayNumbers[i].matches(".*[0-9]+.*") && wayNumbers[i].matches(".*[A-Za-z]+.*")) names[i] = "Highway " + wayNumbers[i];
            else names[i] = "Secondary Road " + wayNumbers[i];
        }

        return String.join("/", names);

    }

    private void checkHighWayAttributes(String k, String v, Way way) {
        switch (k) {
            case "oneway":
                if (v.equals("yes")) way.setOnewayRoad();
                break;

            case "oneway:bicycle":
                if (v.equals("yes")) way.setOnewayBikeRoad();
                break;

            case "cycleway":
                if (v.equals("no")) way.setNotCycleable();
                break;

            case "bicycle":
                if (v.equals("no")) way.setNotCycleable();
                break;

            case "maxspeed":
                try {
                    way.setMaxSpeed(Integer.parseInt(v));
                } catch (NumberFormatException e) {
                }
                break;

            case "source:maxspeed":
                if (v.equals("DK:urban")) way.setMaxSpeed(50);
                if (v.equals("DK:rural")) way.setMaxSpeed(80);
                if (v.equals("DK:motorway")) way.setMaxSpeed(130);
                break;

            case "junction":
                if (v.equals("roundabout")) {
                    way.setOnewayRoad();
                    way.setType("roundabout");
                }
                break;

            case "bicycle_road":
                way.setNotDriveable();
                way.setNotWalkable();
                break;

            case "foot":
                if (v.equals("yes")) isFoot = true;
                break;

            case "service":
                if (v.equals("driveway")) {
                    way.setNotDriveable();
                    way.setNotCycleable();
                    way.setNotWalkable();
                }
                break;

            case "duration":
                if (v.matches("[0-9]{1,2}:[0-9]{1,2}")) {
                    double duration = MapMath.colonTimeToHours(v);
                    double distance = MapMath.getTotalDistance(way.getNodes());
                    way.setMaxSpeed(distance / duration);
                }
                break;

            case "toll":
                /*if (v.equals("yes")) way.setType(way.getType() + "_toll"); // TODO: 4/29/21 fix problem is I cannot change type as it wont draw them then
                break;*/
        }
    }

    private void checkAddressNode(String k, String v, Node node) {
        switch (k) {
            case "addr:city":
                city = v;
                break;
            case "addr:housenumber":
                houseNumber = v;
                break;
            case "addr:postcode" :
                postcode = Integer.parseInt(v.trim());
                break;
            case "addr:street":
                streetName = v;
                break;
            case "name" :
                name = v;
                break;
            case "place":
                if(v.equals("city") || v.equals("town") || v.equals("village") || v.equals("hamlet")) {
                    node.setType(v,typeToLayer.get("text"));
                    elementToText.put(node, name);
                }

                break;
        }
    }

    private void nullifyAddress() {
        city = null;
        houseNumber = null;
        postcode = null;
        streetName = null;
    }

    private boolean isAddress() {
        if (city == null) return false;
        if (postcode == null) return false;
        if (streetName == null) return false;
        if (houseNumber == null) return false;
        return true;
    }

    private void checkHighWayType(Way way, String v) {

        if (v.equals("motorway")) {
            way.setType(v, true, isFoot);
            way.setMaxSpeed(130);
            return;
        }

        if (v.equals("living_street")) {
            way.setType(v, true, isFoot);
            way.setMaxSpeed(15);
            return;
        }

        if (v.equals("unclassified")) {
            way.setType(v, true, isFoot);
            way.setMaxSpeed(50);
            return;
        }

        if (v.equals("residential")) {
            way.setType(v, true, isFoot);
            way.setMaxSpeed(50);
            return;
        }

        if (v.equals("service")) {
            way.setType(v, true, isFoot);
            way.setMaxSpeed(50);
            return;
        }

        if (v.contains("trunk")) {
            //motortrafikvej
            way.setType(v, true, isFoot);
            way.setMaxSpeed(80);
            return;
        }

        if (v.equals("motorway_link")) {
            for(Node n : way.getNodes()) {
                if (destinationInfoMap.get(n) != null) {
                    way.setName(destinationInfoMap.get(n));
                    break;
                }
            }
        }

        if (restOfHighWays(v)) way.setType(v, true, isFoot);

        isFoot = false;
    }

    public boolean restOfHighWays(String v) {
        if (v.equals("primary")) return true;

        if (v.contains("secondary")) return true;

        if (v.contains("link")) return true;

        if (v.contains("tertiary")) return true;

        if (v.equals("pedestrian") || v.equals("footway") || v.equals("cycleway") || v.equals("steps") || v.equals("path"))
            return true;
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

    private void setUpTypeToLayer() {
        typeToLayer.put("water", bottomLayer);
        typeToLayer.put("light_green", bottomLayer);

        typeToLayer.put("park", layerTwo);
        typeToLayer.put("wetland", layerTwo);
        typeToLayer.put("man_made", layerTwo);
        typeToLayer.put("farmland", layerTwo);
        typeToLayer.put("asphalt", layerTwo);
        typeToLayer.put("railway", layerTwo);
        typeToLayer.put("ferry", layerTwo);

        typeToLayer.put("dark_green", layerThree);
        typeToLayer.put("bridge", layerThree);
        typeToLayer.put("building", layerThree);

        typeToLayer.put("coastline", layerFour);

        typeToLayer.put("text", topLayer);
    }
}