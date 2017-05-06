package ru.spbau.bachelor2015.veselov.hw05.examples;

import ru.spbau.bachelor2015.veselov.hw05.annotations.BeforeClass;

public class FailingBeforeClassClass {
    @BeforeClass
    public static void before() throws Exception {
        throw new Exception();
    }
}
