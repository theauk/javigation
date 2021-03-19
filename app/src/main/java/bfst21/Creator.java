package bfst21;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import bfst21.data.NonRoadData;
import bfst21.data.NonRoadElements;
import bfst21.data.RefData;
import bfst21.data.RoadData;
import bfst21.data_structures.BinarySearchTree;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;


/*
Creates Objects such as Nodes, Ways and Relations from the .osm file given from the Loader.
 */
// TODO: 19-03-2021 BST instead of HashMap :3
public class Creator {

    List<Way> roads = new ArrayList<>();
    List<Way> residentialRoads = new ArrayList<>();
    List<Way> highways = new ArrayList<>();
    ArrayList<Way> coastlines = new ArrayList<>();
    private NonRoadData nonRoadData;
    private NonRoadElements nonRoadElements;
    private RefData refData;
    private RoadData roadData;
    List<Way> footway = new ArrayList<>();
    List<Way> tertiary = new ArrayList<>();
    List<Way> bridges = new ArrayList<>();
    List<Node> nodesInRoads = new ArrayList<>();
    float minx, miny, maxx, maxy;
    boolean iscoastline, isRoad, isPrimaryHighway, isBridge, isFootWay, ispedestrianRoad, isresidentialRoad;
    boolean istertiary;
    boolean isRelation;
    ArrayList<Way> relation = new ArrayList<>();

    public Creator(InputStream input) throws IOException, XMLStreamException {
        create(input);
    }

    public void create(InputStream input) throws IOException, XMLStreamException {
        XMLStreamReader reader = XMLInputFactory
                .newInstance()
                .createXMLStreamReader(new BufferedInputStream(input));
        BinarySearchTree<Long,Node> idToNode = new BinarySearchTree<>();
        Way way = null;
        Node node = null;
        var member = new ArrayList<Long>();
        var coastlinesTemp = new ArrayList<Way>();
        var highWayTemp = new ArrayList<Way>();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "bounds":
                            minx = Float.parseFloat(reader.getAttributeValue(null, "minlon"));
                            maxx = Float.parseFloat(reader.getAttributeValue(null, "maxlon"));
                            maxy = Float.parseFloat(reader.getAttributeValue(null, "minlat")) / -0.56f;
                            miny = Float.parseFloat(reader.getAttributeValue(null, "maxlat")) / -0.56f;
                            break;
                        case "relation":
                            // adding memebers like Node and Way into the list
                            break;
                        case "node":
                            var id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            var lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                            var lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                            idToNode.put(id, new Node(id,lat,lon));
                            break;
                        case "way":
                            way = new Way();
                            allBooleansFalse();
                            way.setId(Long.parseLong(reader.getAttributeValue(null, "id")));
                            break;
                        case "tag":
                            var k = reader.getAttributeValue(null, "k");
                            var v = reader.getAttributeValue(null, "v");
                            if (k.equals("natural") && v.equals("coastline")) {
                                iscoastline = true;
                            } else if (k.equals("highway")){
                                if (v.equals("primary")) isPrimaryHighway = true;
                                if (v.equals("pedestrian")) ispedestrianRoad = true;
                                if(v.equals("residential")) isresidentialRoad = true;
                                if(v.equals("tertiary")) istertiary = true;
                                if(v.equals("footway")) isFootWay = true;
                                else isRoad = true; //if there's other roads, then add them to this list.
                            }

                                break;
                                case "nd":
                                    var refNode = Long.parseLong(reader.getAttributeValue(null, "ref"));
                                    way.add(idToNode.get(refNode));


                                    break;
                        case "member":
                            if(isRelation){
                                var refWay = Long.parseLong(reader.getAttributeValue(null,"ref"));
                                member.add(refWay);
                            }
                            break;
                            }
                            break;
                        case END_ELEMENT:
                            switch (reader.getLocalName()) {
                                case "way":
                                    if (iscoastline) coastlines.add(way);
                                    if (isRoad) {
                                        roads.add(way);
                                        nodesInRoads.addAll(way.getNodes()); // adding the Nodes in a Way that contains a road to this list;
                                    }
                                    if (isPrimaryHighway) {
                                        highways.add(way);
                                        nodesInRoads.addAll(way.getNodes()); // adding the Nodes in a Way that contains a road to this list;
                                    }
                                    if (isFootWay) {
                                        footway.add(way);
                                        nodesInRoads.addAll(way.getNodes()); // adding the Nodes in a Way that contains a road to this list;
                                    }
                                    if(isresidentialRoad){
                                        residentialRoads.add(way);
                                        nodesInRoads.addAll(way.getNodes()); // adding the Nodes in a Way that contains a road to this list;
                                    }
                                    if (isBridge) bridges.add(way);
                                    if (istertiary){
                                        tertiary.add(way);
                                        nodesInRoads.addAll(way.getNodes());
                                    }
                                    break;
                            }
                            break;
                    }
            }
        }
    private void allBooleansFalse(){
        iscoastline = false;
        isRoad = false;
        isPrimaryHighway = false;
        isBridge = false;
        isFootWay = false;
        ispedestrianRoad = false;
        isresidentialRoad = false;
        istertiary = false;
    }
    public void printNodeRefnumbers(){
        System.out.println("references for nodes in ways:");
        for(Node node: nodesInRoads){
            System.out.println(node.getID());
        }
    }

    public float getMaxx() {
        return maxx;
    }

    public float getMaxy() {
        return maxy;
    }

    public float getMinx() {
        return minx;
    }

    public float getMiny() {
        return miny;
    }

    public List<Way> getHighways() {
        return highways;
    }

    public List<Way> getBridges() {
        return bridges;
    }

    public List<Way> getFootway() {
        return footway;
    }

    public List<Way> getResidentialRoads() {
        return residentialRoads;
    }

    public List<Way> getTertiary() {
        return tertiary;
    }

    public List<Node> getNodesInRoads(){
        return nodesInRoads;
    }

    public List<Way> getRoads() {
        return roads;
    }

    public ArrayList<Way> getCoastlines() {
        return coastlines;
    }


}


    //create Node Object

    //create Way Object

    //create Road Object

    //Create Relation Object

    // add methods to the different Datasets
