package ru.spbau.bachelor2015.veselov.hw01;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public final class LazyFactoryTest {
    private final Supplier<Supplier<Integer>> incrementalSuppliersFactory =
            () -> new Supplier<Integer>() {
                private int value = 0;

                @Override
                public Integer get() {
                    return value++;
                }
            };

    private final Supplier<Supplier<Integer>> incrementalWithNullSuppliersFactory =
            () -> new Supplier<Integer>() {
                private int value = 0;

                @Override
                public Integer get() {
                    return value++ == 0 ? null : value;
                }
            };

    private final List<Integer> numbers = Stream.iterate(0, n -> n + 1).limit(10).collect(Collectors.toList());

    private final Supplier<Supplier<Integer>> numbersSuppliersFactory =
            () -> new Supplier<Integer>() {
                private Random generator = new Random();

                @Override
                public Integer get() {
                    return numbers.get(generator.nextInt(numbers.size()));
                }
            };

    @Test
    public void incrementerTest() throws Exception {
        supplierTest(incrementalSuppliersFactory, 0);
        supplierOneEvaluationTest(incrementalSuppliersFactory, 0);
    }

    @Test
    public void nullTest() throws Exception {
        supplierTest(incrementalWithNullSuppliersFactory, null);
        supplierOneEvaluationTest(incrementalWithNullSuppliersFactory, null);
    }

    @Test
    public void lockFreeTest() throws Exception {
        supplierMultipleEvaluationTest(numbersSuppliersFactory, numbers);
    }

    private <T> void basicTest(@NotNull Lazy<T> lazy, @Nullable T expectedValue) throws Exception {
        T actualValue = lazy.get();
        assertThat(actualValue, is(equalTo(expectedValue)));

        final int numberOfIterations = 10;
        for (int i = 0; i < numberOfIterations; ++i) {
            assertThat(lazy.get(), is(sameInstance(actualValue)));
        }
    }

    private <T> void supplierTest(@NotNull Supplier<Supplier<T>> suppliersFactory, @Nullable T expectedValue)
            throws Exception {
        basicTest(LazyFactory.createLazy(suppliersFactory.get()), expectedValue);
        basicTest(LazyFactory.createConcurrentLazy(suppliersFactory.get()), expectedValue);
        basicTest(LazyFactory.createLockFreeLazy(suppliersFactory.get()), expectedValue);
    }

    private <T> void concurrentOneEvaluationTest(@NotNull Lazy<T> lazy, @Nullable T expectedValue) throws Exception {
        concurrentMultipleEvaluationsTest(lazy, Collections.singletonList(expectedValue));
    }

    private <T> void supplierOneEvaluationTest(@NotNull Supplier<Supplier<T>> suppliersFactory,
                                               @Nullable T expectedValue) throws Exception {
        concurrentOneEvaluationTest(LazyFactory.createConcurrentLazy(suppliersFactory.get()), expectedValue);
    }

    private <T> void concurrentMultipleEvaluationsTest(@NotNull Lazy<T> lazy, @NotNull List<T> possibleValues)
            throws Exception {
        ArrayList<Thread> threadList = new ArrayList<>();
        final ArrayList<T> resultingList = new ArrayList<>();

        final int numberOfThreads = 100;
        for (int i = 0; i < numberOfThreads; ++i) {
            threadList.add(new Thread(() -> {
                T result = lazy.get();
                synchronized (resultingList) {
                    resultingList.add(result);
                }
            }));
        }

        for (Thread thread : threadList) {
            thread.start();
        }

        for (Thread thread : threadList) {
            thread.join();
        }

        assertThat(resultingList, hasSize(numberOfThreads));
        T actualValue = resultingList.get(0);
        assertThat(actualValue, isIn(possibleValues));
        assertThat(resultingList, contains(Collections.nCopies(numberOfThreads, sameInstance(actualValue))));
    }

    private <T> void supplierMultipleEvaluationTest(@NotNull Supplier<Supplier<T>> suppliersFactory,
                                                    @NotNull List<T> possibleValues) throws Exception {
        concurrentMultipleEvaluationsTest(LazyFactory.createLockFreeLazy(suppliersFactory.get()), possibleValues);
    }
}
