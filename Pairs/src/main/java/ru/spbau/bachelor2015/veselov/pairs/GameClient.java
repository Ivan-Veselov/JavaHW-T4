package ru.spbau.bachelor2015.veselov.pairs;

import com.sun.istack.internal.NotNull;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.spbau.bachelor2015.veselov.pairs.ui.GridPaneProducer;

public class GameClient extends Application {
    @Override
    public void start(final @NotNull Stage primaryStage) throws Exception {
        ClientModel model = new ClientModel();

        Scene scene = new Scene(GridPaneProducer.produce(model), 500, 500);

        primaryStage.setTitle("Pairs");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(final @NotNull String[] args) {
        launch(args);
    }
}
