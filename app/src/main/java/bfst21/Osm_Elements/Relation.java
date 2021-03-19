package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

public class Relation extends Element {
    List<Node> nodes = new ArrayList<>();
    List<Way> ways = new ArrayList<>();

    @Override
    public void draw(GraphicsContext gc) {

    }
}
