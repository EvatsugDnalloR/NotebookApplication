package notebookapplication.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * Main application class that extends JavaFX Application.
 * Responsible for launching the application and setting up the primary stage.
 */
public class MainFrame extends Application {
    private Controller controller;

    /**
     * The main entry point for the JavaFX application.
     *
     * @param args the command line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * The main entry point for all JavaFX applications.
     *
     * <p>Called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * @param stage the primary stage for this application,
     *              onto which the application scene can be set
     * @throws Exception if something goes wrong during application startup
     */
    @Override
    public void start(Stage stage) throws Exception {
        controller = new Controller();
        FXMLLoader fxmlLoader = new FXMLLoader(MainFrame.class.getResource("MainFrame-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}