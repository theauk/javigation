package bfst21.Osm_Elements;

/**
 * Interface used for elements that have coordinates.
 */
public interface Spatializable {

    float getxMax();

    float getxMin();

    float getyMax();

    float getyMin();

    float[] getCoordinates();
}
