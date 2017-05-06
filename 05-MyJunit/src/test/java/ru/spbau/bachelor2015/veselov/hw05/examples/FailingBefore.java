package ru.spbau.bachelor2015.veselov.hw05.examples;

import ru.spbau.bachelor2015.veselov.hw05.annotations.Before;
import ru.spbau.bachelor2015.veselov.hw05.annotations.Test;

public class FailingBefore {
    @Before
    public void before() throws Exception {
        throw new Exception();
    }

    @Test
    public void test() {
    }
}
