package bfst21.Osm_Elements;


import javafx.scene.canvas.GraphicsContext;

public class Way extends NodeHolder {
    private String name;
    private int maxSpeed;
    private boolean onewayRoad = false;
    private boolean onewayBikeRoad = false;
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

    public void setType(String type, boolean isHighway, boolean foot) {
        super.setType(type);
        if (isHighway) {
            setBooleans(foot);
        }
    }

    private void setBooleans(boolean foot) {
        isHighway = true;
        super.setLayer(3);
        if (type.contains("motorway") || type.contains("trunk")) {
            setNotCycleable();
            setNotWalkable();
        }
        if (type.equals("pedestrian") || type.equals("footway") || type.equals("steps") || type.equals("path")) {
            setNotDriveable();
        }
        if (type.equals("cycleway")) {
            setNotDriveable();
            if (!foot) {
                setNotWalkable();
            }
        }
    }

    public boolean hasName() {
        return name != null;
    }

    public void setAsHighWay() {
        isHighway = true;
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

    public void setOnewayBikeRoad() {
        onewayBikeRoad = true;
    }

    public int getMaxSpeed() {
        if (isDriveable && maxSpeed == 0) {
            return 50;
        } else {
            return maxSpeed;
        }
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
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

    public boolean isWalkable() {
        return isWalkable;
    }

    public boolean isCycleable() {
        return isCycleable;
    }

    public boolean isDriveable() {
        return isDriveable;
    }

    public boolean isOnewayRoad() {
        return onewayRoad;
    }

    public boolean isOneWayForBikes() {
        if (onewayBikeRoad) {
            return true;
        } else if (onewayRoad && type.equals("cycleway")) {
            return true;
        } else if (type.equals("roundabout")) {
            return true;
        } else {
            return onewayRoad;
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        //TODO Should check for one way.....
       super.draw(gc);
    }

}