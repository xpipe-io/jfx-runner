package io.xpipe.jfx_runner;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This class is intended to show that the launcher can call the Application.start() method directly if there is no main method
 */
public class SampleApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.show();
    }
}
