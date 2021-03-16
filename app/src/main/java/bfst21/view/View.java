package bfst21.view;

import bfst21.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class View
{
    public View(MapSegment mapSegment, Stage stage) throws IOException
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view.fxml"));
        Scene scene = loader.load();
        stage.setScene(scene);
        stage.setTitle("Working Title...");

        Controller controller = loader.getController();
        stage.show();
        controller.init(mapSegment);
    }
}
