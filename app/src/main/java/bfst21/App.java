package bfst21;

import bfst21.view.MapSegment;
import bfst21.view.View;
import javafx.application.Application;
import javafx.stage.Stage;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class App extends Application
{
    @Override
    public void start(Stage primaryStage) throws IOException, XMLStreamException {
        var filename = "C:\\Users\\Stribe\\IdeaProjects\\BFST21Group10\\app\\src\\main\\resources\\map.zip";
        //var filename = getParameters().getRaw().get(0);
        var loader = new Loader();
        loader.load(filename);
        new View(loader, primaryStage);
    }
}