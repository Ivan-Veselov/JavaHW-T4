package ru.spbau.bachelor2015.veselov.hw05.examples;

import ru.spbau.bachelor2015.veselov.hw05.annotations.Test;

public class ClassWithSimpleTests {
    @Test
    public void passingTest1() {
    }

    @Test
    public void passingTest2() {
    }

    @Test
    public void failingTest1() throws Exception {
        throw new Exception();
    }

    @Test
    public void failingTest2() throws Exception {
        throw new Exception();
    }
}
