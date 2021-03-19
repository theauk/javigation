package bfst21.Osm_Elements;

public class Node extends Element{
    private float x, y;
    private transient long id;
    

    public Node(long id, float lat, float lon) {
        this.id = id;
        this.x = lon;
        this.y = -lat/0.56f;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

	public long getID() {
		return id;
	}

    
   

    
    
}
