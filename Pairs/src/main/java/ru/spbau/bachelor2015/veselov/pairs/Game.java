package ru.spbau.bachelor2015.veselov.pairs;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static ru.spbau.bachelor2015.veselov.pairs.Game.ChoiceResult.*;

/**
 * An object of this class represents a game.
 */
public class Game {
    private final int fieldSize;

    private final int[][] field;

    private final boolean[][] isMatched;

    private Index2 chosenCell = null;

    /**
     * Creates a game with a given field size.
     *
     * @param fieldSize a size of a game field. If this argument is odd then IllegalArgumentException will be thrown.
     */
    public Game(final int fieldSize) {
        if (fieldSize % 2 == 1) {
            throw new IllegalArgumentException();
        }

        this.fieldSize = fieldSize;

        field = new int[fieldSize][fieldSize];

        final int numberOfPairs = fieldSize * fieldSize / 2;

        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < numberOfPairs; i++) {
            numbers.add(i);
            numbers.add(i);
        }

        Collections.shuffle(numbers);

        Iterator<Integer> iterator = numbers.iterator();
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                field[i][j] = iterator.next();
            }
        }

        isMatched = new boolean[fieldSize][fieldSize];
    }

    /**
     * Returns game field size.
     */
    public int getFieldSize() {
        return fieldSize;
    }

    /**
     * Makes a game action wich chooses a cell.
     *
     * @param cell an index of cell which is chosen. If index is incorrect then IllegalArgumentException will be thrown.
     * @return a result of choosing.
     */
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

    /**
     * Returns a number of a particular cell.
     *
     * @param index an index if a cell.
     * @return a numbet in cell.
     */
    public int getValue(final @NotNull Index2 index) {
        if (!isValid(index)) {
            throw new IllegalArgumentException();
        }

        return field[index.getX()][index.getY()];
    }

    /**
     * Checks whether or not the game if over.
     */
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

    /**
     * Possible results of choose action.
     */
    public enum ChoiceResult { FIRST_IN_PAIR, MATCHED, NOT_MATCHED }
}
