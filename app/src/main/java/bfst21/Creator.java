package bfst21;

import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import bfst21.data_structures.BinarySearchTree;

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
// TODO: 19-03-2021 BST instead of HashMap :3
public class Creator {

    private Map map;
    private List<Element> roads;
    private List<Element> coastLines;

    List<Node> nodesInRoads = new ArrayList<>();
    float minx, miny, maxx, maxy;
    boolean iscoastline, isRoad;
    boolean isRelation;

    public Creator(Map map, InputStream input) throws XMLStreamException
    {
        this.map = map;
        roads = new ArrayList<>();
        coastLines = new ArrayList<>();

        create(input);
    }

    public void create(InputStream input) throws XMLStreamException
    {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new BufferedInputStream(input));

        BinarySearchTree<Long, Node> idToNode = new BinarySearchTree<>();
        Way way = null;
        Node node = null;

        while(reader.hasNext())
        {
            switch (reader.next())
            {
                case START_ELEMENT:
                    switch (reader.getLocalName())
                    {
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
                            node = new Node(id, lat, lon);
                            idToNode.put(id, node);
                            break;
                        case "way":
                            way = new Way();
                            allBooleansFalse();
                            way.setId(Long.parseLong(reader.getAttributeValue(null, "id")));
                            break;
                        case "tag":
                            var k = reader.getAttributeValue(null, "k");
                            var v = reader.getAttributeValue(null, "v");
                            switch(k){
                                case "natural":
                                    if(v.equals("coastline")) iscoastline = true;
                                    break;

                                case "highway":
                                    if (v.equals("primary")) isPrimaryHighway = true;
                                    if (v.equals("pedestrian")) ispedestrianRoad = true;
                                    if(v.equals("residential")) isresidentialRoad = true;
                                    if(v.equals("tertiary")) istertiary = true;
                                    if(v.equals("footway")) isFootWay = true;
                                    else isRoad = true; //if there's other roads, then add them to this list.
                                    break;
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
                            if (isRoad) addRoadToList(roads, way);
                            if (isPrimaryHighway) addRoadToList(highways, way);
                            if (isFootWay) addRoadToList(footway, way);
                            if(isresidentialRoad) addRoadToList(residentialRoads, way);
                            if (isBridge) bridges.add(way);
                            if (istertiary) addRoadToList(tertiary, way);

                            break;
                    }
                    break;
            }
        }

        map.addData(coastlines);
    }

    private void addRoadToList(List<Way> list, Way way){
        list.add(way);
        nodesInRoads.addAll(way.getNodes()); // adding the Nodes in a Way that contains a road to this list;

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
