package ru.spbau.bachelor2015.veselov.hw05;

import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw05.annotations.After;
import ru.spbau.bachelor2015.veselov.hw05.annotations.Before;
import ru.spbau.bachelor2015.veselov.hw05.annotations.Test;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.InvalidTestClassException;
import ru.spbau.bachelor2015.veselov.hw05.reports.FailureReport;
import ru.spbau.bachelor2015.veselov.hw05.reports.PassReport;
import ru.spbau.bachelor2015.veselov.hw05.reports.TestReport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Tester {
    private final Class<?> testClass;

    private final List<Method> testMethods;

    private final List<Method> beforeMethods;

    private final List<Method> afterMethods;

    public Tester(final @NotNull Class<?> testClass) {
        this.testClass = testClass;

        testMethods = new ArrayList<>();
        beforeMethods = new ArrayList<>();
        afterMethods = new ArrayList<>();

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
        }
    }

    public @NotNull List<TestReport> test() throws InvalidTestClassException {
        List<TestReport> reports = new ArrayList<>();

        for (Method method : testMethods) {
            Object instance = instantiateObject();

            try {
                runMethods(instance, beforeMethods);
                method.invoke(instance);
                runMethods(instance, afterMethods);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new InvalidTestClassException(e);
            } catch (InvocationTargetException e) {
                reports.add(new FailureReport(e.getTargetException()));
                continue;
            }

            reports.add(new PassReport());
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

    private void runMethods(final @NotNull Object instance, final @NotNull List<Method> methods)
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
