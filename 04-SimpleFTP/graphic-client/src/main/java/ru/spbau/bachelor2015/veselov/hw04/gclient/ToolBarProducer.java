package ru.spbau.bachelor2015.veselov.hw04.gclient;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import org.jetbrains.annotations.NotNull;

final class ToolBarProducer {
    public static @NotNull ToolBar produce() {
        ToolBar toolBar = new ToolBar(new Button("Hi"));

        return toolBar;
    }
}
