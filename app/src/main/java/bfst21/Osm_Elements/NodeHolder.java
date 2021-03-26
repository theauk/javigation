package bfst21.Osm_Elements;

import java.util.ArrayList;
import java.util.List;

public abstract class NodeHolder extends Element {

    protected List<Node> nodes = new ArrayList<>();

    public NodeHolder(long id) {
        super(id);
    }

    public void addNode(Node node) {
        nodes.add(node);
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
