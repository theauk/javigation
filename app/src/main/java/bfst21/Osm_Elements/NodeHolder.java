package bfst21.Osm_Elements;

import java.util.ArrayList;
import java.util.List;

public abstract class NodeHolder extends Element {

    protected List<Node> nodes = new ArrayList<>();

    public NodeHolder(long id) {
        super(id);
    }

    // TODO: 28-03-2021 Due to relations being bigger than input, some nodes are null. 
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

    private void checkMaxAndMin(Node node) {
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

    private void checkX(float xValue) {
        if (xValue > xMax) {
            xMax = xValue;
        } else if (xValue < xMin) {
            xMin = xValue;
        }
    }

    private void checkY(float yValue) {
        if (yValue > yMax) {
            yMax = yValue;
        } else if (yValue < yMin) {
            yMin = yValue;
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

}
