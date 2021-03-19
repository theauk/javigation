package bfst21;

import bfst21.Osm_Elements.Element;

import java.util.ArrayList;
import java.util.List;

public class Map
{
    private List<Element> mapData; //All data

    public Map()
    {
        mapData = new ArrayList<>();
    }

    public void addData(List<Element> toAdd)
    {
        mapData.addAll(toAdd);
    }

    public List<Element> search(/*BOX: X, Y, Width, Height???*/)
    {
        /*
        USE BINARY TREE
         */
        return null;
    }
}
