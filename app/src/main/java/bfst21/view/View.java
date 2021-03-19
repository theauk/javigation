package bfst21.view;

import bfst21.Controller;
import bfst21.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class View
{
    public View(Map map, Stage stage) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view.fxml"));
        Scene scene = fxmlLoader.load();
        stage.setScene(scene);
        stage.setTitle("Working Title...");

        Controller controller = fxmlLoader.getController();
        stage.show();
        controller.init(map);
    }
}
