package ru.spbau.bachelor2015.veselov.hw04.gclient;

import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FileEntry;

public class FileEntryWrapper {
    private final @NotNull FileEntry entry;

    private final @NotNull String name;

    public FileEntryWrapper(final @NotNull FileEntry entry) {
        this.entry = entry;

        name = entry.getFileName();
    }

    public FileEntryWrapper(final @NotNull FileEntry entry, final @NotNull String name) {
        this.entry = entry;
        this.name = name;
    }

    public @NotNull FileEntry getEntry() {
        return entry;
    }

    public @NotNull String getName() {
        return name;
    }
}
