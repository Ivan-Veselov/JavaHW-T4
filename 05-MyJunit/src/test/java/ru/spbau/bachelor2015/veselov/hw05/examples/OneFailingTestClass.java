package ru.spbau.bachelor2015.veselov.hw05.examples;

import ru.spbau.bachelor2015.veselov.hw05.annotations.Test;

public class OneFailingTestClass {
    @Test
    public void test() throws Exception {
        throw new Exception();
    }
}
