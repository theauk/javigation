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

    boolean iscoastline, isRoad;
    boolean iscycleAble, isbuilding;
    boolean isRelation;
    boolean isFerryRoute;
    boolean isAddress;

    private List<Element> roads;
    private List<Element> coastLines;
    private List<TravelWay> travelWays;
    private TravelWay travelWay;
    private List<Element> relations;
    private ArrayList<AddressNode> addressNodes;

    public Creator(MapData mapData, InputStream inputStream, long fileSize) {
        this.mapData = mapData;
        progressInputStream = new ProgressInputStream(inputStream);
        progressInputStream.addInputStreamListener(totalBytes -> updateProgress(totalBytes, fileSize));

        roads = new ArrayList<>();
        coastLines = new ArrayList<>();
        travelWays = new ArrayList<>();
        addressNodes = new ArrayList<>();
    }

    @Override
    protected Void call() throws Exception {
        create();
        return null;
    }

    private void create() throws XMLStreamException {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new BufferedInputStream(progressInputStream));

        AlternateBinarySearchTree<Long, Node> idToNode = new AlternateBinarySearchTree<>();
        AlternateBinarySearchTree<Long, Way> idToWay = new AlternateBinarySearchTree<>();
        AlternateBinarySearchTree<Long, AddressNode> idToAddressNode = new AlternateBinarySearchTree<>();

        Way way = null;
        Node node = null;
        TravelWay travelWay = null;
        String cycle = null; // the cycleproperties of the road comes before "highway" in the .osm files
        Relation relation = null;
        var member = new ArrayList<Long>();

        AddressNode addressNode = null;

        while (reader.hasNext()) {
            if(isCancelled()) return;   //Abort task
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
                                isAddress = false;
                                var id = Long.parseLong(reader.getAttributeValue(null, "id"));
                                var lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                                var lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                                node = new Node(id, lon, lat);
                                idToNode.put(id, node);
                                updateMessage("Loading: Nodes");
                                break;
                            case "way":
                                updateMessage("Loading: Ways");
                                travelWay = null;
                                way = new Way(Long.parseLong(reader.getAttributeValue(null, "id")));
                                allBooleansFalse();
                                break;
                            case "relation":
                                updateMessage("Loading: Relations");
                                member = new ArrayList<>();
                                relations = new ArrayList<>();
                                isRelation = true;
                                break;
                            case "tag":
                                var k = reader.getAttributeValue(null, "k");
                                var v = reader.getAttributeValue(null, "v");
                                switch (k) {
                                    case "natural":
                                        if (v.equals("coastline")) iscoastline = true;
                                        break;

                                    case "highway":
                                        travelWay = new TravelWay(way, v);
                                        isRoad = true;
                                        break;
                                    case "maxspeed":
                                        if (travelWay != null) {
                                            travelWay.setMaxspeed(Integer.parseInt(v));
                                        }
                                        break;
                                    case "name":
                                        if (travelWay != null) {
                                            travelWay.setName(v);
                                        }
                                        if (relation != null) {
                                            relation.setName(v);
                                        }
                                        break;
                                    case "oneway":
                                        if (v.equals("yes")) travelWay.setOnewayRoad(true);

                                    case "cycleway": // should this not be always cycleable unless it's trunk (motortrafikvej) or primary highway.
                                        if (!v.equals("no")) {
                                            cycle = v;
                                            iscycleAble = true;
                                        }
                                        break;

                                    // methods when encountering addressNodes in the .osm file.
                                    case "addr:city":
                                        addressNode = new AddressNode(node, v);
                                        isAddress = true;
                                        break;

                                    case "addr:housenumber":
                                        if (addressNode != null) {
                                            addressNode.setHousenumber((v));
                                        }
                                        break;

                                    case "addr:postcode":
                                        if (addressNode != null) {
                                            addressNode.setPostcode(Integer.parseInt(v.trim()));
                                        }
                                        break;

                                    case "addr:street":
                                        if (addressNode != null) {
                                            addressNode.setStreet(v);
                                        }

                                    case "building":
                                        if (v.equals("yes")) isbuilding = true;
                                        break;

                                    case "route":
                                        if (v.equals("ferry")) isFerryRoute = true;
                                        if (relation != null) {
                                            relation.setRoute(v);
                                        }
                                        break;
                                }
                                break;

                            case "nd":
                                var refNode = Long.parseLong(reader.getAttributeValue(null, "ref"));
                                way.addNode(idToNode.get(refNode));
                                break;

                            case "member":
                                if (isRelation) {
                                    //var refWay = Long.parseLong(reader.getAttributeValue(null,"ref"));
                                    //member.add(refWay);
                                }
                                break;
                        }
                        break;
                    case END_ELEMENT:
                        switch (reader.getLocalName()) {
                            case "way":
                                idToWay.put(way.getId(), way);
                                if (iscoastline) coastLines.add(way);
                                if (isRoad) {
                                    roads.add(way);
                                    travelWays.add(travelWay);
                                    // TODO: 26-03-2021 when all ways tagged as travelway are sure to have names this check is unessecary
                                    if(way.getNodes().get(0).getName() != null){
                                        mapData.addRoadsNodes(way.getNodes());
                                    }



                                }
                                if (iscycleAble) travelWay.setCycleway(cycle);
                                break;

                            case "node":
                                if (isAddress) addressNodes.add(addressNode);
                                break;
                        }
                        break;
                }
            }
        }
        mapData.addData(coastLines);
        mapData.addData(roads);
        updateMessage("");
        reader.close();
    }

    private void allBooleansFalse() {
        iscoastline = false;
        isRoad = false;
        iscycleAble = false;
        isbuilding = false;
    }

    private void AllBooleansRelationsFalse() {
        isFerryRoute = false;
    }
}

