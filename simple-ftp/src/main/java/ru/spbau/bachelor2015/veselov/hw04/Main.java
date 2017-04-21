package ru.spbau.bachelor2015.veselov.hw04;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        new Server(Paths.get("")).start();
    }
}
