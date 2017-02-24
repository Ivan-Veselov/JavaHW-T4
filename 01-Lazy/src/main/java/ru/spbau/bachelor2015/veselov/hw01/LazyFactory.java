package ru.spbau.bachelor2015.veselov.hw01;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * A class which contains static methods that create different implementations of Lazy interface.
 */
public final class LazyFactory {
    /**
     * Creates simple lazy object for given supplier. Simple lazy object calls supplier get method only once to
     * evaluate result. This result is stored in a class field and subsequent calls of get method will return this
     * memorized value.
     *
     * @param supplier an object which evaluates resulting value.
     * @param <T> a resulting value type.
     * @return new simple lazy object.
     */
    public static <T> @NotNull Lazy<T> createLazy(@NotNull final Supplier<T> supplier) {
        return new SimpleLazy<>(supplier);
    }

    /**
     * Creates thread-safe version of lazy object for given supplier. Method get will be locked until result is
     * evaluated. It is guaranteed that at most one thread will call get method of supplier.
     *
     * @param supplier an object which evaluates resulting value.
     * @param <T> a resulting value type.
     * @return new concurrent lazy object.
     */
    public static <T> @NotNull Lazy<T> createConcurrentLazy(@NotNull final Supplier<T> supplier) {
        return new ConcurrentLazy<>(supplier);
    }

    /**
     * Creates lock-free version of multithreading lazy object for given supplier. If multiple threads will call get
     * method of such lazy object in parallel then there is a chance that get method of the underlying supplier will be
     * called more than once. In the end one single result will be chosen and other will be discarded.
     *
     * @param supplier an object which evaluates resulting value.
     * @param <T> a resulting value type.
     * @return new lock-free lazy object.
     */
    public static <T> @NotNull Lazy<T> createLockFreeLazy(@NotNull final Supplier<T> supplier) {
        return new LockFreeLazy<>(supplier);
    }

    private LazyFactory() {
        throw new UnsupportedOperationException();
    }

    private static final class SimpleLazy<T> implements Lazy<T> {
        // An object which denotes that value has not been received from supplier yet.
        private static final Object emptinessMarker = new Object();

        private Supplier<T> supplier;

        private Object value = emptinessMarker;

        public SimpleLazy(@NotNull final Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nullable T get() {
            if (value == emptinessMarker) {
                value = supplier.get();
                supplier = null;
            }

            return (T) value;
        }
    }

    private static final class ConcurrentLazy<T> implements Lazy<T> {
        // An object which denotes that value has not been received from supplier yet.
        private static final Object emptinessMarker = new Object();

        private Supplier<T> supplier;

        private volatile Object value = emptinessMarker;

        public ConcurrentLazy(@NotNull final Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nullable T get() {
            if (value == emptinessMarker) {
                // emptinessMarker final object is a thread guard for value reference
                synchronized (emptinessMarker) {
                    if (value == emptinessMarker) {
                        value = supplier.get();
                        supplier = null;
                    }
                }
            }

            return (T) value;
        }
    }

    private static final class LockFreeLazy<T> implements Lazy<T> {
        // An object which denotes that value has not been received from supplier yet.
        private static final Object emptinessMarker = new Object();

        private Supplier<T> supplier;

        private AtomicReference<Object> value = new AtomicReference<>(emptinessMarker);

        public LockFreeLazy(@NotNull final Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nullable T get() {
            if (value.get() == emptinessMarker) {
                value.compareAndSet(emptinessMarker, supplier.get());
            }

            return (T) value.get();
        }
    }
}
