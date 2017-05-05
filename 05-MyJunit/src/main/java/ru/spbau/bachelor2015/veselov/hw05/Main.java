package ru.spbau.bachelor2015.veselov.hw05;

import com.sun.istack.internal.NotNull;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.InvalidTestClassException;
import ru.spbau.bachelor2015.veselov.hw05.reports.TestReport;

public class Main {
    public static void main(final @NotNull String[] args) throws InvalidTestClassException {
        if (args.length != 1) {
            System.out.println("Argument expected");
            return;
        }

        Class<?> testClass;
        try {
            testClass = Class.forName(args[0]);
        } catch (ClassNotFoundException e) {
            System.out.println("Class name expected");
            return;
        }

        for (TestReport report : new Tester(testClass).test()) {
            System.out.println(report);
        }
    }
}