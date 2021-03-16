package bfst21.Osm_Elements;

public class Node extends Element{
    private float x, y;
    private transient long id;
    private Node leftChild;
    private Node rightChild;
    private Boolean OnXAxis;

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

    public Node getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(Node leftChild) {
        this.leftChild = leftChild;
    }

    public Node getRightChild() {
        return rightChild;
    }

    public void setRightChild(Node rightChild) {
        this.rightChild = rightChild;
    }

    public Boolean IsOnXAxis() {
        return OnXAxis;
    }

    public void setIsOnXAxis(Boolean xAxis) {
        this.OnXAxis = xAxis;
    }

   

    
    
}
