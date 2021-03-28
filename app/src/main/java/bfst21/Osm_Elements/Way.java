package bfst21.Osm_Elements;


import javafx.scene.canvas.GraphicsContext;

public class Way extends NodeHolder {

    public Way(long id) {
        super(id);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.beginPath();
        gc.moveTo(nodes.get(0).getxMin(), nodes.get(0).getyMin());
        for (var node : nodes) {
            gc.lineTo(node.getxMin(), node.getyMin());
        }
        gc.stroke();
    }
}