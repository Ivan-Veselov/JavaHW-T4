package ru.spbau.bachelor2015.veselov.hw05.examples;

import ru.spbau.bachelor2015.veselov.hw05.annotations.Test;

public class NoExceptionWhileExpected {
    @Test(expected = Exception.class)
    public void test() {
    }
}