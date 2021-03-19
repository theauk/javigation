package bfst21;

import bfst21.Osm_Elements.Element;

import java.util.ArrayList;
import java.util.List;

public class Map
{
    private List<Element> mapData; //All data
    private float minX, minY, maxX, maxY;

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

    public List<Element> getMapData()
    {
        return mapData;
    }

    public float getMinX() {
        return minX;
    }

    public void setMinX(float minX) {
        this.minX = minX;
    }

    public float getMinY() {
        return minY;
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    public float getMaxX() {
        return maxX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    public float getMaxY() {
        return maxY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }
}
