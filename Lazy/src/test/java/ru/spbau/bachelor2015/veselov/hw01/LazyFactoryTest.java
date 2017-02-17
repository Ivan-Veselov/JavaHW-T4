package ru.spbau.bachelor2015.veselov.hw01;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class LazyFactoryTest {
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

    @Test
    public void incrementerTest() throws Exception {
        supplierTest(incrementalSuppliersFactory, 0);
        supplierOneEvaluationTest(incrementalSuppliersFactory.get(), 0);
    }

    @Test
    public void nullTest() throws Exception {
        supplierTest(incrementalWithNullSuppliersFactory, null);
        supplierOneEvaluationTest(incrementalWithNullSuppliersFactory.get(), null);
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

        // TODO: uncomment when method is implemented
        // basicTest(LazyFactory.createLockFreeLazy(suppliersFactory.get()), expectedValue);
    }

    private <T> void concurrentOneEvaluationTest(@NotNull Lazy<T> lazy, @Nullable T expectedValue) throws Exception {
        ArrayList<Thread> threadList = new ArrayList<>();
        final ArrayList<T> resultingList = new ArrayList<>();

        final int numberOfThreads = 10;
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

        T actualValue = resultingList.get(0);
        assertThat(actualValue, is(equalTo(expectedValue)));
        assertThat(resultingList, contains(Collections.nCopies(numberOfThreads, sameInstance(actualValue))));
    }

    private <T> void supplierOneEvaluationTest(@NotNull Supplier<T> supplier,
                                               @Nullable T expectedValue) throws Exception {
        concurrentOneEvaluationTest(LazyFactory.createConcurrentLazy(supplier), expectedValue);
    }
}