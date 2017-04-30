package ru.spbau.bachelor2015.veselov.hw04.gclient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.hw04.client.Client;
import ru.spbau.bachelor2015.veselov.hw04.client.exceptions.ConnectionWasClosedException;
import ru.spbau.bachelor2015.veselov.hw04.gclient.exceptions.ServerAddressIsNotSetException;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FileEntry;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public final class ApplicationState {
    private @Nullable InetSocketAddress serverAddress;

    private @Nullable Path currentFolder;

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

    public @Nullable Path getCurrentFolder() {
        return currentFolder;
    }

    public void setServerAddress(final @NotNull InetSocketAddress serverAddress) {
        loadFolderContent(serverAddress, Paths.get("")).ifPresent(entries -> {
            this.serverAddress = serverAddress;

            currentFolderObservable.clear();
            currentFolderObservable.addAll(entries);
        });
    }

    public void setCurrentFolder(final @NotNull Path path) throws ServerAddressIsNotSetException {
        if (serverAddress == null) {
            throw new ServerAddressIsNotSetException();
        }

        loadFolderContent(serverAddress, path).ifPresent(entries -> {
            currentFolder = path;

            currentFolderObservable.clear();
            currentFolderObservable.addAll(entries);
        });
    }

    public @NotNull ObservableList<FileEntry> getCurrentFolderObservable() {
        return currentFolderObservable;
    }

    private @NotNull Optional<List<FileEntry>> loadFolderContent(final @NotNull InetSocketAddress serverAddress,
                                                                 final @NotNull Path folder) {
        try {
            return Optional.of(Client.list(serverAddress, folder));
        } catch (IOException |
                UnresolvedAddressException |
                UnsupportedAddressTypeException |
                ConnectionWasClosedException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("A connection failure");
            alert.setContentText("Failed to download data from given server address.");
            alert.showAndWait();

            return Optional.empty();
        }
    }
}
