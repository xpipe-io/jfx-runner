package io.xpipe.jfx_runner;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This class is intended to show that the Launcher will call the main method if there is one.
 *
 * If you called this main method directly, the JDK would complain about missing JavaFX components.
 */
public class SampleMain extends Application {

    public static void main(String[] args) {
        System.out.println("main");
        Application.launch(SampleMain.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.show();
    }
}
