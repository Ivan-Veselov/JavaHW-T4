package ru.spbau.bachelor2015.veselov.hw01;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A class which contains static methods that create different implementations of Lazy interface.
 */
public final class LazyFactory {
    public static <T> @NotNull Lazy<T> createLazy(@NotNull Supplier<T> supplier) {
        throw new UnsupportedOperationException();
    }

    public static <T> @NotNull Lazy<T> createConcurrentLazy(@NotNull Supplier<T> supplier) {
        throw new UnsupportedOperationException();
    }

    public static <T> @NotNull Lazy<T> createLockFreeLazy(@NotNull Supplier<T> supplier) {
        throw new UnsupportedOperationException();
    }

    private LazyFactory() {
        throw new UnsupportedOperationException();
    }
}
