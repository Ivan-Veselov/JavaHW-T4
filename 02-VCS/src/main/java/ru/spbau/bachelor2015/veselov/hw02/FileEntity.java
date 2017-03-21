package ru.spbau.bachelor2015.veselov.hw02;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;

public class FileEntity implements Serializable {
    private final @NotNull NormalRelativePath pathToFile;

    private final @NotNull SHA1Hash contentHash;

    public FileEntity(final @NotNull NormalRelativePath pathToFile, final @NotNull SHA1Hash contentHash) {
        this.pathToFile = pathToFile;

        this.contentHash = contentHash;
    }

    public FileEntity(final @NotNull NormalRelativePath pathToFile) throws IOException {
        this.pathToFile = pathToFile;

        this.contentHash = new SHA1Hash(pathToFile.toPath());
    }

    public @NotNull NormalRelativePath getPathToFile() {
        return pathToFile;
    }

    public @NotNull SHA1Hash getContentHash() {
        return contentHash;
    }
}
