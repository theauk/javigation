package bfst21.view;

import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Theme
{
    private final Map<String, ThemeElement> palette;
    private final ThemeElement defaultThemeElement;
    private String stylesheet;
    private boolean warned;

    private final String regex = "^ *(?:\"(?<key>[a-z_]*)\" = \\{(?<iColor>iColor = \\[(?<red>\\d{1,3}), (?<green>\\d{1,3}), (?<blue>\\d{1,3})])(?:, (?<oColor>oColor = \\[(?<red2>\\d{1,3}), (?<green2>\\d{1,3}), (?<blue2>\\d{1,3})*]))*}(?:, \\{iWidth = (?<iWidth>\\d+)(?:, oWidth = (?<oWidth>\\d+))*})*(?:, \\{style = \"(?<style>[a-z]+)\"})*(?:, \\{filled = (?<fill>true|false)})*;(?: *#.*)*)* *$|^(?:#.*)$|^name = \"(?<name>[A-Za-z0-9 ]+)\"; *$";
    private final Pattern pattern = Pattern.compile(regex);

    public Theme()
    {
        palette = new HashMap<>();
        defaultThemeElement = new ThemeElement();
        defaultThemeElement.setMapColor(new MapColor(Color.rgb(0, 0, 0), Color.rgb(0, 0, 0)));
        defaultThemeElement.setStyle("normal");
    }

    private void put(String key, ThemeElement themeElement)
    {
        palette.put(key, themeElement);
    }

    public void parseData(String data, int line)
    {
        Matcher matcher = pattern.matcher(data);

        if(matcher.matches())
        {
            if(matchesGroup(matcher, "key"))
            {
                ThemeElement themeElement = new ThemeElement();

                if(matchesGroup(matcher, "iColor"))
                {
                    MapColor mapColor;
                    int r = Integer.parseInt(matcher.group("red"));
                    int g = Integer.parseInt(matcher.group("green"));
                    int b = Integer.parseInt(matcher.group("blue"));

                    if(matchesGroup(matcher, "oColor"))
                    {
                        int r2 = Integer.parseInt(matcher.group("red2"));
                        int g2 = Integer.parseInt(matcher.group("green2"));
                        int b2 = Integer.parseInt(matcher.group("blue2"));
                        mapColor = new MapColor(Color.rgb(r, g, b), Color.rgb(r2, g2, b2));
                    }
                    else mapColor = new MapColor(Color.rgb(r, g, b));

                    themeElement.setMapColor(mapColor);
                }
                if(matchesGroup(matcher, "iWidth"))
                {
                    themeElement.setInnerWidth(Integer.parseInt(matcher.group("iWidth")));
                    if(matchesGroup(matcher, "oWidth")) themeElement.setOuterWidth(Integer.parseInt(matcher.group("oWidth")));
                }
                if(matchesGroup(matcher, "style")) themeElement.setStyle(matcher.group("style"));
                if(matchesGroup(matcher, "fill")) themeElement.setFill(Boolean.parseBoolean(matcher.group("fill")));

                put(matcher.group("key"), themeElement);
            }
        }
        else System.err.println("Warning: Wrong syntax at: '" + data + "' (line: " + line + ")");
    }

    public static String parseName(String file)
    {
        String regex = "^ *name = \"(?<name>[A-Za-z0-9 ]+)\";(?: *#.*)* *$";
        Pattern pattern = Pattern.compile(regex);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Theme.class.getResourceAsStream("/themes/" + file)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if(line.isBlank()) continue;

                Matcher matcher = pattern.matcher(line);

                if(matcher.matches())
                {
                    line = matcher.group("name");
                    break;
                }
            }

            return line;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean matchesGroup(Matcher matcher, String group)
    {
        return matcher.group(group) != null;
    }

    public ThemeElement get(String key)
    {
        if(palette.get(key) == null)
        {
            if(!warned) System.err.println("Warning: Key '" + key + "' is loaded but not supported by the theme. Returning default.");
            warned = true;

            return defaultThemeElement;
        }
        return palette.get(key);
    }

    public void setStylesheet(String stylesheet)
    {
        this.stylesheet = stylesheet;
    }

    public String getStylesheet()
    {
        return stylesheet;
    }

    public class ThemeElement
    {
        private MapColor mapColor;
        private String style;
        private int innerWidth;
        private int outerWidth;
        private boolean fill;

        public ThemeElement()
        {
            style = "normal";
            innerWidth = 1;
            outerWidth = 1;
        }

        public void setMapColor(MapColor mapColor)
        {
            this.mapColor = mapColor;
        }

        public MapColor getColor()
        {
            return mapColor;
        }

        public boolean isTwoColored()
        {
            return mapColor.hasOuter();
        }

        public void setStyle(String style)
        {
            this.style = style;
        }

        public String getStyle()
        {
            return style;
        }

        public void setInnerWidth(int innerWidth)
        {
            this.innerWidth = innerWidth;
        }

        public int getInnerWidth()
        {
            return innerWidth;
        }

        public void setOuterWidth(int outerWidth)
        {
            this.outerWidth = outerWidth;
        }

        public int getOuterWidth()
        {
            return outerWidth;
        }

        public void setFill(boolean fill)
        {
            this.fill = fill;
        }

        public boolean fill()
        {
            return fill;
        }
    }

    public class MapColor
    {
        private final Color inner;
        private final Color outer;

        public MapColor(Color inner)
        {
            this.inner = inner;
            outer = null;
        }

        public MapColor(Color inner, Color outer)
        {
            this.inner = inner;
            this.outer = outer;
        }

        public boolean hasOuter()
        {
            return outer != null;
        }

        public Color getInner()
        {
            return inner;
        }

        public Color getOuter()
        {
            return outer;
        }
    }
}
