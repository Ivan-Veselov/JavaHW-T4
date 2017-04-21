package ru.spbau.bachelor2015.veselov.hw04.messages;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

/**
 * An ftp message which represents an answer on list request message.
 */
public class FTPListAnswerMessage implements FTPMessage {
    private final @NotNull List<Entry> content;

    /**
     * Creates a message.
     *
     * @param content a list of entries which this message stores.
     */
    public FTPListAnswerMessage(final @NotNull List<Entry> content) {
        this.content = new ArrayList<>(content);
    }

    /**
     * Returns a list of entries which this message stores.
     */
    public @NotNull List<Entry> getContent() {
        return new ArrayList<>(content);
    }

    /**
     * An entry of a file in a filesystem.
     */
    public static class Entry implements Serializable {
        private final @NotNull String path;

        private final boolean isDirectory;

        /**
         * Creates an entry.
         *
         * @param path a string representation of a file.
         * @param isDirectory a flag which tells whether or not this entry represents a directory.
         */
        public Entry(final @NotNull String path, final boolean isDirectory) {
            this.path = path;
            this.isDirectory = isDirectory;
        }

        /**
         * Returns a string representation of a file.
         */
        public @NotNull String getPath() {
            return path;
        }

        /**
         * Returns true if this entry represents a directory, false otherwise.
         */
        public boolean isDirectory() {
            return isDirectory;
        }
    }
}
