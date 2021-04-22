package bfst21;

import bfst21.Exceptions.NoNavigationResultException;
import bfst21.Exceptions.NoOSMInZipFileException;
import bfst21.Exceptions.UnsupportedFileFormatException;
import bfst21.Osm_Elements.Node;
import bfst21.file_io.Loader;
import bfst21.file_io.Serializer;
import bfst21.view.CanvasBounds;
import bfst21.view.CustomKeyCombination;
import bfst21.view.MapCanvas;
import bfst21.view.Theme;
import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Controller {
    private MapData mapData;
    private Loader loader;
    private Creator creator;

    private static final String BINARY_FILE = "/small.bmapdata";

    private Point2D lastMouse = new Point2D(0, 0);
    private final CustomKeyCombination upLeftCombination = new CustomKeyCombination(KeyCode.UP, KeyCode.LEFT);
    private final CustomKeyCombination upRightCombination = new CustomKeyCombination(KeyCode.UP, KeyCode.RIGHT);
    private final CustomKeyCombination downLeftCombination = new CustomKeyCombination(KeyCode.DOWN, KeyCode.LEFT);
    private final CustomKeyCombination downRightCombination = new CustomKeyCombination(KeyCode.DOWN, KeyCode.RIGHT);
    private boolean viaZoomSlider = true;
    private boolean dragged;

    private State state = State.MENU;

    private Node currentFromNode;
    private Node currentToNode;

    @FXML private MapCanvas mapCanvas;
    @FXML private Scene scene;
    @FXML private StackPane centerPane;
    @FXML private VBox loaderPane;

    @FXML private Label coordsLabel;
    @FXML private Label geoCoordsLabel;
    @FXML private Label nearestRoadLabel;
    @FXML private Label statusLabel;
    @FXML private Label scaleLabel;
    @FXML private Label boundsTR;
    @FXML private Label boundsBR;
    @FXML private Label boundsTL;
    @FXML private Label boundsBL;

    @FXML private ProgressIndicator loadingBar;
    @FXML private Slider zoomSlider;

    @FXML private ToggleGroup themeGroup;

    @FXML private Menu themeMenu;
    @FXML private MenuItem openItem;
    @FXML private MenuItem resetItem;
    @FXML private MenuItem cancelItem;
    @FXML private MenuItem zoomInItem;
    @FXML private MenuItem zoomOutItem;
    @FXML private MenuItem dumpItem;
    @FXML private RadioMenuItem rTreeDebug;

    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button chooseCorButtonFromNav;
    @FXML private Button chooseCorButtonToNav;
    @FXML private Button searchNav;

    @FXML private TextField textFieldFromNav;
    @FXML private TextField textFieldToNav;

    @FXML private RadioButton radioButtonCarNav;
    @FXML private RadioButton radioButtonBikeNav;
    @FXML private RadioButton radioButtonWalkNav;
    @FXML private RadioButton radioButtonFastestNav;
    @FXML private RadioButton radioButtonShortestNav;
    @FXML private Label distanceAndTimeNav;

    @FXML private ComboBox<String> dropDownPoints;
    @FXML private TextField textFieldPointName;
    @FXML private Button addPointButton;

    public void init() {
        mapData = new MapData();
        loader = new Loader();
        loadThemes();
        initView();
        openFile();
    }

    private void initView() {
        themeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> setTheme(((RadioMenuItem) newValue.getToggleGroup().getSelectedToggle()).getUserData().toString()));
        mapCanvas.initTheme(loader.loadTheme(themeGroup.getSelectedToggle().getUserData().toString()));
        scaleLabel.textProperty().bind(mapCanvas.getRatio());
        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (viaZoomSlider) zoom(newValue.intValue() - oldValue.intValue());
        });
        disableMenus();
        CustomKeyCombination.setTarget(mapCanvas);
    }

    private void initMapCanvas() {
        mapCanvas.init(mapData);
        //TODO MOVE LISTENERS
        mapCanvas.widthProperty().addListener((observable, oldValue, newValue) -> setBoundsLabels());
        mapCanvas.heightProperty().addListener((observable, oldValue, newValue) -> setBoundsLabels());
    }

    private void loadThemes() {
        for (String file : loader.getFilesIn("/themes", ".mtheme")) {
            String themeName = Theme.parseName(file);

            if (!file.equals("default.mtheme")) {
                RadioMenuItem item = new RadioMenuItem(themeName);
                item.setUserData(file);
                item.setToggleGroup(themeGroup);
                themeMenu.getItems().add(item);
            }
        }
    }

    /**
     * Called when a ScrollEvent is fired. Zooms in on the MapCanvas at the Mouse's coordinates.
     *
     * @param e the ScrollEvent associated with the caller.
     */
    @FXML
    private void onScroll(ScrollEvent e) {
        zoom((e.getDeltaY() > 0), new Point2D(e.getX(), e.getY()));
    }

    /**
     * Zoom in or out on the MapCanvas via a Node.
     *
     * @param e the ActionEvent associated with the caller.
     */
    @FXML
    private void zoom(ActionEvent e) {
        if (e.getSource().equals(zoomInItem) || e.getSource().equals(zoomInButton)) zoom(true, new Point2D(mapCanvas.getWidth() / 2, mapCanvas.getHeight() / 2));
        else if (e.getSource().equals(zoomOutItem) || e.getSource().equals(zoomOutButton)) zoom(false, new Point2D(mapCanvas.getWidth() / 2, mapCanvas.getHeight() / 2));
    }

    /**
     * Zoom in or out on the MapCanvas based on levels to be zoomed.
     *
     * @param levels the amount of zoom levels to be zoomed in or out.
     */
    private void zoom(int levels) {
        if (levels > 0) mapCanvas.zoom(true, levels);
        else if (levels < 0) mapCanvas.zoom(false, levels);
        setBoundsLabels();
    }

    /**
     * Zoom in on the mapCanvas at a specific pivot point.
     *
     * @param zoomIn to zoom in or not.
     * @param center the pivot point to zoom in at.
     */
    private void zoom(boolean zoomIn, Point2D center) {
        viaZoomSlider = false;
        mapCanvas.zoom(zoomIn, center);
        zoomSlider.setValue(mapCanvas.getZoomLevel());
        setBoundsLabels();
        viaZoomSlider = true;
    }

    @FXML
    private void onMouseDragged(MouseEvent e) {
        double dx = e.getX() - lastMouse.getX();
        double dy = e.getY() - lastMouse.getY();

        if (e.isPrimaryButtonDown()) {
            mapCanvas.setCursor(Cursor.CLOSED_HAND);
            setBoundsLabels();
            mapCanvas.pan(dx, dy);
            dragged = true;
        }

        onMousePressed(e);
    }

    @FXML
    private void onMouseMoved(MouseEvent e) {
        setLabels(new Point2D(e.getX(), e.getY()));
    }

    @FXML
    private void onMousePressed(MouseEvent e) {
        lastMouse = new Point2D(e.getX(), e.getY());
        mapCanvas.requestFocus();
        setLabels(lastMouse);
    }

    @FXML
    private void onMouseReleased() {
        mapCanvas.setCursor(Cursor.DEFAULT);
        if (dragged) {
            mapCanvas.updateMap();
            dragged = false;
        }
    }

    @FXML
    private void onKeyPressed(KeyEvent e) {
        int acceleration = 50;

        if(upLeftCombination.match(e)) mapCanvas.pan(acceleration, acceleration);
        else if(upRightCombination.match(e)) mapCanvas.pan(-acceleration, acceleration);
        else if(downLeftCombination.match(e)) mapCanvas.pan(acceleration, -acceleration);
        else if(downRightCombination.match(e)) mapCanvas.pan(-acceleration, -acceleration);
        else if(e.getCode().equals(KeyCode.UP)) mapCanvas.pan(0, acceleration);
        else if(e.getCode().equals(KeyCode.DOWN)) mapCanvas.pan(0, -acceleration);
        else if(e.getCode().equals(KeyCode.LEFT)) mapCanvas.pan(acceleration, 0);
        else if(e.getCode().equals(KeyCode.RIGHT)) mapCanvas.pan(-acceleration, 0);
    }

    @FXML
    private void onKeyReleased() {
        if(CustomKeyCombination.keyCodes.size() == 0) mapCanvas.updateMap();
    }

    @FXML
    private void resetView() {
        mapCanvas.reset();
        viaZoomSlider = false;
        zoomSlider.setValue(mapCanvas.getZoomLevel());
        setLabels(lastMouse);
        viaZoomSlider = true;
    }

    @FXML
    private void dumpBinary() {
        String contentText = "The dumping process of MapData takes excessive amount of memory. If not enough memory is available an Out of Memory Error might be thrown, causing the program to crash. \n\nDo you want to continue?";
        Alert warning = createAlert(Alert.AlertType.WARNING, "Dump MapData", "MapData Dump", contentText, ButtonType.YES, ButtonType.NO);
        warning.showAndWait();

        if(warning.getResult() == ButtonType.YES) {
            File file = showFileChooser("save").showSaveDialog(scene.getWindow());
            if(file != null) {
                Serializer serializer = new Serializer(mapData, file);
                showLoaderPane(true);

                serializer.setOnRunning(e -> taskRunning(serializer));
                serializer.setOnSucceeded(e -> taskSuccess());
                serializer.setOnFailed(e -> taskFailed(serializer, true));
                serializer.setOnCancelled(e -> System.out.println("CANCELLED!"));

                Thread serializerThread = new Thread(serializer, "Serializer Thread");
                serializerThread.setDaemon(true);
                serializerThread.start();
            }
        }
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    @FXML
    private void cancelLoad() {
        creator.cancel();
    }

    @FXML
    private void openFile() {
        showLoaderPane(true);
        File file = showFileChooser("open").showOpenDialog(scene.getWindow());
        InputStream inputStream;
        long fileSize;
        boolean binary = false;

        try {
            if (file != null) {
                if(file.getName().endsWith(".bmapdata")) binary = true;
                inputStream = loader.load(file.getPath());
                fileSize = file.getName().endsWith(".zip") ? loader.getZipFileEntrySize(file.getPath()) : file.length();    //If it's a zip file get the size of the entry else use the default file size.
            } else {
                inputStream = loader.loadResource(BINARY_FILE);
                fileSize = loader.getResourceFileSize(BINARY_FILE);
                binary = true;
            }

            loadFile(inputStream, fileSize, binary);
        } catch (IOException e) {
            statusLabel.setText("Failed: File not found.");
        } catch (NoOSMInZipFileException | UnsupportedFileFormatException e) {
            statusLabel.setText("Failed: " + e.getMessage());
        }
    }

    private void loadFile(InputStream inputStream, long fileSize, boolean binary) {
        mapData = new MapData();
        creator = new Creator(inputStream, fileSize, binary);
        creator.setOnRunning(e -> taskRunning(creator));
        creator.setOnSucceeded(e -> loadSuccess());
        creator.setOnCancelled(e -> taskCancelled());
        creator.setOnFailed(e -> taskFailed(creator, false));

        Thread creatorThread = new Thread(creator, "Creator Thread");
        creatorThread.setDaemon(true);
        creatorThread.start();
    }

    private void taskRunning(Task<?> task) {
        state = State.LOADING;
        disableMenus();
        centerPane.setCursor(Cursor.WAIT);
        statusLabel.textProperty().bind(task.messageProperty());
        loadingBar.progressProperty().bind(task.progressProperty());
    }

    private void loadSuccess() {
        mapData = creator.getValue();
        taskSuccess();
        initMapCanvas();
        resetView();
    }

    private void taskSuccess() {
        state = State.MAP;
        showLoaderPane(false);
        cleanupTask();
    }

    private void taskFailed(Task<?> task, boolean showMap) {
        if(showMap) {
            state = State.MAP;
            showLoaderPane(false);
        }
        else state = State.MENU;
        task.exceptionProperty().getValue().printStackTrace();
        statusLabel.setText("Failed: " + task.exceptionProperty().getValue().getMessage());
        cleanupTask();
    }

    private void taskCancelled() {
        state = State.MENU;
        cleanupTask();
        statusLabel.setText("Cancelled.");
    }

    private void cleanupTask() {
        disableMenus();
        centerPane.setCursor(Cursor.DEFAULT);
        statusLabel.textProperty().unbind();
        loadingBar.progressProperty().unbind();
    }

    private void disableMenus() {
        if (state == State.MENU) {
            openItem.setDisable(false);
            zoomInItem.setDisable(true);
            zoomOutItem.setDisable(true);
            resetItem.setDisable(true);
            cancelItem.setDisable(true);
            dumpItem.setDisable(true);
        } else if (state == State.LOADING) {
            openItem.setDisable(true);
            zoomInItem.setDisable(true);
            zoomOutItem.setDisable(true);
            resetItem.setDisable(true);
            cancelItem.setDisable(false);
            dumpItem.setDisable(true);
        } else if (state == State.MAP) {
            openItem.setDisable(false);
            zoomInItem.setDisable(false);
            zoomOutItem.setDisable(false);
            resetItem.setDisable(false);
            cancelItem.setDisable(true);
            dumpItem.setDisable(false);
        }
    }

    private void showLoaderPane(boolean show) {
        FadeTransition ft = new FadeTransition(Duration.millis(500), loaderPane);
        if (show) {
            if (loaderPane.isVisible()) return;
            statusLabel.setText("Waiting");
            loadingBar.setProgress(0.0);
            loaderPane.setVisible(true);
            state = State.MENU;
            disableMenus();
            ft.setFromValue(0);
            ft.setToValue(1);
        } else {
            ft.setDelay(Duration.millis(500));
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(e -> loaderPane.setVisible(false));
        }

        ft.play();
    }

    private void setTheme(String themeFile) {
        Theme theme = loader.loadTheme(themeFile);
        scene.getStylesheets().remove(mapCanvas.getTheme().getStylesheet());
        if (theme.getStylesheet() != null) {
            scene.getStylesheets().add(theme.getStylesheet());
        }
        mapCanvas.changeTheme(theme);
    }

    private void setLabels(Point2D point) {
        Point2D coords = mapCanvas.getTransCoords(point.getX(), point.getY());
        Point2D geoCoords = mapCanvas.getGeoCoords(point.getX(), point.getY());
        setCoordsLabel((float) coords.getX(), (float) coords.getY());
        setGeoCoordsLabel((float) geoCoords.getX(), (float) geoCoords.getY());
        setNearestRoadLabel(geoCoords.getX(), geoCoords.getY());
    }

    private void setCoordsLabel(float x, float y) {
        coordsLabel.setText("Coordinates: (" + round(x, 1) + ", " + round(y, 1) + ")");
    }

    private void setGeoCoordsLabel(float x, float y) {
        geoCoordsLabel.setText("Geo-coordinates: (" + round(x, 7) + ", " + round(y, 7) + ")");
    }

    private void setNearestRoadLabel(double x, double y) {
        nearestRoadLabel.setText("Nearest Road: " + mapData.getNearestRoad((float) x, (float) -y / 0.56f));
    }

    private void setBoundsLabels() {
        CanvasBounds bounds = mapCanvas.getBounds();
        boundsTL.setText("(" + round(bounds.getMinX(), 1) + ", " + round(bounds.getMinY(), 1) + ")");
        boundsTR.setText("(" + round(bounds.getMaxX(), 1) + ", " + round(bounds.getMinY(), 1) + ")");
        boundsBL.setText("(" + round(bounds.getMinX(), 1) + ", " + round(bounds.getMaxY(), 1) + ")");
        boundsBR.setText("(" + round(bounds.getMaxX(), 1) + ", " + round(bounds.getMaxY(), 1) + ")");
    }

    private double round(double number, int digits) {
        double scale = Math.pow(10, digits);
        return Math.round(number * scale) / scale;
    }

    private FileChooser showFileChooser(String option) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));

        if(option.equals("open")) {
            fileChooser.setTitle("Open File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All OSM Files", "*.osm", "*zip", "*.bmapdata"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OSM File", "*.osm"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Zipped OSM File", "*zip"));
        } else if(option.equals("save")) {
            fileChooser.setTitle("Save File");
            fileChooser.setInitialFileName("binary_map_data");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Binary MapData", "*.bmapdata"));
        }

        return fileChooser;
    }

    @FXML
    private void setRTreeDebug() {
        mapData.setRTreeDebug(rTreeDebug.isSelected());
        mapCanvas.rTreeDebugMode();
    }

    @FXML
    public void getPointNavFrom(ActionEvent actionEvent) { // TODO: 4/12/21 better way to do this to avoid two methods?
        getPointNav(true);
    }

    @FXML
    public void getPointNavTo(ActionEvent actionEvent) {
        getPointNav(false);
    }

    public void getPointNav(boolean fromSelected) {
        EventHandler<MouseEvent> event = new EventHandler<>() {
            @Override
            public void handle(MouseEvent e) {
                Point2D cursorPoint = new Point2D(e.getX(), e.getY());
                Point2D geoCoords = mapCanvas.getGeoCoords(cursorPoint.getX(), cursorPoint.getY());
                Node nearestRoadNode = mapData.getNearestRoadNode((float) geoCoords.getX(), (float) -geoCoords.getY() / 0.56f);

                String names = mapData.getNodeHighWayNames(nearestRoadNode);
                if (fromSelected) {
                    textFieldFromNav.setText(names);
                    currentFromNode = nearestRoadNode;
                } else {
                    textFieldToNav.setText(names);
                    currentToNode = nearestRoadNode;
                }
                mapCanvas.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
            }
        };
        mapCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event);
    }

    @FXML
    public void searchNav() {
        if (currentToNode == null || currentFromNode == null) {
            showDialogBox("Navigation Error", "Please enter both from and to");
        } else if (!radioButtonCarNav.isSelected() && !radioButtonBikeNav.isSelected() && !radioButtonWalkNav.isSelected()) {
            showDialogBox("Navigation Error", "Please select a vehicle type");
        } else if (!radioButtonFastestNav.isSelected() && !radioButtonShortestNav.isSelected()) {
            showDialogBox("Navigation Error", "Please select either fastest or shortest");
        } else {
            getDijkstraPath();
        }
    }

    private void showDialogBox(String title, String contentText) {
        createAlert(Alert.AlertType.INFORMATION, title, null, contentText).showAndWait();
    }

    private Alert createAlert(Alert.AlertType alertType, String title, String header, String text, ButtonType... buttons) {
        Alert alert = new Alert(alertType, text, buttons);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        return alert;
    }

    @FXML
    public void getDijkstraPath() {
        try {
            mapData.setDijkstraRoute(currentFromNode, currentToNode, radioButtonCarNav.isSelected(), radioButtonBikeNav.isSelected(), radioButtonWalkNav.isSelected(), radioButtonFastestNav.isSelected());
            setDistanceAndTimeNav(mapData.getDistanceNav(), mapData.getTimeNav());
            mapCanvas.repaint();
        } catch (NoNavigationResultException e) {
            e.printStackTrace();
        }
    }

    public void setDistanceAndTimeNav(double distance, double time) {
        distanceAndTimeNav.setVisible(true);
        String s = "Total distance: ";

        if (distance < 1000) {
            s += round(distance, 0) + " m";
        } else {
            s += round(distance / 1000f, 2) + " km";
        }

        if (time < 60) {
            s += " , Total time: " + round(time, 0) + " s";
        } else {
            s += " , Total time: " + round(time / 60f, 2) + " min";
        }
        distanceAndTimeNav.setText(s);
    }

    public void hideDistanceAndTimeNav() {
        distanceAndTimeNav.setVisible(false);
    }

    @FXML
    public void moveToPoint(ActionEvent actionEvent) {
        int i = dropDownPoints.getSelectionModel().getSelectedIndex();
        Node node = mapData.getUserAddedPoints().get(i);
        mapCanvas.centerOnPoint(node.getxMin(), node.getyMin());

    }

    @FXML
    public void addUserPoint(ActionEvent actionEvent) {
        if (textFieldPointName.getText().equals("")) showDialogBox("User added point error", "Please input name for your point");
        else {
            EventHandler<MouseEvent> event = new EventHandler<>() {
                @Override
                public void handle(MouseEvent e) {
                    //// TODO: 20-04-2021 make this work

                    Point2D cursorPoint = new Point2D(e.getX(), e.getY());
                    Point2D geoCoords = mapCanvas.getGeoCoords(cursorPoint.getX(), cursorPoint.getY());
                    Node node = new Node((float) geoCoords.getX(), (float) -geoCoords.getY() / 0.56f);
                    String nodeName = textFieldPointName.getText();
                    mapData.addToUserPointList(node);
                    dropDownPoints.getItems().add(nodeName);
                    mapCanvas.repaint();
                    textFieldPointName.setText("");
                    mapCanvas.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
                }
            };
            mapCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event);



        }
    }

    private enum State {
        MENU,
        LOADING,
        MAP
    }
}
