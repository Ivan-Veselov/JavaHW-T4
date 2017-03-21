package ru.spbau.bachelor2015.veselov.hw02;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileEntity implements Serializable {
    private final @NotNull URI pathToFile;

    private final @NotNull SHA1Hash contentHash;

    public FileEntity(final @NotNull Path pathToFile, final @NotNull SHA1Hash contentHash) {
        this.pathToFile = pathToFile.toUri();

        this.contentHash = contentHash;
    }

    public FileEntity(final @NotNull Path pathToFile) throws IOException {
        this.pathToFile = pathToFile.toUri();

        this.contentHash = new SHA1Hash(pathToFile);
    }

    public @NotNull Path getPathToFile() {
        return Paths.get(pathToFile);
    }

    public @NotNull SHA1Hash getContentHash() {
        return contentHash;
    }
}
