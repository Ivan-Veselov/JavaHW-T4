package ru.spbau.bachelor2015.veselov.hw05.examples;

import ru.spbau.bachelor2015.veselov.hw05.annotations.AfterClass;

public class FailingAfterClass {
    @AfterClass
    public static void after() throws Exception {
        throw new Exception();
    }
}
