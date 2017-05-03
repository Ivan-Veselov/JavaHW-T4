package ru.spbau.bachelor2015.veselov.hw04.gclient;

import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FileEntry;

/**
 * A file entry wrapper class which adds a new String name field which allows to create a specific String representation
 * of an entry for a list of entries on the screen.
 */
public class FileEntryWrapper {
    private final @NotNull FileEntry entry;

    private final @NotNull String name;

    /**
     * Creates a wrapper from file entry. This ctor uses real file entry name as string representation.
     *
     * @param entry an entry to create wrapper for.
     */
    public FileEntryWrapper(final @NotNull FileEntry entry) {
        this.entry = entry;

        name = entry.getFileName();
    }

    /**
     * Creates a wrapper from file entry.
     *
     * @param entry an entry to create wrapper for.
     * @param name a string represenation of an entry.
     */
    public FileEntryWrapper(final @NotNull FileEntry entry, final @NotNull String name) {
        this.entry = entry;
        this.name = name;
    }

    /**
     * Returns underlying entry.
     */
    public @NotNull FileEntry getEntry() {
        return entry;
    }

    /**
     * Returns a string represenation of an entry.
     */
    public @NotNull String getName() {
        return name;
    }
}
