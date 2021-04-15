package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

public abstract class NodeHolder extends Element {

    protected List<Node> nodes = new ArrayList<>();

    public NodeHolder(long id) {
        super(id);
    }

    public NodeHolder(){

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
        //TODO Should check for one way.....
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
            }
        }
        return nextNode;
    }

    public Node getPreviousNode(Node currentNode) {
        Node previousNode = null;
        for (int i = 1; i < nodes.size(); i++) {
            if (nodes.get(i) == currentNode) {
                previousNode = nodes.get(i - 1);
            }
        }
        return previousNode;
    }

}
