package ru.spbau.bachelor2015.veselov.hw05;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.hw05.annotations.*;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.*;
import ru.spbau.bachelor2015.veselov.hw05.reports.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
                   AfterClassStageFailedException {
        List<TestReport> reports = new ArrayList<>();

        try {
            invokeMethods(null, beforeClassMethods);
        } catch (InvocationTargetException e) {
            throw new BeforeClassStageFailedException();
        } catch (NullPointerException e) {
            throw new InvalidTestClassException(e);
        }

        for (Method method : testMethods) {
            reports.add(runTestMethod(method));
        }

        try {
            invokeMethods(null, afterClassMethods);
        } catch (InvocationTargetException e) {
            throw new AfterClassStageFailedException();
        }  catch (NullPointerException e) {
            throw new InvalidTestClassException(e);
        }

        return reports;
    }

    private @NotNull TestReport runTestMethod(final @NotNull Method method) throws InvalidTestClassException {
        Test testAnnotation = method.getAnnotation(Test.class);

        String ignoreReason = testAnnotation.ignore();
        if (!ignoreReason.equals(Test.noIgnoranceDescription)) {
            return new IgnoreReport(testClass.getName(), method.getName(), ignoreReason);
        }

        long startTime = System.nanoTime();

        Object instance = instantiateObject();

        long estimatedTime;
        Throwable exception = null;
        try {
            invokeMethods(instance, beforeMethods);
            method.invoke(instance);
            invokeMethods(instance, afterMethods);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InvalidTestClassException(e);
        } catch (InvocationTargetException e) {
            exception = e.getTargetException();
        } finally {
            estimatedTime = System.nanoTime() - startTime;
        }

        if (exception != null && !testAnnotation.expected().isInstance(exception)) {
            return new UnexpectedExceptionFailureReport(testClass.getName(),
                                                        method.getName(),
                                                        exception,
                                                        estimatedTime);
        }

        if (exception == null && !testAnnotation.expected().equals(Test.None.class)) {
            return new NoExceptionFailureReport(testClass.getName(),
                                                method.getName(),
                                                testAnnotation.expected(),
                                                estimatedTime);
        }

        return new PassReport(testClass.getName(), method.getName(), estimatedTime);
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
