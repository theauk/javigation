package bfst21.Osm_Elements;


import javafx.scene.canvas.GraphicsContext;

public class Way extends NodeHolder {
    private String name;
    private int maxspeed;
    private boolean onewayRoad = false;
    private boolean isDriveable = true;
    private boolean isCycleable = true;
    private boolean isWalkable = true;
    private boolean isHighway = false;


    public Way(long id) {
        super(id);
        isHighway = false;
    }

    public Way() {
        super();
        isHighway = false;
    }

    public static Way merge(Way first, Way second) {
        if (first == null) return second;
        if (second == null) return first;
        Way merged = new Way();
        merged.nodes.addAll(first.nodes);
        merged.nodes.addAll(second.nodes.subList(1, second.nodes.size()));
        return merged;
    }

    public static Way merge(Way before, Way coast, Way after) {
        return merge(merge(before, coast), after);
    }

    public void setType(String type, boolean isHighway) {
        super.setType(type);
        if (isHighway) {
            setBooleans();
            for (Node n : nodes) {
                n.addReferenceToHighWay(this);
            }
        }
    }

    private void setBooleans() {
        isHighway = true;
        super.setLayer(3);
        if (type.contains("motorway") || type.contains("trunk")) {
            setNotCycleable();
            setNotWalkable();
        }
        if (type.equals("pedestrian") || type.equals("footway") || type.equals("steps")) {
            setNotDriveable();
        }
        if (type.equals("cycleway")) {
            setNotDriveable();
            setNotWalkable();
        }
    }

    public boolean hasName() {
        return name != null;
    }

    public void setNotCycleable() {
        this.isCycleable = false;
    }

    public void setNotWalkable() {
        this.isWalkable = false;
    }

    public void setNotDriveable() {
        isDriveable = false;
    }

    public void setOnewayRoad() {
        onewayRoad = true;
    }

    public int getMaxspeed() {
        if (isDriveable && maxspeed == 0) {
            return 50;
        } else {
            return maxspeed;
        }
    }

    public void setMaxspeed(int maxspeed) {
        this.maxspeed = maxspeed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHighWay() {
        return isHighway;
    }

    @Override
    public void draw(GraphicsContext gc) {
        //TODO Should check for one way.....
        gc.beginPath();
        gc.moveTo(nodes.get(0).getxMin(), nodes.get(0).getyMin());
        for (var node : nodes) {
            gc.lineTo(node.getxMin(), node.getyMin());
        }
        gc.stroke();
    }

    public boolean isWalkable() {
        return isWalkable;
    }

    public boolean isCycleable() {
        return isCycleable;
    }

    public boolean isOnewayRoad() {
        return onewayRoad;
    }

    public Node first() {
        return nodes.get(0);
    }

    public Node last() {
        return nodes.get(nodes.size() - 1);
    }
}