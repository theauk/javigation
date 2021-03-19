package bfst21.view;

import bfst21.Controller;
import bfst21.Loader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class View
{
    public View(Loader loader, Stage stage) throws IOException, XMLStreamException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view.fxml"));
        Scene scene = fxmlLoader.load();
        stage.setScene(scene);
        stage.setTitle("Working Title...");

        Controller controller = fxmlLoader.getController();
        stage.show();
        controller.init(loader);
    }
}
