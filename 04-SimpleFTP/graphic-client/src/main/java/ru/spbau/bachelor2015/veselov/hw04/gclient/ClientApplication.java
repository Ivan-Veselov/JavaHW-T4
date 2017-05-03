package ru.spbau.bachelor2015.veselov.hw04.gclient;

import javafx.application.Application;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.gclient.ui.MainSceneProducer;

/**
 * A main class which is an entry point of this application.
 */
public class ClientApplication extends Application {
    /**
     * This method is called by JavaFX when all initialization has been done.
     *
     * @param primaryStage a primary stage of this application.
     * @throws Exception any uncaught exception.
     */
    @Override
    public void start(final @NotNull Stage primaryStage) throws Exception {
        primaryStage.setTitle("FTP client");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

        primaryStage.setScene(MainSceneProducer.produce(new ApplicationModel(primaryStage)));
        primaryStage.show();
    }

    /**
     * Entry point of a programme.
     *
     * @param args arguments are passed to JavaFX library.
     */
    public static void main(final @NotNull String[] args) {
        launch(args);
    }
}