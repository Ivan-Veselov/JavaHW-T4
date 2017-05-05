package ru.spbau.bachelor2015.veselov.hw05;

import org.jetbrains.annotations.NotNull;
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

    public Tester(final @NotNull Class<?> testClass) {
        this.testClass = testClass;

        testMethods = new ArrayList<>();
        for (Method method : testClass.getMethods()) {
            Test testAnnotation = method.getAnnotation(Test.class);
            if (testAnnotation != null) {
                testMethods.add(method);
            }
        }
    }

    public @NotNull List<TestReport> test() throws InvalidTestClassException {
        Object instance;
        List<TestReport> reports = new ArrayList<>();

        try {
            instance = testClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidTestClassException(e);
        }

        for (Method method : testMethods) {
            try {
                method.invoke(instance);
            } catch (IllegalAccessException e) {
                throw new InvalidTestClassException(e);
            } catch (InvocationTargetException e) {
                reports.add(new FailureReport(e.getTargetException()));
                continue;
            }

            reports.add(new PassReport());
        }

        return reports;
    }
}
