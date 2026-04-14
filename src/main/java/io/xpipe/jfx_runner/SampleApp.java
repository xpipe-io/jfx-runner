package io.xpipe.jfx_runner;

import javafx.application.Application;
import javafx.stage.Stage;

public class SampleApp extends Application {

    static void main(String[] args) {
        System.out.println("main");
        Application.launch(SampleApp.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.show();
    }
}
