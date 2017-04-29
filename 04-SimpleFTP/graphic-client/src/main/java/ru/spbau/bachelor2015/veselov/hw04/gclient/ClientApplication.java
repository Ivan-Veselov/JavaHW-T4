package ru.spbau.bachelor2015.veselov.hw04.gclient;

import javafx.application.Application;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientApplication extends Application {
    private @Nullable String host;

    private int port;

    @Override
    public void start(final @NotNull Stage primaryStage) throws Exception {
        primaryStage.setTitle("FTP client");
        primaryStage.setScene(MainSceneProducer.produce());
        primaryStage.show();
    }

    public static void main(final @NotNull String[] args) {
        launch(args);
    }
}