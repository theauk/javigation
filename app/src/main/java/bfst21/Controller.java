package bfst21;

import bfst21.view.MapCanvas;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.FileChooser;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

public class Controller
{
    private Map map;
    private Loader osmLoader;

    private Point2D currentMouse;
    private Point2D lastMouse;

    private int zoomLevel;
    private final int MAX_ZOOM_LEVEL = 100;
    private final int MIN_ZOOM_LEVEL = 0;
    private final int ZOOM_FACTOR = 32;
    private boolean viaSlider = true;

    @FXML private MapCanvas mapCanvas;
    @FXML private Scene scene;
    @FXML private Label coordsLabel;
    @FXML private Label geoCoordsLabel;
    @FXML private Slider zoomSlider;
    @FXML private MenuItem zoomInItem;
    @FXML private MenuItem zoomOutItem;

    public void init(Map map)
    {
        this.map = map;
        mapCanvas.init(map);
        osmLoader = new Loader(map);

        initView();

        openFile();
    }

    private void initView()
    {
        zoomSlider.setValue(zoomLevel);
        zoomSlider.setMax(MAX_ZOOM_LEVEL);
        zoomSlider.setMin(MIN_ZOOM_LEVEL);
        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(viaSlider)
            {
                if(newValue.intValue() > oldValue.intValue()) zoom(ZOOM_FACTOR);
                else if(newValue.intValue() < oldValue.intValue()) zoom(-ZOOM_FACTOR);
            }
        });
    }

    /**
     * Called when a ScrollEvent is raised. Zooms in on the MapCanvas at the Mouse's coordinates.
     * @param e The ScrollEvent associated with the caller.
     */
    @FXML
    private void onScroll(ScrollEvent e)
    {
        viaSlider = false;
        zoom(e.getDeltaY(), new Point2D(e.getX(), e.getY()));
    }

    /**
     * Zoom in or out on the MapCanvas via the MenuBar.
     * @param e The ActionEvent associated with the caller.
     */
    @FXML
    private void zoom(ActionEvent e)
    {
        viaSlider = false;
        if(e.getSource().equals(zoomInItem)) zoom(ZOOM_FACTOR);
        else if(e.getSource().equals(zoomOutItem)) zoom(-ZOOM_FACTOR);
    }

    /**
     * Zooms in or out on the MapCanvas at the center point of the Canvas.
     * @param amount The zoom amount.
     */
    private void zoom(double amount)
    {
        zoom(amount, new Point2D(mapCanvas.getWidth() / 2, mapCanvas.getHeight() / 2));
    }

    /**
     * Zooms in or out on the MapCanvas within a certain zoom level at a certain point.
     * @param amount The zoom amount.
     * @param center The zoom center as a 2D point.
     */
    private void zoom(double amount, Point2D center)
    {
        if(zoomLevel >= MIN_ZOOM_LEVEL && zoomLevel <= MAX_ZOOM_LEVEL)
        {
            double factor = Math.pow(1.01, amount);

            if(amount > 0 && zoomLevel != MAX_ZOOM_LEVEL)
            {
                zoomLevel++;
                mapCanvas.zoom(factor, center);
            }
            else if(amount < 0 && zoomLevel != MIN_ZOOM_LEVEL)
            {
                zoomLevel--;
                mapCanvas.zoom(factor, center);
            }
        }

        if(!viaSlider) zoomSlider.setValue(zoomLevel);
        viaSlider = true;
    }

    @FXML
    private void onMouseDragged(MouseEvent e)
    {
        double dx = e.getX() - lastMouse.getX();
        double dy = e.getY() - lastMouse.getY();

        mapCanvas.setCursor(Cursor.CLOSED_HAND);
        if(e.isPrimaryButtonDown()) mapCanvas.pan(dx, dy);

        currentMouse = new Point2D(e.getX(), e.getY());
        onMousePressed(e);
    }

    @FXML
    private void onMouseMoved(MouseEvent e)
    {
        mapCanvas.setCursor(Cursor.DEFAULT);
        setCoordsLabel(new Point2D(e.getX(), e.getY()));
        currentMouse = new Point2D(e.getX(), e.getY());
    }

    @FXML
    private void onMousePressed(MouseEvent e)
    {
        lastMouse = new Point2D(e.getX(), e.getY());
        currentMouse = new Point2D(e.getX(), e.getY());
        setCoordsLabel(new Point2D(e.getX(), e.getY()));
    }

    @FXML
    private void onMouseReleased()
    {
        mapCanvas.setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void resetView()
    {
        mapCanvas.reset();
        zoomLevel = MIN_ZOOM_LEVEL;
        zoomSlider.setValue(MIN_ZOOM_LEVEL);
        setCoordsLabel(currentMouse);
    }

    @FXML
    private void openFile()
    {
        File file = showFileChooser().showOpenDialog(scene.getWindow());

        if(file != null) loadFile(file.getAbsolutePath());
        else loadFile(getClass().getResource("").getPath()); //USE BINARY FILE
    }

    @FXML
    private void exit()
    {
        System.exit(0);
    }

    private void loadFile(String path)
    {
        try {
            osmLoader.load(path);
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void setCoordsLabel(Point2D point)
    {
        Point2D coords = null;
        Point2D geoCoords = null;
        try {
            coords = mapCanvas.getTransCoords(point.getX(), point.getY());
            geoCoords = mapCanvas.getGeoCoords(point.getX(), point.getY());
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }

        double x = Math.round(coords.getX() * 10) / 10.0;
        double y = Math.round(coords.getY() * 10) / 10.0;
        coordsLabel.setText("CanvasCoords: " + x + ", " + y);

        x = Math.round(geoCoords.getX() * 10000000) / 10000000.0;
        y = Math.round(geoCoords.getY() * 10000000) / 10000000.0;
        geoCoordsLabel.setText("GeoCoords: " + x + ", " + y);
    }

    private FileChooser showFileChooser()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Open Street Map File", "*.osm", "*.zip"));

        return fileChooser;
    }
}
