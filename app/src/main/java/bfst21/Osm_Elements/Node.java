package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

public class Node extends Element{
    private List<Way> partOfWays;
    private boolean isAddress;

    private String housenumber;
    private String city;
    private int postcode;
    private String street;

    public Node(long id, float lon, float lat) {
        super(id);
        this.xMin = lon;
        this.xMax = lon;
        this.yMin = -lat / 0.56f;
        this.yMax = -lat / 0.56f;

    }

    public Node(long id, float lon, float lat, boolean nodeFromNode) {
        super(id);
        if (nodeFromNode) {
            this.xMin = lon;
            this.xMax = lon;
            this.yMin = lat;
            this.yMax = lat;
        }
    }

    public Node(float lon, float lat) { // TODO: 3/28/21 for Rtree debug mode where the y should not be converted
        super(0);
        this.xMin = lon;
        this.xMax = lon;
        this.yMin = lat;
        this.yMax = lat;
    }
    public void addReferenceToHighWay(Way way){
        if(partOfWays == null){
         partOfWays = new ArrayList<>();
        }
        partOfWays.add(way);
    }

    public List<Way> getReferencedHighWays(){
        return partOfWays;
    }

    @Override
    public void draw(GraphicsContext gc) {

    }

    public boolean isAddress(){
        return isAddress;
    }

    public String getHousenumber() {
        return housenumber;
    }

    public void setHousenumber(String housenumber) {
        this.housenumber = housenumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getPostcode() {
        return postcode;
    }

    public void setPostcode(int postcode) {
        this.postcode = postcode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        isAddress = true;
        this.street = street;
    }

}
