package ru.spbau.bachelor2015.veselov.hw04.gclient.ui;

import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.gclient.ApplicationModel;

/**
 * Special class which has only one method to produce main scene widget.
 */
public final class MainSceneProducer {
    /**
     * Returns initialized widget.
     *
     * @param model an application model.
     */
    public static @NotNull Scene produce(final @NotNull ApplicationModel model) {
        TableView fileTable = FileTableProducer.produce(model);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(ToolBarProducer.produce(model), fileTable);

        vBox.setFillWidth(true);
        VBox.setVgrow(fileTable, Priority.ALWAYS);

        Scene scene = new Scene(vBox);

        return scene;
    }
}
