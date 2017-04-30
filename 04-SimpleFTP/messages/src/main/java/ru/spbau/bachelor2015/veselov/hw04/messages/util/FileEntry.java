package ru.spbau.bachelor2015.veselov.hw04.messages.util;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.nio.file.Path;

/**
 * An entry of a file in a filesystem.
 */
public class FileEntry implements Serializable {
    private final @NotNull IndependentPath path;

    private final boolean isDirectory;

    /**
     * Creates an entry.
     *
     * @param path a path to a file.
     * @param isDirectory a flag which tells whether or not this entry represents a directory.
     */
    public FileEntry(final @NotNull Path path, final boolean isDirectory) {
        this.path = new IndependentPath(path);
        this.isDirectory = isDirectory;
    }

    /**
     * Returns a path to a file.
     */
    public @NotNull Path getPath() {
        return path.toPath();
    }

    /**
     * Returns true if this entry represents a directory, false otherwise.
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Returns a string representation of this file entry.
     */
    public @NotNull String toString() {
        if (isDirectory) {
            return getPath() + " [directory]";
        }

        return getPath().toString();
    }
}
