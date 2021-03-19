package bfst21.Osm_Elements;

import javafx.scene.canvas.GraphicsContext;

public abstract class Element {
    private Long id;

    public Element(long id)
    {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public abstract void draw(GraphicsContext gc);
}
