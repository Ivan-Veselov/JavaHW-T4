package ru.spbau.bachelor2015.veselov.hw04.messages;

import org.jetbrains.annotations.NotNull;

/**
 * An ftp request message. This message asks server to send a content of a specified file.
 */
public class FTPGetMessage implements FTPMessage {
    private final @NotNull String path;

    /**
     * Creates a message.
     *
     * @param path a string representation of a path to a file which content will be requested.
     */
    public FTPGetMessage(final @NotNull String path) {
        this.path = path;
    }

    /**
     * Returns a string representation of a path to a file which content will be requested.
     */
    public @NotNull String getPath() {
        return path;
    }
}
