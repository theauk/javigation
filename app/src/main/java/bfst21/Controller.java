package bfst21;

import bfst21.view.CanvasBounds;
import bfst21.view.MapCanvas;
import bfst21.view.Theme;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.FileChooser;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Controller {
    private final int MAX_ZOOM_LEVEL = 100;
    private final int MIN_ZOOM_LEVEL = 0;
    private final int ZOOM_FACTOR = 32;
    private MapData mapData;
    private Loader loader;
    private Map<String, Theme> themes = new HashMap<>();
    private Point2D currentMouse;
    private Point2D lastMouse;
    private int zoomLevel;
    private boolean viaSlider = true;

    @FXML
    private MapCanvas mapCanvas;
    @FXML
    private Scene scene;
    @FXML
    private Label boundsLabel;
    @FXML
    private Label coordsLabel;
    @FXML
    private Label geoCoordsLabel;
    @FXML
    private Slider zoomSlider;
    @FXML
    private Menu themeMenu;
    @FXML
    private MenuItem zoomInItem;
    @FXML
    private MenuItem zoomOutItem;
    @FXML
    private RadioMenuItem defaultThemeItem;
    @FXML
    private ToggleGroup themeGroup;

    public void init(MapData mapData) {
        this.mapData = mapData;
        loader = new Loader(mapData);

        loadThemes();
        initView();

        openFile();
    }

    private void loadThemes() {
        for (String file : loader.getFilesIn("/themes", ".theme")) {
            Theme theme = loader.loadTheme(file);
            themes.put(theme.getName(), theme);

            if (!file.equals("default.theme")) {
                addTheme(new RadioMenuItem(theme.getName()));
            }
        }
    }

    private void addTheme(RadioMenuItem item) {
        item.setToggleGroup(themeGroup);
        themeMenu.getItems().add(item);
    }

    private void initView() {
        mapCanvas.init(mapData, themes.get("Default"));

        mapCanvas.widthProperty().addListener(((observable, oldValue, newValue) -> {
            setBoundsLabel();
        }));
        mapCanvas.heightProperty().addListener((observable, oldValue, newValue) -> {
            setBoundsLabel();
        });

        zoomSlider.setValue(zoomLevel);
        zoomSlider.setMax(MAX_ZOOM_LEVEL);
        zoomSlider.setMin(MIN_ZOOM_LEVEL);
        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (viaSlider) {
                if (newValue.intValue() > oldValue.intValue()) zoom(ZOOM_FACTOR);
                else if (newValue.intValue() < oldValue.intValue()) zoom(-ZOOM_FACTOR);
            }
        });

        themeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> setTheme(((RadioMenuItem) newValue.getToggleGroup().getSelectedToggle()).getText()));
    }

    /**
     * Called when a ScrollEvent is raised. Zooms in on the MapCanvas at the Mouse's coordinates.
     *
     * @param e the ScrollEvent associated with the caller.
     */
    @FXML
    private void onScroll(ScrollEvent e) {
        viaSlider = false;
        zoom(e.getDeltaY(), new Point2D(e.getX(), e.getY()));
    }

    /**
     * Zoom in or out on the MapCanvas via the MenuBar.
     *
     * @param e the ActionEvent associated with the caller.
     */
    @FXML
    private void zoom(ActionEvent e) {
        viaSlider = false;
        if (e.getSource().equals(zoomInItem)) zoom(ZOOM_FACTOR);
        else if (e.getSource().equals(zoomOutItem)) zoom(-ZOOM_FACTOR);
    }

    /**
     * Zooms in or out on the MapCanvas at the center point of the Canvas.
     *
     * @param amount the zoom amount.
     */
    private void zoom(double amount) {
        zoom(amount, new Point2D(mapCanvas.getWidth() / 2, mapCanvas.getHeight() / 2));
    }

    /**
     * Zooms in or out on the MapCanvas within a certain zoom level at a certain point.
     *
     * @param amount the zoom amount.
     * @param center the zoom center as a 2D point.
     */
    private void zoom(double amount, Point2D center) {
        if (zoomLevel >= MIN_ZOOM_LEVEL && zoomLevel <= MAX_ZOOM_LEVEL) {
            double factor = Math.pow(1.01, amount);

            if (amount > 0 && zoomLevel != MAX_ZOOM_LEVEL) {
                zoomLevel++;
                mapCanvas.zoom(factor, center);
            } else if (amount < 0 && zoomLevel != MIN_ZOOM_LEVEL) {
                zoomLevel--;
                mapCanvas.zoom(factor, center);
            }

            setBoundsLabel();
        }

        if (!viaSlider) zoomSlider.setValue(zoomLevel);
        viaSlider = true;
    }

    @FXML
    private void onMouseDragged(MouseEvent e) {
        double dx = e.getX() - lastMouse.getX();
        double dy = e.getY() - lastMouse.getY();

        mapCanvas.setCursor(Cursor.CLOSED_HAND);
        if (e.isPrimaryButtonDown()) {
            setBoundsLabel();
            mapCanvas.pan(dx, dy);
        }

        currentMouse = new Point2D(e.getX(), e.getY());
        onMousePressed(e);
    }

    @FXML
    private void onMouseMoved(MouseEvent e) {
        mapCanvas.setCursor(Cursor.DEFAULT);
        setCoordsLabel(new Point2D(e.getX(), e.getY()));
        currentMouse = new Point2D(e.getX(), e.getY());
    }

    @FXML
    private void onMousePressed(MouseEvent e) {
        lastMouse = new Point2D(e.getX(), e.getY());
        currentMouse = new Point2D(e.getX(), e.getY());
        setCoordsLabel(new Point2D(e.getX(), e.getY()));
    }

    @FXML
    private void onMouseReleased() {
        mapCanvas.setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void resetView() {
        mapCanvas.reset();
        themeGroup.selectToggle(defaultThemeItem);
        zoomLevel = MIN_ZOOM_LEVEL;
        zoomSlider.setValue(MIN_ZOOM_LEVEL);
        setCoordsLabel(currentMouse);
    }

    @FXML
    private void openFile() {
        File file = showFileChooser().showOpenDialog(scene.getWindow());

        if (file != null) loadFile(file.getAbsolutePath());
        else loadFile(getClass().getResource("/small.osm").getPath()); //USE BINARY FILE
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    private void loadFile(String path) {
        try {
            loader.load(path);
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void setTheme(String themeName) {
        mapCanvas.setTheme(themes.get(themeName));
    }

    private void setCoordsLabel(Point2D point) {
        try {
            Point2D coords = mapCanvas.getTransCoords(point.getX(), point.getY());
            Point2D geoCoords = mapCanvas.getGeoCoords(point.getX(), point.getY());

            double x = round(coords.getX(), 1);
            double y = round(coords.getY(), 1);
            coordsLabel.setText("CanvasCoords: " + x + ", " + y);

            // TODO: 26-03-2021 Visning af roadnavne skal være mere hensigtsmæssigt
            //x = round(geoCoords.getX(), 7);
            //y = round(geoCoords.getY(), 7);
            geoCoordsLabel.setText(mapData.getNearestRoad((float)coords.getX(), (float) coords.getY()));
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    private void setBoundsLabel() {
        CanvasBounds bounds = mapCanvas.getBounds();
        boundsLabel.setText("Bounds: (" + round(bounds.getMinX(), 1) + ", " + round(bounds.getMinY(), 1) + ") | (" + round(bounds.getMaxX(), 1) + ", " + round(bounds.getMaxY(), 1) + ")");
    }

    private double round(double number, int digits) {
        double scale = Math.pow(10, digits);
        return Math.round(number * scale) / scale;
    }

    private FileChooser showFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Open Street Map File", "*.osm", "*.zip"));

        return fileChooser;
    }
}
