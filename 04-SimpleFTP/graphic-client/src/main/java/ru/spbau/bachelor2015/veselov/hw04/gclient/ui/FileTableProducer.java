package ru.spbau.bachelor2015.veselov.hw04.gclient.ui;

import javafx.beans.binding.StringBinding;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.gclient.ApplicationModel;
import ru.spbau.bachelor2015.veselov.hw04.gclient.exceptions.ServerAddressIsNotSetException;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FileEntry;

import java.io.File;

public final class FileTableProducer {
    public static @NotNull TableView produce(final @NotNull ApplicationModel model) {
        TableView<FileEntry> table = new TableView<>();

        table.setEditable(false);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<FileEntry, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setMinWidth(100);
        fileNameColumn.setCellValueFactory(param -> new StringBinding() {
            @Override
            protected @NotNull String computeValue() {
                return param.getValue().getFileName();
            }
        });

        TableColumn<FileEntry, String> fileTypeColumn = new TableColumn<>("Type");
        fileTypeColumn.setMinWidth(100);
        fileTypeColumn.setCellValueFactory(param -> new StringBinding() {
            @Override
            protected @NotNull String computeValue() {
                return param.getValue().isDirectory() ? "folder" : "";
            }
        });

        table.getColumns().addAll(fileNameColumn, fileTypeColumn);

        table.setRowFactory(param -> {
            TableRow<FileEntry> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() != 2 || row.isEmpty()) {
                    return;
                }

                FileEntry entry = row.getItem();
                if (!entry.isDirectory()) {
                    FileChooser fileChooser = SaveAsFileChooserProducer.produce(entry.getFileName());
                    File file = fileChooser.showSaveDialog(model.getMainStage());
                    if (file == null) {
                        return;
                    }

                    // download file
                } else {
                    try {
                        model.setCurrentFolder(row.getItem().getPath());
                    } catch (ServerAddressIsNotSetException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            return row;
        });

        table.setItems(model.getCurrentFolderObservable());
        return table;
    }
}
