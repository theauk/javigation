package bfst21.view;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class Theme
{
    private String name;
    private final Map<String, Color> palette;

    public Theme()
    {
        palette = new HashMap<>();
    }

    public void put(String key, int red, int green, int blue) throws InvalidRGBValueException
    {
        if(red < 0 || red > 255) throw new InvalidRGBValueException(key, red, green, blue);
        if(green < 0 || green > 255) throw new InvalidRGBValueException(key, red, green, blue);
        if(blue < 0 || blue > 255) throw new InvalidRGBValueException(key, red, green, blue);
        palette.put(key, Color.rgb(red, green, blue));
    }

    public boolean isEmpty()
    {
        return palette.isEmpty();
    }

    public Color get(String key)
    {
        if(palette.get(key) == null)
        {
            System.err.println("Warning: No matching color to key: '" + key + "'. Returning BLACK!");
            return Color.BLACK;
        }
        return palette.get(key);
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
