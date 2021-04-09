package bfst21.view;

import bfst21.Controller;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class View
{
    public static final int WIDTH = 640;
    public static final int HEIGHT = WIDTH / 4 * 3; //Ratio 1:4

    public View(Stage stage) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view.fxml"));
        Scene scene = fxmlLoader.load();
        stage.setScene(scene);
        stage.setTitle("Working Title...");
        stage.setMinWidth(WIDTH);
        stage.setMinHeight(HEIGHT);

        Controller controller = fxmlLoader.getController();
        stage.show();
        controller.init();
    }
}
