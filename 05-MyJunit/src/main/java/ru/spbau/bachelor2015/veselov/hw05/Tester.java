package ru.spbau.bachelor2015.veselov.hw05;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.hw05.annotations.*;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.*;
import ru.spbau.bachelor2015.veselov.hw05.reports.FailureReport;
import ru.spbau.bachelor2015.veselov.hw05.reports.PassReport;
import ru.spbau.bachelor2015.veselov.hw05.reports.TestReport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

// TODO: specify invalidity exceptions, especially check invoke method exception reasons. Don't forget about tests.
public class Tester {
    private final Class<?> testClass;

    private final List<Method> testMethods;

    private final List<Method> beforeMethods;

    private final List<Method> afterMethods;

    private final List<Method> beforeClassMethods;

    private final List<Method> afterClassMethods;

    public Tester(final @NotNull Class<?> testClass) {
        this.testClass = testClass;

        testMethods = new ArrayList<>();
        beforeMethods = new ArrayList<>();
        afterMethods = new ArrayList<>();
        beforeClassMethods = new ArrayList<>();
        afterClassMethods = new ArrayList<>();

        for (Method method : testClass.getMethods()) {
            if (method.getAnnotation(Test.class) != null) {
                testMethods.add(method);
            }

            if (method.getAnnotation(Before.class) != null) {
                beforeMethods.add(method);
            }

            if (method.getAnnotation(After.class) != null) {
                afterMethods.add(method);
            }

            if (method.getAnnotation(BeforeClass.class) != null) {
                beforeClassMethods.add(method);
            }

            if (method.getAnnotation(AfterClass.class) != null) {
                afterClassMethods.add(method);
            }
        }
    }

    public @NotNull List<TestReport> test()
            throws InvalidTestClassException,
            BeforeClassStageFailedException,
            AfterClassStageFailedException,
            NonStaticAfterClassMethodException,
            NonStaticBeforeClassMethodException {
        List<TestReport> reports = new ArrayList<>();

        try {
            invokeMethods(null, beforeClassMethods);
        } catch (InvocationTargetException e) {
            throw new BeforeClassStageFailedException();
        } catch (NullPointerException e) {
            throw new NonStaticBeforeClassMethodException();
        }

        for (Method method : testMethods) {
            Object instance = instantiateObject();

            try {
                invokeMethods(instance, beforeMethods);
                method.invoke(instance);
                invokeMethods(instance, afterMethods);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new InvalidTestClassException(e);
            } catch (InvocationTargetException e) {
                Throwable exception = e.getTargetException();
                if (!method.getAnnotation(Test.class).expected().isInstance(exception)) {
                    reports.add(new FailureReport(exception));
                    continue;
                }
            }

            reports.add(new PassReport());
        }

        try {
            invokeMethods(null, afterClassMethods);
        } catch (InvocationTargetException e) {
            throw new AfterClassStageFailedException();
        }  catch (NullPointerException e) {
            throw new NonStaticAfterClassMethodException();
        }

        return reports;
    }

    private @NotNull Object instantiateObject() throws InvalidTestClassException {
        Object instance;

        try {
            instance = testClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidTestClassException(e);
        }

        return instance;
    }

    private void invokeMethods(final @Nullable Object instance, final @NotNull List<Method> methods)
            throws InvocationTargetException, InvalidTestClassException {
        for (Method method : methods) {
            try {
                method.invoke(instance);
            } catch (IllegalAccessException e) {
                throw new InvalidTestClassException(e);
            }
        }
    }
}
