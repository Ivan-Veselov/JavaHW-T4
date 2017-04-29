package ru.spbau.bachelor2015.veselov.hw04.gclient;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

final class MainSceneProducer {
    public static @NotNull Scene produce() {
        VBox vBox = new VBox();
        vBox.getChildren().add(ToolBarProducer.produce());

        Scene scene = new Scene(vBox);

        return scene;
    }
}
