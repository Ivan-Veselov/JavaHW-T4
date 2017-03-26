package ru.spbau.bachelor2015.veselov.hw02;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class AbsolutePath {
    private final @NotNull Path path;

    public AbsolutePath(final @NotNull Path path) {
        this.path = path.toAbsolutePath().normalize();
    }

    public @NotNull Path getPath() {
        return path;
    }

    public boolean isInside(final @NotNull AbsolutePath other) {
        return other.path.startsWith(this.path);
    }

    public @NotNull AbsolutePath resolve(final @NotNull Path other) {
        return new AbsolutePath(path.resolve(other));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbsolutePath)) {
            return false;
        }

        AbsolutePath other = (AbsolutePath) o;
        return path.equals(other.path);
    }
}
