package ru.spbau.bachelor2015.veselov.hw04.gclient.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

public final class ServerChoiceDialogProducer {
    public static @NotNull Dialog<InetSocketAddress> produce() {
        Dialog<InetSocketAddress> dialog = new Dialog<>();
        dialog.setTitle("Server Choice Dialog");
        dialog.setHeaderText("Enter address of a server");

        ButtonType chooseButtonType = new ButtonType("Choose", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(chooseButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Node chooseButton = dialog.getDialogPane().lookupButton(chooseButtonType);

        HostPortTextFields textFields = new HostPortTextFields(chooseButton);

        grid.add(new Label("Host:"), 0, 0);
        grid.add(textFields.getHostTextField(), 1, 0);
        grid.add(new Label("Port:"), 0, 1);
        grid.add(textFields.getPortTextField(), 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == chooseButtonType) {
                return new InetSocketAddress(textFields.getHostTextField().getText(),
                                             Integer.parseInt(textFields.getPortTextField().getText()));
            }

            return null;
        });

        return dialog;
    }

    private static final class HostPortTextFields {
        private final @NotNull TextField host;

        private final @NotNull TextField port;

        private final @NotNull Node chooseButton;

        private boolean doesHostMakeDisabled = true;

        private boolean doesPortMakeDisabled = true;

        public HostPortTextFields(final @NotNull Node chooseButton) {
            host = new TextField();
            host.setPromptText("Host");

            host.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    doesHostMakeDisabled = newValue.isEmpty();
                    chooseButton.setDisable(isDisabled());
                }
            );

            port = new TextField();
            port.setPromptText("Port");

            port.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    doesPortMakeDisabled = false;

                    if (newValue.isEmpty()) {
                        doesPortMakeDisabled = true;
                    } else {
                        try {
                            Integer.parseInt(newValue);
                        } catch (NumberFormatException e) {
                            doesPortMakeDisabled = true;
                        }
                    }

                    chooseButton.setDisable(isDisabled());
                }
            );

            this.chooseButton = chooseButton;
            this.chooseButton.setDisable(true);
        }

        public @NotNull TextField getHostTextField() {
            return host;
        }

        public @NotNull TextField getPortTextField() {
            return port;
        }

        private boolean isDisabled() {
            return doesHostMakeDisabled || doesPortMakeDisabled;
        }
    }
}
