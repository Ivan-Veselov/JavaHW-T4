package ru.spbau.bachelor2015.veselov.hw02;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NormalRelativePath implements Serializable {
    private @NotNull Path basePath;

    private @NotNull Path path;

    public NormalRelativePath(final @NotNull Path path) {
        this(path, path);
    }

    public NormalRelativePath(final @NotNull Path basePath, final @NotNull Path path) {
        this.basePath = basePath.toAbsolutePath().normalize();
        this.path = this.basePath.relativize(path.toAbsolutePath().normalize());
    }

    public @NotNull NormalRelativePath relativePath(final @NotNull Path path) {
        return new NormalRelativePath(realPath(), path);
    }

    public @NotNull Path realPath() {
        if (path.getName(0).toString().equals("")) {
            return basePath;
        }

        return basePath.resolve(path).normalize();
    }

    public @NotNull Path relativePath() {
        return path;
    }

    public @NotNull NormalRelativePath resolve(final @NotNull String path) {
        return new NormalRelativePath(basePath, realPath().resolve(path));
    }

    public @NotNull NormalRelativePath shifted() {
        return new NormalRelativePath(basePath.resolve(path.getName(0)), realPath());
    }

    public boolean isInner() {
        return realPath().startsWith(basePath);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(basePath.toString());
        out.writeObject(path.toString());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        basePath = Paths.get((String) in.readObject());
        path = Paths.get((String) in.readObject());
    }
}
