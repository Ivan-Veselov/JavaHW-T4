package ru.spbau.bachelor2015.veselov.hw01;

import org.jetbrains.annotations.Nullable;

public interface Lazy<T> {
    @Nullable T get();
}
