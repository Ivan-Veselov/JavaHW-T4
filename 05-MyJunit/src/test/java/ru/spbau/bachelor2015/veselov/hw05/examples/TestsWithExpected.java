package ru.spbau.bachelor2015.veselov.hw05.examples;

import ru.spbau.bachelor2015.veselov.hw05.annotations.Test;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.Exception1;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.Exception2;

public class TestsWithExpected {
    @Test(expected = Exception1.class)
    public void test1() throws Exception {
        throw new Exception2();
    }

    @Test(expected = Exception2.class)
    public void test2() throws Exception {
        throw new Exception2();
    }
}
