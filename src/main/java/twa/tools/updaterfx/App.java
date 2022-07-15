package twa.tools.updaterfx;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twa.tools.updaterfx.controller.app.InterfaceController;

/**
 * Hauptklasse der Application.
 */
public class App extends Application {
    final private static Logger logger = LoggerFactory.getLogger(App.class);
    private static Application application;

    public static Application getApplication() {
        return application;
    }

    /**
     * Start application with parameters.
     *
     * @param args Parameters.
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        InterfaceController.getInstance().start(stage);
    }
}
