package bfst21;

import bfst21.Osm_Elements.Node;
import bfst21.data_structures.AddressTrieNode;
import bfst21.data_structures.RouteNavigation;
import bfst21.exceptions.NoOSMInZipFileException;
import bfst21.exceptions.UnsupportedFileFormatException;
import bfst21.file_io.Loader;
import bfst21.file_io.Serializer;
import bfst21.utils.CustomKeyCombination;
import bfst21.utils.MapMath;
import bfst21.utils.VehicleType;
import bfst21.view.CanvasBounds;
import bfst21.view.MapCanvas;
import bfst21.view.Theme;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class Controller {
    private MapData mapData;
    private Creator creator;
    private RouteNavigation routeNavigation;

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

    private ArrayList<AddressTrieNode> currentAutoCompleteList;

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

    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;

    @FXML private TextField textFieldFromNav;
    @FXML private TextField textFieldToNav;
    @FXML private VBox autoCompleteFromNav;
    @FXML private ScrollPane ScrollpaneAutoCompleteToNav;
    @FXML private VBox autoCompleteToNav;
    @FXML private ScrollPane ScrollpaneAutoCompleteFromNav;

    @FXML private RadioButton radioButtonFastestNav;
    @FXML private RadioButton radioButtonShortestNav;
    @FXML private Label distanceAndTimeNav;
    @FXML private RadioMenuItem aStarNav;
    @FXML private VBox directionsBox;
    @FXML private ScrollPane directionsScrollPane;
    @FXML private Label specialPathFeaturesNav;

    @FXML private ComboBox<String> dropDownPoints;
    @FXML private TextField textFieldPointName;
    @FXML private Button addPointButton;

    public void init() {
        mapData = new MapData();
        routeNavigation = new RouteNavigation();
        loadThemes();
        initView();
        openFile();
    }

    private void initView() {
        themeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> setTheme(((RadioMenuItem) newValue.getToggleGroup().getSelectedToggle()).getUserData().toString()));
        mapCanvas.initTheme(Loader.loadTheme(themeGroup.getSelectedToggle().getUserData().toString()));
        scaleLabel.textProperty().bind(mapCanvas.getRatio());
        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (viaZoomSlider) zoom(newValue.intValue() - oldValue.intValue());
        });
        disableMenus();
        CustomKeyCombination.setTarget(mapCanvas);
        addListenerToSearchFields();
    }

    private void initMapCanvas() {
        mapCanvas.init(mapData);
        //TODO MOVE LISTENERS
        mapCanvas.widthProperty().addListener((observable, oldValue, newValue) -> setBoundsLabels());
        mapCanvas.heightProperty().addListener((observable, oldValue, newValue) -> setBoundsLabels());

        // TODO: 29-04-2021 textfields should remove text og user added points
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

    private void addListenerToSearchFields() {
        textFieldFromNav.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if (newValue.length() > 2) fillAutoCompleteText(autoCompleteFromNav, true);
            }
        });
        textFieldToNav.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if (newValue.length() > 2) fillAutoCompleteText(autoCompleteToNav, false);
            }
        });

    }

    private void removeChildren(){
        // TODO: 28-04-2021 Remove search when under 2 charachters
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
        else if (e.getCode().equals(KeyCode.UP)) mapCanvas.pan(0, acceleration);
        else if (e.getCode().equals(KeyCode.DOWN)) mapCanvas.pan(0, -acceleration);
        else if (e.getCode().equals(KeyCode.LEFT)) mapCanvas.pan(acceleration, 0);
        else if (e.getCode().equals(KeyCode.RIGHT)) mapCanvas.pan(-acceleration, 0);
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

    @FXML
    private void dumpBinary() {
        String contentText = "The dumping process of MapData takes excessive amount of memory. If not enough memory is available an Out of Memory Error might be thrown, causing the program to crash. \n\nDo you want to continue?";
        Alert warning = createAlert(Alert.AlertType.WARNING, "Dump MapData", "MapData Dump", contentText, ButtonType.YES, ButtonType.NO);
        warning.showAndWait();

        if (warning.getResult() == ButtonType.YES) {
            File file = showFileChooser("save").showSaveDialog(scene.getWindow());
            if (file != null) {
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
        routeNavigation.setNodeToWayMap(mapData.getNodeToHighWay());
        routeNavigation.setNodeToRestriction(mapData.getNodeToRestriction());
        routeNavigation.setWayToRestriction(mapData.getWayToRestriction());
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
        if (showMap) {
            state = State.MAP;
            showLoaderPane(false);
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
        coordsLabel.setText("Coordinates: (" + MapMath.round(x, 1) + ", " + MapMath.round(y, 1) + ")");
    }

    private void setGeoCoordsLabel(float x, float y) {
        geoCoordsLabel.setText("Geo-coordinates: (" + MapMath.round(x, 7) + ", " + MapMath.round(y, 7) + ")");
    }

    private void setNearestRoadLabel(double x, double y) {
        nearestRoadLabel.setText("Nearest Road: " + mapData.getNearestRoad((float) x, (float) y, kdTreeNearestNode.isSelected()));
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
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All OSM Files", "*.osm", "*zip", "*.bmapdata"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OSM File", "*.osm"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Zipped OSM File", "*zip"));
        } else if (option.equals("save")) {
            fileChooser.setTitle("Save File");
            fileChooser.setInitialFileName("binary_map_data");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Binary MapData", "*.bmapdata"));
        }

        return fileChooser;
    }

    private void fillAutoCompleteText(VBox autoComplete, boolean fromNav) {
        autoComplete.getChildren().removeAll(autoComplete.getChildren());
        fillAutoCompleteText(fromNav);
    }


    private void fillAutoCompleteText(boolean fromNav){
            if (fromNav) {
                for(AddressTrieNode addressNode : mapData.getAutoCompleteAdresses(textFieldFromNav.getText())) {
                    labelForAutoComplete(addressNode, autoCompleteFromNav, textFieldFromNav, ScrollpaneAutoCompleteFromNav, fromNav);
                    ScrollpaneAutoCompleteFromNav.setVisible(true);
                }
            } else {
                for(AddressTrieNode addressNode : mapData.getAutoCompleteAdresses(textFieldToNav.getText())) {
                    labelForAutoComplete(addressNode, autoCompleteToNav, textFieldToNav, ScrollpaneAutoCompleteToNav, fromNav);
                    ScrollpaneAutoCompleteToNav.setVisible(true);
                }
            }
    }


    private void labelForAutoComplete(AddressTrieNode addressNode, VBox autoComplete, TextField textField, ScrollPane scrollPane, boolean fromNav) {

        for (Map.Entry<Integer, String> entry : addressNode.getAddresses().entrySet()){
            String address = entry.getValue();
            Label label = new Label(address);
            autoComplete.getChildren().add(label);
            label.prefWidth(autoComplete.getWidth());
            label.setOnMouseClicked((ActionEvent) -> {
                autoComplete.getChildren().removeAll(autoComplete.getChildren());

                fullAddressLabelForAutoComplete(addressNode, autoComplete, textField, scrollPane, fromNav, entry.getKey());

            });
        }
    }

    private void fullAddressLabelForAutoComplete(AddressTrieNode addressNode, VBox autoComplete, TextField textField, ScrollPane scrollPane, boolean fromNav, int postcode) {

        for (Map.Entry<String, Node> houseNumber : addressNode.getHouseNumbersOnStreet(postcode).entrySet()) {
            String addressWithHouseNumber = houseNumber.getKey();
            Node node = houseNumber.getValue();
            Label labelHouseNumber = new Label(addressWithHouseNumber);
            autoComplete.getChildren().add(labelHouseNumber);
            labelHouseNumber.prefWidth(autoComplete.getWidth());
            labelHouseNumber.setOnMouseClicked((ActionEvent2) -> {
                System.out.println("here");
                textField.setText(addressWithHouseNumber);
                if (fromNav) currentFromNode = node;
                else currentToNode = node;
                autoComplete.getChildren().removeAll(autoComplete.getChildren());
                scrollPane.setVisible(false);
            });
        }
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
                Point2D coords = mapCanvas.getTransCoords(e.getX(), e.getY());
                Node nearestRoadNode = mapData.getNearestRoadNode((float) coords.getX(), (float) coords.getY());

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
        } else if (currentFromNode == currentToNode) {
            showDialogBox("Navigation Error", "From and to are the same entries");
        } else if (vehicleNavGroup.getSelectedToggle() == null) {
            showDialogBox("Navigation Error", "Please select a vehicle type");
        } else if (!radioButtonFastestNav.isSelected() && !radioButtonShortestNav.isSelected()) {
            showDialogBox("Navigation Error", "Please select either fastest or shortest");
        } else {
            getRoute();
        }
    }

    private void showDialogBox(String title, String contentText) {
        createAlert(Alert.AlertType.INFORMATION, title, null, contentText).showAndWait();
    }

    private Alert createAlert(Alert.AlertType alertType, String title, String header, String text, ButtonType... buttons) {
        Alert alert = new Alert(alertType, text, buttons);
        alert.setTitle(title);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        return alert;
    }

    @FXML
    public void getRoute() {
        routeNavigation.setupRoute(currentFromNode, currentToNode, (VehicleType) vehicleNavGroup.getSelectedToggle().getUserData(), radioButtonFastestNav.isSelected(), aStarNav.isSelected());
        routeNavigation.startRouting();

        routeNavigation.setOnSucceeded(e -> {
            mapData.setCurrentRoute(routeNavigation.getValue());
            setDistanceAndTimeNav(routeNavigation.getTotalDistance(), routeNavigation.getTotalTime());
            setDirections(routeNavigation.getDirections());
            setSpecialPathFeatures(routeNavigation.getSpecialPathFeatures());
            mapCanvas.repaint();
        });
        routeNavigation.setOnFailed(e -> showDialogBox("No Route Found", routeNavigation.getException().getMessage()));
        mapCanvas.repaint();
    }

    public void setDirections(ArrayList<String> directions) {
        directionsBox.getChildren().removeAll(directionsBox.getChildren());
        int order = 1;
        for (String s : directions) {
            Label l = new Label(order + ". " + s);
            directionsBox.getChildren().add(l);
            directionsBox.getChildren().add(new Label(""));
            order++;
        }
        directionsScrollPane.setMinSize(160, 200);
        directionsScrollPane.setPrefSize(160, 200);
        directionsScrollPane.setVisible(true);
    }

    public void setDistanceAndTimeNav(double distance, double time) {
        distanceAndTimeNav.setVisible(true);
        String s = "Total distance: ";

        if (distance < 1000) {
            s += MapMath.round(distance, 0) + " m";
        } else {
            s += MapMath.round(distance / 1000f, 3) + " km";
        }

        if (time < 60) {
            s += " , Total time: " + MapMath.round(time, 0) + " s";
        } else {
            s += " , Total time: " + MapMath.round(time / 60f, 3) + " min";
        }
        distanceAndTimeNav.setText(s);
    }

    public void hideDistanceAndTimeNav() {
        distanceAndTimeNav.setVisible(false);
    }

    public void setSpecialPathFeatures(HashSet<String> specialPathFeatures) {
        StringBuilder labelText = new StringBuilder();
        for (String s : specialPathFeatures) {
            labelText.append("This route includes ").append(s).append("\n");
        }
        specialPathFeaturesNav.setText(labelText.toString());
        specialPathFeaturesNav.setVisible(true);
    }

    @FXML
    public void moveToPoint(ActionEvent actionEvent) {
        int i = dropDownPoints.getSelectionModel().getSelectedIndex();
        Node node = mapData.getUserAddedPoints().get(i);
        mapCanvas.centerOnPoint(node.getxMin(), node.getyMin());
        mapCanvas.repaint();
    }

    @FXML
    public void addUserPoint(ActionEvent actionEvent) {
        if (textFieldPointName.getText().equals(""))
            showDialogBox("User added point error", "Please input name for your point");
        else {
            EventHandler<MouseEvent> event = new EventHandler<>() {
                @Override
                public void handle(MouseEvent e) {
                    Point2D cursorPoint = mapCanvas.getTransCoords(e.getX(), e.getY());
                    Node node = new Node((float) cursorPoint.getX(), (float) cursorPoint.getY());
                    //// TODO: 20-04-2021 make this work

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