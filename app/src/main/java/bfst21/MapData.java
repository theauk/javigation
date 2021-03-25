package bfst21;

import bfst21.Osm_Elements.Element;
import bfst21.Osm_Elements.NodeHolder;
import bfst21.data_structures.RTree;
import bfst21.view.CanvasBounds;

import java.util.ArrayList;
import java.util.List;

public class MapData
{
    private List<Element> mapData; //All data
    private RTree rTree;
    private List<NodeHolder> mapSegment; //Only content within bounds
    private float minX, minY, maxX, maxY;

    public MapData()
    {
        mapData = new ArrayList<>();
    }

    public void addData(List<Element> toAdd)
    {
        mapData.addAll(toAdd);
    }

    public void searchInData(CanvasBounds bounds)
    {
        mapSegment = rTree.search(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
    }

    public List<Element> getMapData()
    {
        return mapData;
    }

    public List<NodeHolder> getMapSegment()
    {
        return mapSegment;
    }

    public void setRTree(RTree rTree)
    {
        this.rTree = rTree;
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
