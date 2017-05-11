package ru.spbau.bachelor2015.veselov.pairs;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;

import static ru.spbau.bachelor2015.veselov.pairs.Game.ChoiceResult.*;

public class Game {
    private final int fieldSize;

    private final int[][] field;

    private final boolean[][] isMatched;

    private Index2 chosenCell = null;

    public Game(final int fieldSize) {
        if (fieldSize % 2 == 1) {
            throw new IllegalArgumentException();
        }

        this.fieldSize = fieldSize;

        field = new int[fieldSize][fieldSize];

        final int numberOfPairs = fieldSize * fieldSize / 2;
        int[] numbers = new int[2 * numberOfPairs];
        int pointer = 0;
        for (int i = 0; i < numberOfPairs; i++) {
            numbers[pointer++] = i;
            numbers[pointer++] = i;
        }

        Collections.shuffle(Arrays.asList(numbers));

        pointer = 0;
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                field[i][j] = numbers[pointer++];
            }
        }

        isMatched = new boolean[fieldSize][fieldSize];
    }

    public int getFieldSize() {
        return fieldSize;
    }

    public @NotNull ChoiceResult choose(final @NotNull Index2 cell) {
        if (!isValid(cell)) {
            throw new IllegalArgumentException();
        }

        if (isMatched[cell.getX()][cell.getY()]) {
            throw new IllegalArgumentException();
        }

        if (chosenCell == null) {
            chosenCell = cell;
            return FIRST_IN_PAIR;
        }

        if (chosenCell.equals(cell)) {
            throw new IllegalArgumentException();
        }

        if (field[cell.getX()][cell.getY()] != field[chosenCell.getX()][chosenCell.getY()]) {
            chosenCell = null;
            return NOT_MATCHED;
        }

        isMatched[chosenCell.getX()][chosenCell.getY()] = true;
        isMatched[cell.getX()][cell.getY()] = true;
        chosenCell = null;

        return MATCHED;
    }

    public int getValue(final @NotNull Index2 index) {
        if (!isValid(index)) {
            throw new IllegalArgumentException();
        }

        return field[index.getX()][index.getY()];
    }

    public boolean isOver() {
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                if (!isMatched[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isValid(final @NotNull Index2 cell) {
        return 0 <= cell.getX() && cell.getX() < fieldSize && 0 <= cell.getY() && cell.getY() < fieldSize;
    }

    public enum ChoiceResult { FIRST_IN_PAIR, MATCHED, NOT_MATCHED }
}
