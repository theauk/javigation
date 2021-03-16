package bfst21;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import bfst21.data.NonRoadData;
import bfst21.data.NonRoadElements;
import bfst21.data.RefData;
import bfst21.data.RoadData;
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
public class Creator {
    private NonRoadData nonRoadData;
    private NonRoadElements nonRoadElements;
    private RefData refData;
    private RoadData roadData;
    List<Way> roads;
    List<Way> footway;
    List<Way> bridges;
    float minx, miny, maxx, maxy;
    //ArrayList<Way> relation = new ArrayList<>();
    boolean iscoastline, isRoad, isPrimaryHighway, isBridge, isFootWay, ispedestrianRoad;
    boolean isRelation;

    public Creator() {}


    public void create(InputStream input) throws IOException, XMLStreamException {
        XMLStreamReader reader = XMLInputFactory
                .newInstance()
                .createXMLStreamReader(new BufferedInputStream(input));
        var idToNode = new HashMap<Long,Node>();
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
                            //way.setId(Long.parseLong(reader.getAttributeValue(null, "id")));
                            allBooleansFalse();
                            break;
                        case "tag":
                            var k = reader.getAttributeValue(null, "k");
                            var v = reader.getAttributeValue(null, "v");
                            if (k.equals("natural") && v.equals("coastline")) {
                                iscoastline = true;
                            } else if (k.equals("highway")){
                                if (v.equals("primary")) isPrimaryHighway = true;
                                if (v.equals("pedestrian")) ispedestrianRoad = true;
                                else isRoad = true;
                            }

                                break;
                                case "nd":
                                    var refNode = Long.parseLong(reader.getAttributeValue(null, "ref"));
                                    way.add(idToNode.get(refNode));
                                    break;
                        case "member":
                            if(isRelation){
                                var refWay = Long.parseLong(reader.getAttributeValue(null,"ref"));
                                //member.add(refWay);
                            }
                            break;
                            }
                            break;
                        case END_ELEMENT:
                            switch (reader.getLocalName()) {
                                case "way":
                                    if (iscoastline) coastlinesTemp.add(way);
                                    if (isRoad) roads.add(way);
                                    if (isPrimaryHighway) highWayTemp.add(way);
                                    if (isFootWay) footway.add(way);
                                    if (isBridge) bridges.add(way);
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
    }

}

    //create Node Object

    //create Way Object

    //create Road Object

    //Create Relation Object

    // add methods to the different Datasets
