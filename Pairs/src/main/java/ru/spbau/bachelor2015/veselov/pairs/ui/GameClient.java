package ru.spbau.bachelor2015.veselov.pairs.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.pairs.ClientModel;

/**
 * Application main class.
 */
public class GameClient extends Application {
    /**
     * JavaFX application entry point.
     *
     * @param primaryStage a primary stage of an application.
     * @throws Exception throws any uncaught exception.
     */
    @Override
    public void start(final @NotNull Stage primaryStage) throws Exception {
        ClientModel model = new ClientModel(primaryStage);

        Scene scene = new Scene(GridPaneProducer.produce(model), 500, 500);

        primaryStage.setTitle("Pairs");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Entry point of a programme.
     *
     * @param args arguments are ignored.
     */
    public static void main(final @NotNull String[] args) {
        launch(args);
    }
}
