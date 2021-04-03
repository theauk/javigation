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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class Controller {
    private final int MAX_ZOOM_LEVEL = 100;
    private final int MIN_ZOOM_LEVEL = 0;
    private final int ZOOM_FACTOR = 32;

    private MapData mapData;
    private Loader loader;
    private Creator creator;

    private Map<String, String> themes = new HashMap<>();
    private Point2D currentMouse;
    private Point2D lastMouse;
    private int zoomLevel;
    private boolean viaSlider = true;

    @FXML private MapCanvas mapCanvas;
    @FXML private Scene scene;
    @FXML private StackPane centerPane;
    @FXML private VBox loaderPane;
    @FXML private ProgressIndicator loadingBar;
    @FXML private Label boundsLabel;
    @FXML private Label coordsLabel;
    @FXML private Label geoCoordsLabel;
    @FXML private Label statusLabel;
    @FXML private Slider zoomSlider;
    @FXML private Menu themeMenu;
    @FXML private MenuItem resetItem;
    @FXML private MenuItem zoomInItem;
    @FXML private MenuItem zoomOutItem;
    @FXML private RadioMenuItem defaultThemeItem;
    @FXML private ToggleGroup themeGroup;
    @FXML private RadioMenuItem rTreeDebug;

    public void init() {
        mapData = new MapData();
        loader = new Loader();
        loadThemes();
        openFile();
        initView();
    }

    private void initView() {
        mapCanvas.init(mapData, loader.loadTheme(themes.get("Default")));

        mapCanvas.widthProperty().addListener(((observable, oldValue, newValue) -> setBoundsLabel()));
        mapCanvas.heightProperty().addListener((observable, oldValue, newValue) -> setBoundsLabel());

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

    private void loadThemes() {
        for (String file : loader.getFilesIn("/themes", ".mtheme")) {
            String themeName = Theme.parseName(file);
            themes.put(themeName, file);

            if (!file.equals("default.mtheme")) {
                RadioMenuItem item = new RadioMenuItem(themeName);
                item.setToggleGroup(themeGroup);
                themeMenu.getItems().add(item);
            }
        }
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
                CanvasBounds cb = mapCanvas.getBounds();
                //System.out.println("Point 1: (" + cb.getMinX() + ", " + (-cb.getMinY() * 0.56f) + ")");
                //System.out.println("Point 2: (" + cb.getMaxX() + ", " + (-cb.getMinY() * 0.56f) + ")");
                printDistance(new Point2D((cb.getMinY() * -0.56f), cb.getMinX()), new Point2D((cb.getMinY() * -0.56f), cb.getMaxX()));
            } else if (amount < 0 && zoomLevel != MIN_ZOOM_LEVEL) {
                zoomLevel--;
                mapCanvas.zoom(factor, center);
                CanvasBounds cb = mapCanvas.getBounds();
                //System.out.println("Point 1: (" + cb.getMinX() + ", " + (-cb.getMinY() * 0.56f) + ")");
                //System.out.println("Point 2: (" + cb.getMaxX() + ", " + (-cb.getMinY() * 0.56f) + ")");
                printDistance(new Point2D((cb.getMinY() * -0.56f), cb.getMinX()), new Point2D((cb.getMinY() * -0.56f), cb.getMaxX()));
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

        if (e.isPrimaryButtonDown()) {
            mapCanvas.setCursor(Cursor.CLOSED_HAND);
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

    public void printDistance(Point2D start, Point2D end) {
        int earthRadius = 6371000; //in meters
        double lat1 = start.getX() * Math.PI / 180;
        double lat2 = end.getX() * Math.PI / 180;
        double lon1 = start.getY();
        double lon2 = end.getY();

        double deltaLat = (lat2 - lat1) * Math.PI / 180;
        double deltaLon = (lon2 - lon1) * Math.PI / 180;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        //System.out.println(round(distance, 1) + " m");
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

        if (file != null) loadFile(file.getAbsolutePath(), file.length());
        else {
            File binaryFile = new File(getClass().getResource("/small.osm").getPath());
            String path = binaryFile.getAbsolutePath();
            path = handlePotentialSpacesInPath(path);
            loadFile(path, binaryFile.length());
        }
    }

    private String handlePotentialSpacesInPath(String path) {
        try {
            return URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return path.replaceAll("%20", " ");
        }
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    @FXML
    private void cancelLoad() {
        if (creator != null) creator.cancel();
    }

    private void loadFile(String path, long fileSize) {
        try {
            //mapData = new MapData(); // TODO: 4/1/21 (1) Determine if it should be "wiped" each time.
            mapData.setupTrees(); // TODO: 4/1/21 (2) Or we want something like mapData.setupTrees() where the trees are initialized. Then we can reuse MapData and it should work in Canvas
            creator = new Creator(mapData, loader.load(path), fileSize);
            creator.setOnRunning(e -> {
                centerPane.setCursor(Cursor.WAIT);
                statusLabel.textProperty().bind(creator.messageProperty());
                loadingBar.progressProperty().bind(creator.progressProperty());
                disableGui(true);
                loaderPane.setVisible(true);
            });
            creator.setOnSucceeded(e -> {
                centerPane.setCursor(Cursor.DEFAULT);
                statusLabel.textProperty().unbind();
                loadingBar.progressProperty().unbind();
                disableGui(false);
                loaderPane.setVisible(false);
                //mapCanvas.loadFile(mapData); // TODO: 4/1/21 (1) delete if not (1)
                mapCanvas.reset(); // TODO: 4/1/21 (2) It should call reset since reset also calls other necessary methods including startup();
            });
            creator.setOnCancelled(e -> {
                centerPane.setCursor(Cursor.DEFAULT);
                statusLabel.textProperty().unbind();
                loadingBar.progressProperty().unbind();
                statusLabel.setText("Cancelled.");
                disableGui(false);
            });
            creator.setOnFailed(e -> {
                centerPane.setCursor(Cursor.DEFAULT);
                statusLabel.textProperty().unbind();
                loadingBar.progressProperty().unbind();
                statusLabel.setText("Failed.");
                creator.exceptionProperty().get().printStackTrace();
                disableGui(false);
            });

            Thread creatorThread = new Thread(creator, "Creator Thread");
            creatorThread.setDaemon(true);
            creatorThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTheme(String themeName) {
        String name = themes.get(themeName);
        Theme theme = loader.loadTheme(name);
        scene.getStylesheets().clear();
        if(theme.getStylesheet() != null) scene.getStylesheets().add(theme.getStylesheet());
        mapCanvas.setTheme(theme);
    }

    private void setCoordsLabel(Point2D point) {
        try {
            Point2D coords = mapCanvas.getTransCoords(point.getX(), point.getY());
            Point2D geoCoords = mapCanvas.getGeoCoords(point.getX(), point.getY());

            //double x = round(coords.getX(), 1);
            //double y = round(coords.getY(), 1);
            // coordsLabel.setText("CanvasCoords: " + x + ", " + y);

            // TODO: 26-03-2021 Visning af roadnavne skal være mere hensigtsmæssigt
            //x = round(geoCoords.getX(), 7);
            //y = round(geoCoords.getY(), 7);
            //geoCoordsLabel.setText(mapData.getNearestRoad((float)coords.getX(), (float) coords.getY()));
            //geoCoordsLabel.setText("(" + geoCoords.getX() + ", " + geoCoords.getY() + ")");
            double x = round(geoCoords.getX(), 5);
            double y = round(geoCoords.getY(), 5);
            y = -y / 0.56f;
            coordsLabel.setText("CanvasCoords: " + x + ", " + y);
            //geoCoordsLabel.setText("GeoCoords : " + x + ", "+ y);
            geoCoordsLabel.setText(mapData.getNearestRoad((float) x, (float) y));
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

    private void disableGui(boolean disable) {
        zoomSlider.setDisable(disable);
        zoomInItem.setDisable(disable);
        zoomOutItem.setDisable(disable);
        resetItem.setDisable(disable);
    }

    private FileChooser showFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Open Street Map File", "*.osm", "*.zip"));

        return fileChooser;
    }

    @FXML
    private void setRTreeDebug() {
        mapData.setRTreeDebug(rTreeDebug.isSelected()); // TODO: 3/31/21 which class should it go via?
        mapCanvas.rTreeDebugMode();
    }
}
