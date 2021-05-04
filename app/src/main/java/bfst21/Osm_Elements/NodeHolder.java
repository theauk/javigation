package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class NodeHolder extends Element implements Serializable {
    @Serial
    private static final long serialVersionUID = 4506251123911227447L;
    protected float xMax;
    protected float yMax;

    protected List<Node> nodes = new ArrayList<>();

    public NodeHolder(long id) {
        super(id);
    }

    public NodeHolder() {

    }

    @Override
    public float getxMax() {
        return xMax;
    }
    
    @Override
    public float getyMax() {
        return yMax;
    }

    @Override
    public float[] getCoordinates() {
        return new float[]{xMin, xMax, yMin, yMax};
    }

    public void addNode(Node node) {
        if (node != null) {
            nodes.add(node);
            checkMaxAndMin(node);
        }
    }

    public void addAllNodes(List<Node> nodes) {
        for (Node n : nodes) {
            addNode(n);
        }
    }

    public void addNodeBetweenIndices(Node node, int index) {
        // not necessary to update max and min as the node is on a line between i and j nodes.
        nodes.add(index, node);
    }

    public void removeNodeFromIndex(int i) {
        nodes.remove(i);
    }

    protected void checkMaxAndMin(Node node) {
        if (nodes.size() > 1) {
            checkX(node.getxMin());
            checkY(node.getyMin());
        } else {
            xMin = node.getxMin();
            xMax = node.getxMin();
            yMin = node.getyMin();
            yMax = node.getyMin();
        }
    }

    protected void checkX(float xValue) {
        if (xValue > xMax) {
            xMax = xValue;
        } else if (xValue < xMin) {
            xMin = xValue;
        }
    }

    protected void checkY(float yValue) {
        if (yValue > yMax) {
            yMax = yValue;
        } else if (yValue < yMin) {
            yMin = yValue;
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.moveTo(nodes.get(0).getxMin(), nodes.get(0).getyMin());
        for (var node : nodes) {
            gc.lineTo(node.getxMin(), node.getyMin());
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Node first() {
        return nodes.get(0);
    }

    public Node last() {
        return nodes.get(nodes.size() - 1);
    }

    public Node getNextNode(Node currentNode) {
        Node nextNode = null;
        for (int i = 0; i < nodes.size() - 1; i++) {
            if (nodes.get(i) == currentNode) {
                nextNode = nodes.get(i + 1);
                break;
            }
        }
        return nextNode;
    }

    public Node getPreviousNode(Node currentNode) {
        Node previousNode = null;
        for (int i = 1; i < nodes.size(); i++) {
            if (nodes.get(i) == currentNode) {
                previousNode = nodes.get(i - 1);
                break;
            }
        }
        return previousNode;
    }


}
