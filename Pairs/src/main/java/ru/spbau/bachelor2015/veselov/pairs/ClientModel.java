package ru.spbau.bachelor2015.veselov.pairs;

public class ClientModel {
    private final static int FIELD_SIZE = 10;

    private final Game game;

    public ClientModel() {
        game = new Game(FIELD_SIZE);
    }

    public int getFieldSize() {
        return FIELD_SIZE;
    }
}
