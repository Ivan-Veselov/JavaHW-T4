package ru.spbau.bachelor2015.veselov.hw04.gclient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.hw04.client.Client;
import ru.spbau.bachelor2015.veselov.hw04.client.exceptions.ConnectionWasClosedException;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FileEntry;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.file.Paths;
import java.util.List;

public final class ApplicationState {
    private @Nullable InetSocketAddress serverAddress;

    private final @NotNull ObservableList<FileEntry> currentFolderObservable = FXCollections.observableArrayList(
        new FileEntry(Paths.get("root/folder1"), true),
        new FileEntry(Paths.get("root/folder2"), true),
        new FileEntry(Paths.get("root/file1"), false),
        new FileEntry(Paths.get("root/file2"), false),
        new FileEntry(Paths.get("root/file3"), false)
    );

    public ApplicationState() {
    }

    public @Nullable InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(final @Nullable InetSocketAddress serverAddress) {
        if (serverAddress == null) {
            this.serverAddress = null;
            return;
        }

        List<FileEntry> entries;
        try {
            entries = Client.list(serverAddress, Paths.get(""));
        } catch (IOException |
                 UnresolvedAddressException |
                 UnsupportedAddressTypeException |
                 ConnectionWasClosedException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("A connection failure");
            alert.setContentText("Failed to download data from given server address.");
            alert.showAndWait();

            return;
        }

        this.serverAddress = serverAddress;

        currentFolderObservable.clear();
        currentFolderObservable.addAll(entries);
    }

    public @NotNull ObservableList<FileEntry> getCurrentFolderObservable() {
        return currentFolderObservable;
    }
}
