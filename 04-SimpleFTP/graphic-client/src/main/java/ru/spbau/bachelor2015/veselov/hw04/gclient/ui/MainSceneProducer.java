package ru.spbau.bachelor2015.veselov.hw04.gclient.ui;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

public final class MainSceneProducer {
    public static @NotNull Scene produce() {
        VBox vBox = new VBox();
        vBox.getChildren().add(ToolBarProducer.produce());

        Scene scene = new Scene(vBox);

        return scene;
    }
}
