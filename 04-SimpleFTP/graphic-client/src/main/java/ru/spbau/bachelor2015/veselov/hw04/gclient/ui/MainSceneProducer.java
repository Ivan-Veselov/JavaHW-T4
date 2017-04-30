package ru.spbau.bachelor2015.veselov.hw04.gclient.ui;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.gclient.ApplicationModel;

public final class MainSceneProducer {
    public static @NotNull Scene produce(final @NotNull ApplicationModel model) {
        VBox vBox = new VBox();
        vBox.getChildren().addAll(ToolBarProducer.produce(model), FileTableProducer.produce(model));

        vBox.setFillWidth(true);

        Scene scene = new Scene(vBox);

        return scene;
    }
}
