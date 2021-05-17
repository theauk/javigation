package bfst21;

import bfst21.Osm_Elements.Node;
import bfst21.Osm_Elements.Way;
import bfst21.data_structures.RTree;
import bfst21.data_structures.RouteNavigation;
import bfst21.exceptions.NoOSMInZipFileException;
import bfst21.exceptions.UnsupportedFileFormatException;
import bfst21.file_io.Loader;
import bfst21.file_io.Serializer;
import bfst21.utils.*;
import bfst21.view.AutoFillTextField;
import bfst21.view.CanvasBounds;
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
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

public class Controller {
    private MapData mapData;
    private Creator creator;
    private RouteNavigation routeNavigation;
    private AddressFilter fromAddressFilter;
    private AddressFilter toAddressFilter;

    private static final String BINARY_FILE = "/bornholm.bmapdata";

    private Point2D lastMouse = new Point2D(0, 0);
    private Point2D currentRightClick = new Point2D(0,0);
    private final CustomKeyCombination upLeftCombination = new CustomKeyCombination(KeyCode.W, KeyCode.A);
    private final CustomKeyCombination upRightCombination = new CustomKeyCombination(KeyCode.W, KeyCode.D);
    private final CustomKeyCombination downLeftCombination = new CustomKeyCombination(KeyCode.S, KeyCode.A);
    private final CustomKeyCombination downRightCombination = new CustomKeyCombination(KeyCode.S, KeyCode.D);
    private boolean viaZoomSlider = true;
    private boolean dragged;

    private State state = State.MENU;

    private boolean useKDTreeNearestRoad;

    private Node currentFromNode;
    private Node currentToNode;
    private Way currentFromWay;
    private Way currentToWay;
    private int[] nearestFromWaySegmentIndices;
    private int[] nearestToWaySegmentIndices;
    private Node clickedNodeTo;
    private Node clickedNodeFrom;

    @FXML private MapCanvas mapCanvas;
    @FXML private Scene scene;
    @FXML private StackPane centerPane;
    @FXML private StackPane menuPane;
    @FXML private VBox loadingBarPane;
    @FXML private VBox logoPane;

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
    @FXML private ToggleGroup vehicleNavGroup;

    @FXML private Menu themeMenu;
    @FXML private MenuItem openItem;
    @FXML private MenuItem resetItem;
    @FXML private MenuItem cancelItem;
    @FXML private MenuItem zoomInItem;
    @FXML private MenuItem zoomOutItem;
    @FXML private MenuItem dumpItem;
    @FXML private RadioMenuItem rTreeDebug;
    @FXML private RadioMenuItem kdTreeNearestNode;
    @FXML public RadioMenuItem rTreeNearestNode;
    @FXML public RadioMenuItem showLeftView;

    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;

    @FXML private AutoFillTextField textFieldFromNav;
    @FXML private AutoFillTextField textFieldToNav;
    @FXML private AutoFillTextField addressSearchTextField;

    @FXML private RadioButton radioButtonFastestNav;
    @FXML private RadioButton radioButtonShortestNav;
    @FXML private Label distanceNav;
    @FXML private Label timeNav;
    @FXML private RadioMenuItem aStarNav;

    @FXML private Label specialPathFeaturesNav;

    @FXML private ListView<String> myPlacesListView;

    @FXML private ListView<ListCell<String>> directionsList;

    @FXML private ContextMenu rightClickMenu;

    @FXML private Button directionsButton;
    @FXML private Button searchForAddress;
    @FXML private Button switchButton;
    @FXML private Button backButton;
    @FXML private VBox address_myPlacesPane;
    @FXML private VBox navigationLeftPane;
    @FXML private ToggleButton bikeNavToggleButton;
    @FXML private ToggleButton walkNavToggleButton;
    @FXML private HBox fastestShortestGroup;
    @FXML private ToggleButton carNavToggleButton;

    public void init() {
        mapData = new MapData();
        routeNavigation = new RouteNavigation();
        fromAddressFilter = new AddressFilter();
        toAddressFilter = new AddressFilter();
        loadThemes();
        initView();
        startUp();
    }

    private void startUp() {
        FadeTransition fadeIn = createFadeTransition(1.5, logoPane, true);
        fadeIn.setOnFinished(e -> {
            loadingBarPane.setVisible(true);
            openFile();
        });
        fadeIn.play();
    }

    private void initView() {
        themeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> setTheme(((RadioMenuItem) newValue.getToggleGroup().getSelectedToggle()).getUserData().toString()));
        mapCanvas.initTheme(Loader.loadTheme(themeGroup.getSelectedToggle().getUserData().toString()));
        scaleLabel.textProperty().bind(mapCanvas.getRatio());
        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (viaZoomSlider) zoom(newValue.intValue() - oldValue.intValue());
        });
        disableMenus();
        addListeners();
    }

    private void initMapCanvas() {
        mapCanvas.init(mapData);
        mapCanvas.widthProperty().addListener((observable, oldValue, newValue) -> setBoundsLabels());
        mapCanvas.heightProperty().addListener((observable, oldValue, newValue) -> setBoundsLabels());

        determineNearestRoadMethod();

        textFieldToNav.clear();
        textFieldFromNav.clear();
        addressSearchTextField.clear();
        myPlacesListView.getItems().removeAll(myPlacesListView.getItems());
        directionsList.getItems().removeAll(directionsList.getItems());
        specialPathFeaturesNav.setText("");
        distanceNav.setText("");
        timeNav.setText("");
    }

    /**
     * Determines if the nearest road should be determined through the KD-tree or the R-tree. For maps with an area bigger
     * than 2.5 the KD-tree is used and the user cannot change the method to the R-tree. For maps smaller than 2.5 R-tree
     * is set as default but the user can also change to the KD-tree.
     */
    private void determineNearestRoadMethod() {
        double mapWidth = Math.abs(mapData.getMaxX() - mapData.getMinX());
        double mapHeight = Math.abs(mapData.getMaxY() - mapData.getMinY());
        double mapArea = mapWidth * mapHeight;
        if (mapArea < 2.5) {
            useKDTreeNearestRoad = false;
            rTreeNearestNode.setDisable(false);
            rTreeNearestNode.setSelected(true);
        } else {
            useKDTreeNearestRoad = true;
            rTreeNearestNode.setDisable(true);
            kdTreeNearestNode.setSelected(true);
        }
    }

    private void loadThemes() {
        for (String file : Loader.getFilesIn("/themes", ".mtheme")) {
            String themeName = Theme.parseName(file);

            if (!file.equals("default.mtheme")) {
                RadioMenuItem item = new RadioMenuItem(themeName);
                item.setUserData(file);
                item.setToggleGroup(themeGroup);
                themeMenu.getItems().add(item);
            }
        }
    }

    private void addListeners() {
        CustomKeyCombination.setTarget(mapCanvas);

        mapCanvas.setOnContextMenuRequested(e -> {
            rightClickMenu.show(mapCanvas, e.getScreenX(), e.getScreenY());
            currentRightClick = new Point2D(e.getX(), e.getY());
        });

        textFieldFromNav.textProperty().addListener(((observable, oldValue, newValue) -> {
            fromAddressFilter.search(newValue);
            textFieldFromNav.suggest(fromAddressFilter.getSuggestions());
        }));

        textFieldToNav.textProperty().addListener(((observable, oldValue, newValue) -> {
            toAddressFilter.search(newValue);
            textFieldToNav.suggest(toAddressFilter.getSuggestions());
        }));

        addressSearchTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
            toAddressFilter.search(newValue);
            addressSearchTextField.suggest(toAddressFilter.getSuggestions());
        }));

        addressSearchTextField.setOnAction(e -> addSearchResult());
        searchForAddress.setOnAction(e -> addSearchResult());

        directionsButton.setOnAction(e -> {
            mapData.resetCurrentSearchResult();
            textFieldToNav.setSuggest(false);
            textFieldToNav.setText(addressSearchTextField.getText());
            address_myPlacesPane.setVisible(false);
            navigationLeftPane.setVisible(true);
            textFieldToNav.setSuggest(true);
        });

        switchButton.setOnAction(e -> switchDirections());

        backButton.setOnAction(e -> {
            addressSearchTextField.clear();
            navigationLeftPane.setVisible(false);
            address_myPlacesPane.setVisible(true);
            textFieldFromNav.clear();
            textFieldToNav.clear();
            directionsList.getItems().clear();
            timeNav.setVisible(false);
            distanceNav.setVisible(false);
            mapData.resetCurrentRoute();
            mapData.resetCurrentSearchResult();
            resetNavigation();
            mapCanvas.repaint();
        });

        myPlacesListView.setOnMouseClicked(e -> {
            if(e.getClickCount() == 2 && myPlacesListView.getItems().size() > 0) moveToPoint(myPlacesListView.getSelectionModel().getSelectedIndex());
        });
        
        myPlacesListView.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ENTER && myPlacesListView.getItems().size() > 0) moveToPoint(myPlacesListView.getSelectionModel().getSelectedIndex());
        });
    }

    private void addSearchResult() {
        mapData.resetCurrentSearchResult();
        if(toAddressFilter.getMatchedAddress() != null){
            Node match = toAddressFilter.getMatchedAddress().getNode();
            mapData.addUserSearchResult(match);
            mapCanvas.centerOnPoint(match.getxMax(), match.getyMax());
        } else {
            createAlert(Alert.AlertType.WARNING, "No Address Found", "No Address Found!", "Please check if you've written the correct address.").showAndWait();
            mapCanvas.repaint();
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
        if (e.getSource().equals(zoomInItem) || e.getSource().equals(zoomInButton))
            zoom(true, new Point2D(mapCanvas.getWidth() / 2, mapCanvas.getHeight() / 2));
        else if (e.getSource().equals(zoomOutItem) || e.getSource().equals(zoomOutButton))
            zoom(false, new Point2D(mapCanvas.getWidth() / 2, mapCanvas.getHeight() / 2));
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
        rightClickMenu.hide();
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

        if (upLeftCombination.match(e)) mapCanvas.pan(acceleration, acceleration);
        else if (upRightCombination.match(e)) mapCanvas.pan(-acceleration, acceleration);
        else if (downLeftCombination.match(e)) mapCanvas.pan(acceleration, -acceleration);
        else if (downRightCombination.match(e)) mapCanvas.pan(-acceleration, -acceleration);
        else if (e.getCode().equals(KeyCode.W)) mapCanvas.pan(0, acceleration);
        else if (e.getCode().equals(KeyCode.S)) mapCanvas.pan(0, -acceleration);
        else if (e.getCode().equals(KeyCode.A)) mapCanvas.pan(acceleration, 0);
        else if (e.getCode().equals(KeyCode.D)) mapCanvas.pan(-acceleration, 0);
    }

    @FXML
    private void onKeyReleased() {
        if (CustomKeyCombination.keyCodes.size() == 0) mapCanvas.updateMap();
    }

    @FXML
    private void resetView() {
        mapCanvas.reset();
        viaZoomSlider = false;
        zoomSlider.setValue(mapCanvas.getZoomLevel());
        setLabels(lastMouse);
        viaZoomSlider = true;
    }

    /**
     * Method to create a Binary file from the application
     * A progress bar will be shown to the user and how much data the method has dumped
     */
    @FXML
    private void dumpBinary() {
        String contentText = "The dumping process of MapData takes excessive amount of memory. If not enough memory is available an Out of Memory Error might be thrown, causing the program to crash. \n\nDo you want to continue?";
        Alert warning = createAlert(Alert.AlertType.WARNING, "Dump MapData", "MapData Dump", contentText, ButtonType.YES, ButtonType.NO);
        warning.showAndWait();

        if (warning.getResult() == ButtonType.YES) {
            File file = showFileChooser("save").showSaveDialog(scene.getWindow());
            if (file != null) {
                Serializer serializer = new Serializer(mapData, file);
                showMenuPane(true);

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
        showMenuPane(true);
        File file = showFileChooser("open").showOpenDialog(scene.getWindow());
        InputStream inputStream;
        long fileSize;
        boolean binary = false;

        try {
            if (file != null) {
                if(file.getName().endsWith(".bmapdata")) binary = true;
                inputStream = Loader.load(file.getPath());
                fileSize = file.getName().endsWith(".zip") ? Loader.getZipFileEntrySize(file.getPath()) : file.length();    //If it's a zip file get the size of the entry else use the default file size.
            } else {
                inputStream = Loader.loadResource(BINARY_FILE);
                fileSize = Loader.getResourceFileSize(BINARY_FILE);
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
        routeNavigation.setNodeToHighwayMap(mapData.getNodeToHighWay());
        routeNavigation.setNodeToRestriction(mapData.getNodeToRestriction());
        routeNavigation.setWayToRestriction(mapData.getWayToRestriction());
        fromAddressFilter.setAddressTree(mapData.getAddressTree());
        toAddressFilter.setAddressTree(mapData.getAddressTree());
        taskSuccess();
        initMapCanvas();
        resetView();
    }

    private void taskSuccess() {
        state = State.MAP;
        showMenuPane(false);
        cleanupTask();
    }

    private void taskFailed(Task<?> task, boolean showMap) {
        if (showMap) {
            state = State.MAP;
            showMenuPane(false);
        }
        else state = State.MENU;
        cleanupTask();
        task.exceptionProperty().getValue().printStackTrace();
        statusLabel.setText("Failed: " + task.exceptionProperty().getValue().getMessage());
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
            address_myPlacesPane.setVisible(false);
            navigationLeftPane.setVisible(false);
            showLeftView.setDisable(true);
            openItem.setDisable(false);
            zoomInItem.setDisable(true);
            zoomOutItem.setDisable(true);
            resetItem.setDisable(true);
            cancelItem.setDisable(true);
            dumpItem.setDisable(true);
        } else if (state == State.LOADING) {
            address_myPlacesPane.setVisible(false);
            navigationLeftPane.setVisible(false);
            showLeftView.setDisable(true);
            openItem.setDisable(true);
            zoomInItem.setDisable(true);
            zoomOutItem.setDisable(true);
            resetItem.setDisable(true);
            cancelItem.setDisable(false);
            dumpItem.setDisable(true);
        } else if (state == State.MAP) {
            showLeftView.setDisable(false);
            openItem.setDisable(false);
            zoomInItem.setDisable(false);
            zoomOutItem.setDisable(false);
            resetItem.setDisable(false);
            cancelItem.setDisable(true);
            dumpItem.setDisable(false);
        }
    }

    private void showMenuPane(boolean show) {
        FadeTransition fader;

        if (show) {
            if (menuPane.isVisible()) return;
            statusLabel.setText("Waiting");
            loadingBar.setProgress(0.0);
            menuPane.setVisible(true);
            state = State.MENU;
            disableMenus();
            fader = createFadeTransition(0.5, menuPane, true);
        } else {
            fader = createFadeTransition(0.5, menuPane, false);
            fader.setDelay(Duration.millis(500));
            fader.setOnFinished(e -> {
                menuPane.setVisible(false);
                address_myPlacesPane.setVisible(true);
            });
        }

        fader.play();
    }

    private void setTheme(String themeFile) {
        Theme theme = Loader.loadTheme(themeFile);
        scene.getStylesheets().remove(mapCanvas.getTheme().getStylesheet());
        if (theme.getStylesheet() != null) {
            scene.getStylesheets().add(theme.getStylesheet());
        }
        mapCanvas.changeTheme(theme);
    }

    private void setLabels(Point2D point) {
        Point2D coords = mapCanvas.getTransCoords(point.getX(), point.getY());
        Point2D geoCoords = MapMath.convertToGeoCoords(mapCanvas.getTransCoords(point.getX(), point.getY()));
        setCoordsLabel((float) coords.getX(), (float) coords.getY());
        setGeoCoordsLabel((float) geoCoords.getX(), (float) geoCoords.getY());
        setNearestRoadLabel(coords.getX(), coords.getY());
    }

    private void setCoordsLabel(float x, float y) {
        coordsLabel.setText("Coordinates: (" + MapMath.round(x, 7) + ", " + MapMath.round(y, 7) + ")");
    }

    private void setGeoCoordsLabel(float x, float y) {
        geoCoordsLabel.setText("Geo-coordinates: (" + MapMath.round(x, 7) + ", " + MapMath.round(y, 7) + ")");
    }

    private void setNearestRoadLabel(double x, double y) {
        nearestRoadLabel.setText("Nearest Road: " + mapData.getNearestRoad((float) x, (float) y, useKDTreeNearestRoad));
    }

    private void setBoundsLabels() {
        CanvasBounds bounds = mapCanvas.getBounds();
        boundsTL.setText("(" + MapMath.round(bounds.getMinX(), 1) + ", " + MapMath.round(bounds.getMinY(), 1) + ")");
        boundsTR.setText("(" + MapMath.round(bounds.getMaxX(), 1) + ", " + MapMath.round(bounds.getMinY(), 1) + ")");
        boundsBL.setText("(" + MapMath.round(bounds.getMinX(), 1) + ", " + MapMath.round(bounds.getMaxY(), 1) + ")");
        boundsBR.setText("(" + MapMath.round(bounds.getMaxX(), 1) + ", " + MapMath.round(bounds.getMaxY(), 1) + ")");
    }

    private FileChooser showFileChooser(String option) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));

        if (option.equals("open")) {
            fileChooser.setTitle("Open File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All OSM Files", "*.osm", "*.zip", "*.bmapdata"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OSM File", "*.osm"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Zipped OSM File", "*zip"));
        } else if (option.equals("save")) {
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

    /**
     * Get the nearest road, node on road an road indices to given coordinate.
     * @param x coordinate
     * @param y coordinate
     * @param addressWay  if closest road needs to have addressWay name else null.
     * @param vehicleType If vehicle type is chosen, else null.
     * @return RTree.NearestRoadPriorityQueueEntry
     */
    private RTree.NearestRoadPriorityQueueEntry getNearestRoadEntry(float x, float y, String addressWay, VehicleType vehicleType) {
        return mapData.getNearestRoadRTreePQEntry(x, y, addressWay, vehicleType);
    }
    // Finds the cosmetic nearest road in order to write it in the textfield
    private void setClosestToWay(Node node) {
        textFieldToNav.setSuggest(false);
        Way nearestWay = getNearestRoadEntry(node.getxMax(), node.getyMax(), null, null).getWay();
        textFieldToNav.setText(nearestWay.getName());
        textFieldToNav.setSuggest(true);
    }
    // same as above - just for the FromNav field
    private void setClosestFromWay(Node node) {
        textFieldFromNav.setSuggest(false);
        Way nearestWay = getNearestRoadEntry(node.getxMax(), node.getyMax(), null, null).getWay();
        textFieldFromNav.setText(nearestWay.getName());
        textFieldFromNav.setSuggest(true);
    }

    /**
     * Checks if all the needed input is given in order to get route from navigation.
     * Checks closest nodes with toggle group input in order to find the closets road matching the chosen vehicle type.
     */
    @FXML
    public void searchNav() {
        if (vehicleNavGroup.getSelectedToggle() == null) {
            createAlert(Alert.AlertType.INFORMATION, "Navigation Error", "Navigation Error", "Please select a vehicle type").showAndWait();
        } else if (!radioButtonFastestNav.isSelected() && !radioButtonShortestNav.isSelected()) {
            createAlert(Alert.AlertType.INFORMATION, "Navigation Error", "Navigation Error", "Please select either fastest or shortest").showAndWait();
        } else {
                // TO
                if (toAddressFilter.getMatchedAddress() != null) {
                    Address address = toAddressFilter.getMatchedAddress();
                    setCurrentToNode(address.getNode().getxMax(), address.getNode().getyMax(), address.getStreet());
                } else if(clickedNodeTo != null){
                    setCurrentToNode(clickedNodeTo.getxMax(), clickedNodeTo.getyMax(), null);
                } else {
                    createAlert(Alert.AlertType.INFORMATION, "Navigation Error", "Navigation Error", "Please select where to go to").showAndWait();
                    return;
                }
                // FROM
                if (fromAddressFilter.getMatchedAddress() != null) {
                    Address address = fromAddressFilter.getMatchedAddress();
                    setCurrentFromNode(address.getNode().getxMax(), address.getNode().getyMax(), address.getStreet());
                } else if (clickedNodeFrom != null) {
                    setCurrentFromNode(clickedNodeFrom.getxMax(), clickedNodeFrom.getyMax(), null);
                } else {
                    createAlert(Alert.AlertType.INFORMATION, "Navigation Error", "Navigation Error", "Please select where to go from").showAndWait();
                    return;
                }

             if (currentFromNode.getxMax() == currentToNode.getxMax() && currentFromNode.getyMax() == currentToNode.getyMax()) {
                 createAlert(Alert.AlertType.INFORMATION, "Navigation Error", "Navigation Error", "From and to are the same address").showAndWait();
             } else {
                 getRoute();
             }
        }
    }

    /**
     * Sets the current from node for navigation.
     * @param x coordinate
     * @param y coordinate
     * @param street Null or address street name that node should be on.
     */
    private void setCurrentFromNode(float x, float y, String street) {
        VehicleType vehicleType = (VehicleType) vehicleNavGroup.getSelectedToggle().getUserData();
        RTree.NearestRoadPriorityQueueEntry entry = getNearestRoadEntry(x, y, street, vehicleType);
        Node nearestNodeOnNearestWay = MapMath.getClosestPointOnWayAsNode(x, y, entry.getSegment());
        currentFromWay = entry.getWay();
        nearestFromWaySegmentIndices = entry.getSegmentIndices();
        currentFromNode = nearestNodeOnNearestWay;
    }

    /**
     * Sets the current to node for navigation.
     * @param x coordinate
     * @param y coordinate
     * @param street Null or address street name that node should be on.
     */
    private void setCurrentToNode(float x, float y, String street) {
        VehicleType vehicleType = (VehicleType) vehicleNavGroup.getSelectedToggle().getUserData();
        RTree.NearestRoadPriorityQueueEntry entry = getNearestRoadEntry(x, y, street, vehicleType);
        Node nearestNodeOnNearestWay = MapMath.getClosestPointOnWayAsNode(x, y, entry.getSegment());
        currentToWay = entry.getWay();
        nearestToWaySegmentIndices = entry.getSegmentIndices();
        currentToNode = nearestNodeOnNearestWay;
    }

    private void resetNavigation(){
        currentToNode = null;
        currentFromNode = null;
        nearestFromWaySegmentIndices = null;
        nearestToWaySegmentIndices = null;
        clickedNodeFrom = null;
        clickedNodeTo = null;
        fromAddressFilter.resetAddress();
        toAddressFilter.resetAddress();
    }

    private void switchDirections() {
        textFieldFromNav.setSuggest(false);
        textFieldToNav.setSuggest(false);

        String currentFromTextCopy = textFieldFromNav.getText();
        Way currentFromWayCopy = currentFromWay;
        int[] currentNearestFromWaySegmentIndicesCopy = nearestFromWaySegmentIndices;
        Node clickedNodeFromCopy = clickedNodeFrom;

        textFieldFromNav.setText(textFieldToNav.getText());
        currentFromWay = currentToWay;
        nearestFromWaySegmentIndices = nearestToWaySegmentIndices;
        clickedNodeFrom = clickedNodeTo;

        textFieldToNav.setText(currentFromTextCopy);
        currentToWay = currentFromWayCopy;
        nearestToWaySegmentIndices = currentNearestFromWaySegmentIndicesCopy;
        clickedNodeTo = clickedNodeFromCopy;

        textFieldFromNav.setSuggest(true);
        textFieldToNav.setSuggest(true);
    }

    /**
     * helper method to create Alert windows
     * @param alertType The type of alert the pop up
     * @param title The title of the window
     * @param header the header for the text
     * @param text The text that is the content of the alert
     * @param buttons The buttons such as "Confirm" and "cancel"
     * @return An alert with the given information.
     */
    private Alert createAlert(Alert.AlertType alertType, String title, String header, String text, ButtonType... buttons) {
        Alert alert = new Alert(alertType, text, buttons);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        return alert;
    }

    private FadeTransition createFadeTransition(double duration, javafx.scene.Node target, boolean fadeIn) {
        FadeTransition ft = new FadeTransition(Duration.seconds(duration), target);
        if(fadeIn) {
            ft.setFromValue(0);
            ft.setToValue(1);
        } else {
            ft.setFromValue(1);
            ft.setToValue(0);
        }

        return ft;
    }

    /**
     * Starts the route finding process in its own thread <br>
     * Sets the time, distance, and if there is a special path feature, to the given label
     */
    @FXML
    public void getRoute() {
        routeNavigation.setupRoute(currentFromNode, currentToNode, currentFromWay, currentToWay, nearestFromWaySegmentIndices, nearestToWaySegmentIndices, (VehicleType) vehicleNavGroup.getSelectedToggle().getUserData(), radioButtonFastestNav.isSelected(), aStarNav.isSelected());
        routeNavigation.startRouting();

        routeNavigation.setOnSucceeded(e -> {
            mapData.setCurrentRoute(routeNavigation.getValue());
            setDistanceNav(routeNavigation.getTotalDistance());
            setTimeNav(routeNavigation.getTotalTime());
            setDirections(routeNavigation.getDirections());
            setSpecialPathFeatures(routeNavigation.getSpecialPathFeatures());
            mapCanvas.panToRoute(routeNavigation.getCoordinatesForPanToRoute());
            mapCanvas.repaint();
        });
        routeNavigation.setOnFailed(e -> {
            mapData.resetCurrentRoute();
            directionsList.getItems().clear();
            distanceNav.setVisible(false);
            timeNav.setVisible(false);
            mapCanvas.repaint();
            createAlert(Alert.AlertType.INFORMATION, "No Route Found", "No Route Found", "Could not find a route between the two points").showAndWait();
        });
        mapCanvas.repaint();
    }

    /**
     * Inserts the directions into the given ListView
     * @param directions the list of directions to be inserted in the ListView
     */
    public void setDirections(ArrayList<String> directions) {
        directionsList.getItems().removeAll(directionsList.getItems());
        int order = 1;
        for (String s : directions) {
            ListCell<String> l = new ListCell<>();
            l.setText(order + ". " + s);
            l.setEditable(false);
            l.setWrapText(true);
            l.setMaxWidth(directionsList.getWidth());
            directionsList.getItems().add(l);
            order++;
        }
    }

    public void setDistanceNav(double meters) {
        distanceNav.setVisible(true);
        String s = "Total distance: ";
        s += MapMath.formatDistance(meters, 2);
        distanceNav.setText(s);
    }

    public void setTimeNav(double seconds){
        timeNav.setVisible(true);
        String s = "Total time: ";
        s += MapMath.formatTime(seconds,2);
        timeNav.setText(s);
    }

    public void setSpecialPathFeatures(HashSet<String> specialPathFeatures) {
        StringBuilder labelText = new StringBuilder();
        for (String s : specialPathFeatures) {
            labelText.append("This route includes ").append(s).append("\n");
        }
        specialPathFeaturesNav.setText(labelText.toString());
        specialPathFeaturesNav.setVisible(true);
    }

    public void moveToPoint(int index) {
        Node node = mapData.getUserAddedPoints().get(index);
        mapCanvas.centerOnPoint(node.getxMax(), node.getyMax());
    }

    /**
     * Makes the user able to choose a point on the map, name it, and add it to the list of user points
     * this method is used by the "add" button
     */
    public void addUserPoint() {
        EventHandler<MouseEvent> event = new EventHandler<>() {
            @Override
            public void handle(MouseEvent e) {
                Point2D cursorPoint = mapCanvas.getTransCoords(e.getX(), e.getY());
                addUserPoint(cursorPoint);
                mapCanvas.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
            }
        };
        mapCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event);
    }

    /**
     *
     * @param point The point that the user chooses on the map to be saved to the list over user points
     *              this method is used by the right click menu and the other addUserPoint() method
     */
    private void addUserPoint(Point2D point){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Name Your Point");
        dialog.setHeaderText("Please enter a name for your point");
        dialog.setContentText("Point name:");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String name;
            if (result.get().equals("")) name = "Point " + (myPlacesListView.getItems().size() + 1);
            else name = result.get();
            Node node = new Node(0, (float) point.getX(), (float) point.getY());
            mapData.addToUserPointList(node);
            myPlacesListView.getItems().add(name);
            mapCanvas.repaint();
        }
    }

    /**
     * Deletes the selected point from the Listview containing the user's points on the map
     */
    @FXML
    public void deleteUserPoint(){
        if(myPlacesListView.getSelectionModel().getSelectedItem() != null){
            int i = myPlacesListView.getSelectionModel().getSelectedIndex();
            myPlacesListView.getItems().remove(i);
            mapData.getUserAddedPoints().remove(i);
            mapCanvas.repaint();
        }

    }

    /**
     * Add user point via the ContextMenu (right click menu)
     */
    @FXML
    public void rightClickAddUserPoint() {
        Point2D point = mapCanvas.getTransCoords(currentRightClick.getX(), currentRightClick.getY());
        addUserPoint(point);
    }

    /**
     * Finds the street name closest to where the user opened the context menu and updates the "from" AutoTextField
     * with the street name
     */
    @FXML
    public void rightClickPointNavFrom() {
        Point2D point =  mapCanvas.getTransCoords(currentRightClick.getX(), currentRightClick.getY());
        clickedNodeFrom = new Node(0, (float) point.getX(), (float) point.getY());
        fromAddressFilter.resetAddress();
        if (!navigationLeftPane.isVisible()) {
            navigationLeftPane.setVisible(true);
            address_myPlacesPane.setVisible(false);
        }
        setClosestFromWay(clickedNodeFrom);
    }

    /**
     * Finds the street name closest to where the context menu was opened and updates the "to" AutoTextField with the
     * street name
     */
    @FXML
    public void rightClickPointNavTo() {
        Point2D point =  mapCanvas.getTransCoords(currentRightClick.getX(), currentRightClick.getY());
        clickedNodeTo = new Node(0, (float) point.getX(), (float) point.getY());
        toAddressFilter.resetAddress();
        if (!navigationLeftPane.isVisible()) {
            navigationLeftPane.setVisible(true);
            address_myPlacesPane.setVisible(false);
        }
        setClosestToWay(clickedNodeTo);
    }

    public void toggleLeftPanel() {
        address_myPlacesPane.setVisible(showLeftView.isSelected());
    }

    @FXML
    public void toggleShortestFastest(){
        if(bikeNavToggleButton.isSelected()|| walkNavToggleButton.isSelected()){
            radioButtonShortestNav.setSelected(true);
            fastestShortestGroup.setVisible(false);
        }
        if(carNavToggleButton.isSelected()){
            fastestShortestGroup.setVisible(true);
            radioButtonShortestNav.setSelected(false);
        }
    }

    private enum State {
        MENU,
        LOADING,
        MAP
    }
}