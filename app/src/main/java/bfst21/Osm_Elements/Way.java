package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serial;
import java.io.Serializable;

public class Way extends NodeHolder implements Serializable {
    @Serial private static final long serialVersionUID = 8806015478559051293L;

    private String name;
    private int maxSpeed;
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

    @Override
    public void draw(GraphicsContext gc) {
        //TODO Should check for one way.....
       super.draw(gc);
    }

}