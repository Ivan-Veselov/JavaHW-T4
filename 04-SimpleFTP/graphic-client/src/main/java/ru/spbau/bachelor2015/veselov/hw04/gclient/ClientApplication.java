package ru.spbau.bachelor2015.veselov.hw04.gclient;

import javafx.application.Application;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.gclient.ui.MainSceneProducer;

public class ClientApplication extends Application {
    @Override
    public void start(final @NotNull Stage primaryStage) throws Exception {
        primaryStage.setTitle("FTP client");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

        primaryStage.setScene(MainSceneProducer.produce(new ApplicationModel(primaryStage)));
        primaryStage.show();
    }

    public static void main(final @NotNull String[] args) {
        launch(args);
    }
}