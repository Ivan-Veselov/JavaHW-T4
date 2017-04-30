package ru.spbau.bachelor2015.veselov.hw04.gclient.ui;

import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;

public class SaveAsFileChooserProducer {
    public static @NotNull FileChooser produce(final @NotNull String fileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save as");
        fileChooser.setInitialFileName(fileName);

        return fileChooser;
    }
}
