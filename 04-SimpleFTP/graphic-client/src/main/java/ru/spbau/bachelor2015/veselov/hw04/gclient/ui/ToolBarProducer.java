package ru.spbau.bachelor2015.veselov.hw04.gclient.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ToolBar;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.gclient.ApplicationModel;

import java.net.InetSocketAddress;
import java.util.Optional;

public final class ToolBarProducer {
    public static @NotNull ToolBar produce(final @NotNull ApplicationModel model) {
        Dialog<InetSocketAddress> dialog = ServerChoiceDialogProducer.produce();

        Button chooseServerButton = new Button("Choose server");
        chooseServerButton.setOnAction(event -> {
            Optional<InetSocketAddress> result = dialog.showAndWait();
            result.ifPresent(model::setServerAddress);
        });

        ToolBar toolBar = new ToolBar(chooseServerButton);

        return toolBar;
    }
}
