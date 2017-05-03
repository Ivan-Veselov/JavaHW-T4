package ru.spbau.bachelor2015.veselov.hw04.gclient.ui;

import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;

/**
 * Special class which has only one method to produce file chooser widget for file saving.
 */
public class SaveAsFileChooserProducer {
    /**
     * Returns initialized widget.
     *
     * @param fileName a default file name.
     */
    public static @NotNull FileChooser produce(final @NotNull String fileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save as");
        fileChooser.setInitialFileName(fileName);

        return fileChooser;
    }
}
