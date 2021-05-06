package bfst21.view;

import bfst21.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class View {
    public static final int WIDTH = 640;
    public static final int HEIGHT = WIDTH / 4 * 3; //Ratio 4:3

    public View(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view.fxml"));
        Scene scene = fxmlLoader.load();
        stage.setScene(scene);
        stage.setTitle("Javigation");
        stage.setMinWidth(WIDTH);
        stage.setMinHeight(HEIGHT);

        Controller controller = fxmlLoader.getController();
        stage.show();
        controller.init();
    }
}
