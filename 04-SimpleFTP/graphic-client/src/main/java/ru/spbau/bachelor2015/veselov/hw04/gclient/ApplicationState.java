package ru.spbau.bachelor2015.veselov.hw04.gclient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FileEntry;

import java.net.InetSocketAddress;
import java.nio.file.Paths;

public final class ApplicationState {
    private @Nullable InetSocketAddress serverAddress;

    final @NotNull ObservableList<FileEntry> currentFolderObservable = FXCollections.observableArrayList(
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
        this.serverAddress = serverAddress;

        if (serverAddress == null) {
            return;
        }
    }

    public @NotNull ObservableList<FileEntry> getCurrentFolderObservable() {
        return currentFolderObservable;
    }
}
