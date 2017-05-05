package ru.spbau.bachelor2015.veselov.hw05.examples.invalid;

import ru.spbau.bachelor2015.veselov.hw05.annotations.After;
import ru.spbau.bachelor2015.veselov.hw05.annotations.Test;

public class AfterWithArgumentClass {
    @Test
    public void test() {}

    @After
    public void after(int argument) {}
}
