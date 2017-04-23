package ru.spbau.bachelor2015.veselov.hw04;

import org.jetbrains.annotations.NotNull;

/**
 * A request ftp message. This message asks server to list the content of a specified folder.
 */
public class FTPListMessage implements FTPMessage {
    private final @NotNull String path;

    /**
     * Creates a message.
     *
     * @param path a string representation of a path to a folder which content will be requested.
     */
    public FTPListMessage(final @NotNull String path) {
        this.path = path;
    }

    /**
     * Returns string representation of a path to a folder.
     */
    public @NotNull String getPath() {
        return path;
    }
}