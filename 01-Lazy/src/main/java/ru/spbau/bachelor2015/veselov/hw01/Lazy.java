package ru.spbau.bachelor2015.veselov.hw01;

import org.jetbrains.annotations.Nullable;

/**
 * An interface which represents an evaluation. It will be computed once and it's result will be memorized for
 * subsequent queries.
 *
 * @param <T> type of evaluation result.
 */
public interface Lazy<T> {
    /**
     * A method which returns result of evaluation. At some point result will be calculated and memorized. After that
     * this method will simply return this saved value on every call.
     *
     * @return result of evaluation.
     */
    @Nullable T get();
}
