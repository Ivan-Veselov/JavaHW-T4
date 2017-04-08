package ru.spbau.bachelor2015.veselov.hw02;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * A class which represents a named version of an arbitrary class.
 *
 * @param <T> an arbitrary class.
 */
public class Named<T> implements Serializable, Comparable<Named<?>> {
    private final @NotNull T object;

    private final @NotNull String name;

    /**
     * Replaces object with a given name on another object. A resulting named object is returned.
     *
     * @param namedObject named object.
     * @param object unnamed object.
     * @param <U> type of unnamed object.
     * @return a resulting named object.
     */
    public static <U> @NotNull Named<U> replace(final @NotNull Named<?> namedObject, final @NotNull U object) {
        return new Named<>(object, namedObject.getName());
    }

    /**
     * Constructs a named version of given object.
     *
     * @param object an object of type T.
     * @param name a name for an object.
     */
    public Named(final @NotNull T object, final @NotNull String name) {
        this.object = object;
        this.name = name;
    }

    /**
     * Returns an underlying object.
     */
    public @NotNull T getObject() {
        return object;
    }

    /**
     * Returns a name assigned for an underlying object.
     */
    public @NotNull String getName() {
        return name;
    }

    @Override
    public int compareTo(Named<?> o) {
        return name.compareTo(o.getName());
    }
}
