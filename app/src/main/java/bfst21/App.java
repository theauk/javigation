package bfst21;

import bfst21.view.View;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application
{
    @Override
    public void start(Stage primaryStage) throws IOException
    {
        new View(new MapData(), primaryStage);
    }
}