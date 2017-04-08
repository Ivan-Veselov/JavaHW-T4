package ru.spbau.bachelor2015.veselov.hw02;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * A class which represents a Path wrapper and guarantees that inner Path object is an absolute path.
 */
public class AbsolutePath {
    private final @NotNull Path path;

    /**
     * Constructs an object for a given arbitrary Path object.
     *
     * @param path a Path object.
     */
    public AbsolutePath(final @NotNull Path path) {
        this.path = path.toAbsolutePath().normalize();
    }

    /**
     * Returns an underlying Path object.
     */
    public @NotNull Path getPath() {
        return path;
    }

    /**
     * Checks whether or not this path lies inside given absolute path.
     *
     * @param other given absolute path.
     * @return true if this path lies inside a given one, false otherwise.
     */
    public boolean isInside(final @NotNull AbsolutePath other) {
        return other.path.startsWith(this.path);
    }

    /**
     * Resolves this path with a given one.
     *
     * @param other a given path.
     * @return a new absolute path which is a result of resolution.
     */
    public @NotNull AbsolutePath resolve(final @NotNull Path other) {
        return new AbsolutePath(path.resolve(other));
    }

    /**
     * Compares this path with an arbitrary object on equivalence.
     *
     * @param o a given object.
     * @return true if given object is an absolute path object and equals for underlying Path object returns true, false
     *         otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbsolutePath)) {
            return false;
        }

        AbsolutePath other = (AbsolutePath) o;
        return path.equals(other.path);
    }
}
