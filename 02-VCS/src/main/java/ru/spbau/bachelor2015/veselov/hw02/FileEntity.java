package ru.spbau.bachelor2015.veselov.hw02;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Objects of this class represent a file which may not actually exist in a file system. Entity has a path in file
 * system and a SHA1 hash of a content.
 */
public class FileEntity implements Serializable {
    private @NotNull Path pathToFile;

    private @NotNull SHA1Hash contentHash;

    /**
     * Creates entity.
     *
     * @param pathToFile a path to a file.
     * @param contentHash hash of a content.
     */
    public FileEntity(final @NotNull Path pathToFile, final @NotNull SHA1Hash contentHash) {
        this.pathToFile = pathToFile;

        this.contentHash = contentHash;
    }

    /**
     * Returns path to a file of this entity.
     */
    public @NotNull Path getPathToFile() {
        return pathToFile;
    }

    /**
     * Returns a hash of a content of this entity.
     */
    public @NotNull SHA1Hash getContentHash() {
        return contentHash;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(pathToFile.toString());
        out.writeObject(contentHash);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        pathToFile = Paths.get((String) in.readObject());
        contentHash = (SHA1Hash) in.readObject();
    }
}
