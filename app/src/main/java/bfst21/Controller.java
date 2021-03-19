package bfst21;

import bfst21.view.MapCanvas;
import bfst21.view.MapSegment;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.FileChooser;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

public class Controller
{
    private MapSegment mapSegment;
    private Point2D currentMouse;
    private Point2D lastMouse;
    private Loader osmLoader;

    @FXML
    private MapCanvas mapCanvas;
    @FXML
    private Scene scene;
    @FXML
    private Label coordsLabel;

    public void init(Loader loader) throws IOException, XMLStreamException {
        this.osmLoader = loader;
       mapCanvas.init(loader);
        openFile();
    }

    @FXML
    public void onScroll(ScrollEvent e)
    {
        double factor = Math.pow(1.01, e.getDeltaY());
        mapCanvas.zoom(factor, new Point2D(e.getX(), e.getY()));
    }

    @FXML
    public void onMouseDragged(MouseEvent e)
    {
        double dx = e.getX() - lastMouse.getX();
        double dy = e.getY() - lastMouse.getY();

        mapCanvas.setCursor(Cursor.CLOSED_HAND);

        if(e.isPrimaryButtonDown()) mapCanvas.pan(dx, dy);

        currentMouse = new Point2D(e.getX(), e.getY());
        onMousePressed(e);
    }

    @FXML
    public void onMouseMoved(MouseEvent e)
    {
        mapCanvas.setCursor(Cursor.DEFAULT);
        setCoordsLabel(new Point2D(e.getX(), e.getY()));
        currentMouse = new Point2D(e.getX(), e.getY());
    }

    @FXML
    public void onMousePressed(MouseEvent e)
    {
        lastMouse = new Point2D(e.getX(), e.getY());
        currentMouse = new Point2D(e.getX(), e.getY());
        setCoordsLabel(new Point2D(e.getX(), e.getY()));
    }

    @FXML
    public void onMouseReleased(MouseEvent e)
    {
        mapCanvas.setCursor(Cursor.DEFAULT);
    }

    @FXML
    public void resetView()
    {
        mapCanvas.reset();
        setCoordsLabel(currentMouse);
    }

    @FXML
    public void openFile() throws IOException, XMLStreamException {
        File file = showFileChooser().showOpenDialog(scene.getWindow());

        if(file != null) loadFile(file.getAbsolutePath());
        else loadFile(getClass().getResource("").getPath()); //USE BINARY FILE
    }

    @FXML
    public void exit()
    {
        System.exit(0);
    }

    private void loadFile(String path) throws IOException, XMLStreamException {
        osmLoader.load(path);
    }

    private void setCoordsLabel(Point2D point)
    {
        Point2D coords = mapCanvas.getTransCoords(point.getX(), point.getY());
        double x = Math.round(coords.getX() * 10) / 10.0;
        double y = Math.round(coords.getY() * 10) / 10.0;
        coordsLabel.setText("Coords: " + x + ", " + y);
    }

    private FileChooser showFileChooser()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Open Street Map File", "*.xml", "*.zip"));

        return fileChooser;
    }
}
