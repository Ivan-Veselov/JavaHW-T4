package ru.spbau.bachelor2015.veselov.hw04.gclient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
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
import java.util.stream.Collectors;

/**
 * An application model class which represent a bare logic of a programme with minimum interface.
 */
public final class ApplicationModel {
    private @NotNull Stage mainStage;

    private @Nullable InetSocketAddress serverAddress;

    private @Nullable Path currentFolder;

    private final @NotNull ObservableList<FileEntryWrapper> currentFolderObservable = FXCollections.observableArrayList(
        new FileEntryWrapper(new FileEntry(Paths.get("root/folder1"), true)),
        new FileEntryWrapper(new FileEntry(Paths.get("root/folder2"), true)),
        new FileEntryWrapper(new FileEntry(Paths.get("root/file1"), false)),
        new FileEntryWrapper(new FileEntry(Paths.get("root/file2"), false)),
        new FileEntryWrapper(new FileEntry(Paths.get("root/file3"), false))
    );

    /**
     * Creates an application model.
     *
     * @param mainStage a main stage of a programme.
     */
    public ApplicationModel(final @NotNull Stage mainStage) {
        this.mainStage = mainStage;
    }

    /**
     * Returns main stage.
     */
    public @NotNull Stage getMainStage() {
        return mainStage;
    }

    /**
     * Sets new server address.
     *
     * @param serverAddress a new server address.
     */
    public void setServerAddress(final @NotNull InetSocketAddress serverAddress) {
        loadFolderContent(serverAddress, Paths.get("")).ifPresent(entries -> {
            this.serverAddress = serverAddress;

            currentFolderObservable.clear();
            currentFolderObservable.addAll(entries);
        });
    }

    /**
     * Sets new current folder which content will be displayed on the screen.
     *
     * @param path a path to new current folder.
     * @throws ServerAddressIsNotSetException if server address is not set.
     */
    public void setCurrentFolder(final @NotNull Path path) throws ServerAddressIsNotSetException {
        if (serverAddress == null) {
            throw new ServerAddressIsNotSetException();
        }

        loadFolderContent(serverAddress, path).ifPresent(entries -> {
            currentFolder = path;

            currentFolderObservable.clear();

            if (!path.equals(Paths.get(""))) {
                Path parent = path.getParent();
                if (parent == null) {
                    parent = Paths.get("");
                }

                currentFolderObservable.add(new FileEntryWrapper(new FileEntry(parent, true), ".."));
            }

            currentFolderObservable.addAll(entries);
        });
    }

    /**
     * Returns a special observable list which contains entries for current folder.
     */
    public @NotNull ObservableList<FileEntryWrapper> getCurrentFolderObservable() {
        return currentFolderObservable;
    }

    /**
     * Downloads a file to a given destination.
     *
     * @param pathToSource a path to a source file on server.
     * @param pathToDestination a local path to a destination where data will be written.
     * @throws ServerAddressIsNotSetException if server address is not set.
     */
    public void downloadFile(final @NotNull Path pathToSource,
                             final @NotNull Path pathToDestination) throws ServerAddressIsNotSetException{
        if (serverAddress == null) {
            throw new ServerAddressIsNotSetException();
        }

        try {
            Client.get(serverAddress, pathToSource, pathToDestination);
        } catch (IOException |
                 UnresolvedAddressException |
                 UnsupportedAddressTypeException |
                 ConnectionWasClosedException e) {
            showFailureAlert();
        }
    }

    private @NotNull Optional<List<FileEntryWrapper>> loadFolderContent(final @NotNull InetSocketAddress serverAddress,
                                                                 final @NotNull Path folder) {
        try {
            return Optional.of(Client.list(serverAddress, folder).stream()
                                                                 .map(FileEntryWrapper::new)
                                                                 .collect(Collectors.toList()));
        } catch (IOException |
                UnresolvedAddressException |
                UnsupportedAddressTypeException |
                ConnectionWasClosedException e) {
            showFailureAlert();

            return Optional.empty();
        }
    }

    private void showFailureAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("A connection failure");
        alert.setContentText("Failed to download data from given server address.");
        alert.showAndWait();
    }
}
