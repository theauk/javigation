package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

public abstract class Element {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public abstract void draw(GraphicsContext gc);
}
