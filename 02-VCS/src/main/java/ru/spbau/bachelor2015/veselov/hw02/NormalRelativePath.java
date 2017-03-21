package ru.spbau.bachelor2015.veselov.hw02;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;

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

    @NotNull NormalRelativePath relativePath(final @NotNull Path path) {
        return new NormalRelativePath(toPath(), path);
    }

    public @NotNull Path toPath() {
        if (path.getName(0).toString().equals("")) {
            return basePath;
        }

        return basePath.resolve(path).normalize();
    }

    public @NotNull NormalRelativePath resolve(final @NotNull String path) {
        return new NormalRelativePath(basePath, toPath().resolve(path));
    }

    public boolean isInner() {
        return toPath().startsWith(basePath);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(basePath.toString());
        out.writeObject(path.toString());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        basePath = (Path) in.readObject();
        path = (Path) in.readObject();
    }
}
