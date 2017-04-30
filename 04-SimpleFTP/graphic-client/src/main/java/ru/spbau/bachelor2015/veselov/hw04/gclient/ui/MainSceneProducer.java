package ru.spbau.bachelor2015.veselov.hw04.gclient.ui;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.gclient.ApplicationState;

public final class MainSceneProducer {
    public static @NotNull Scene produce(final @NotNull ApplicationState state) {
        VBox vBox = new VBox();
        vBox.getChildren().addAll(ToolBarProducer.produce(state), FileTableProducer.produce(state));

        vBox.setFillWidth(true);

        Scene scene = new Scene(vBox);

        return scene;
    }
}
