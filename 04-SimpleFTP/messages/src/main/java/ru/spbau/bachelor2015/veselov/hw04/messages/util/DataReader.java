package ru.spbau.bachelor2015.veselov.hw04.messages.util;

import java.io.IOException;

public interface DataReader {
    ReadingResult read() throws IOException;

    enum ReadingResult {
        READ, NOT_READ, CLOSED
    }
}
