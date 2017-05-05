package ru.spbau.bachelor2015.veselov.hw05.examples;

import ru.spbau.bachelor2015.veselov.hw05.annotations.After;
import ru.spbau.bachelor2015.veselov.hw05.annotations.Test;

public class FailingAfterClass {
    @Test
    public void test() {
    }

    @After
    public void after() throws Exception {
        throw new Exception();
    }
}
