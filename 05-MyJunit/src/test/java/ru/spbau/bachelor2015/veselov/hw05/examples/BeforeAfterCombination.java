package ru.spbau.bachelor2015.veselov.hw05.examples;

import ru.spbau.bachelor2015.veselov.hw05.annotations.After;
import ru.spbau.bachelor2015.veselov.hw05.annotations.Before;
import ru.spbau.bachelor2015.veselov.hw05.annotations.Test;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.Exception1;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.Exception2;

public class BeforeAfterCombination {
    private int variable = 0;

    @Before
    public void before() {
        variable++;
    }

    @Test
    public void test1() {
    }

    @Test
    public void test2() {
        variable++;
    }

    @Test
    public void test3() {
        variable += 2;
    }

    @After
    public void after1() throws Exception {
        if (variable == 3) {
            throw new Exception1();
        }
    }

    @After
    public void after2() throws Exception {
        if (variable == 2) {
            throw new Exception2();
        }
    }
}
