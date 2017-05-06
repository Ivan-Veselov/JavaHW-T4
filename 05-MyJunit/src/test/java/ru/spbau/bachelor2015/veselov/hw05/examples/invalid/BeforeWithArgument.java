package ru.spbau.bachelor2015.veselov.hw05.examples.invalid;

import ru.spbau.bachelor2015.veselov.hw05.annotations.Before;
import ru.spbau.bachelor2015.veselov.hw05.annotations.Test;

public class BeforeWithArgument {
    @Before
    public void before(int argument) {}

    @Test
    public void test() {}
}
