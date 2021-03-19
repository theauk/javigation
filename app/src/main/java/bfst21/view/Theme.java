package bfst21.view;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class Theme
{
    private String name;
    private Map<String, Color> palette;

    public Theme(String name)
    {
        this.name = name;
        palette = new HashMap<>();
    }

    public void put(String key, Color value)
    {
        palette.put(key, value);
    }

    public Color get(String key)
    {
        if(palette.get(key) == null) return Color.BLACK;
        return palette.get(key);
    }

    public String getName()
    {
        return name;
    }
}
