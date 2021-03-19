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
                            map.setMinX(Float.parseFloat(reader.getAttributeValue(null, "minlon")));
                            map.setMaxX(Float.parseFloat(reader.getAttributeValue(null, "maxlon")));
                            map.setMaxY(Float.parseFloat(reader.getAttributeValue(null, "minlat")) / -0.56f);
                            map.setMinY(Float.parseFloat(reader.getAttributeValue(null, "maxlat")) / -0.56f);
                            break;
                        case "relation":
                            // adding memebers like Node and Way into the list
                            break;
                        case "node":
                            var id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            var lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                            var lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                            node = new Node(id, lon, lat);
                            idToNode.put(id, node);
                            break;
                        case "way":
                            way = new Way(Long.parseLong(reader.getAttributeValue(null, "id")));
                            allBooleansFalse();
                            break;
                        case "tag":
                            var k = reader.getAttributeValue(null, "k");
                            var v = reader.getAttributeValue(null, "v");
                            switch(k){
                                case "natural":
                                    if(v.equals("coastline")) iscoastline = true;
                                    break;

                                case "highway":
                                    isRoad = true;
                                    break;
                            }
                            break;

                        case "nd":
                            var refNode = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            way.addNode(idToNode.get(refNode));
                            break;

                        case "member":
                            if(isRelation){
                                //var refWay = Long.parseLong(reader.getAttributeValue(null,"ref"));
                                //member.add(refWay);
                            }
                            break;
                    }
                    break;
                case END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "way":
                            if (iscoastline) coastLines.add(way);
                            if (isRoad) roads.add(way);
                            break;
                    }
                    break;
            }
        }

        map.addData(coastLines);
        map.addData(roads);
    }

    private void allBooleansFalse() {
        iscoastline = false;
        isRoad = false;
    }
}

