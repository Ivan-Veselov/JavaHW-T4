package ru.spbau.bachelor2015.veselov.pairs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.pairs.ui.GameCell;

import java.util.ArrayList;
import java.util.List;

public class ClientModel {
    private final static int FIELD_SIZE = 4;

    private final @NotNull Game game;

    private @Nullable GameCell chosenCell;

    private final @NotNull List<GameCell> cellsToActivate = new ArrayList<>();

    public ClientModel() {
        game = new Game(FIELD_SIZE);
    }

    public int getFieldSize() {
        return FIELD_SIZE;
    }

    public void clickCell(final @NotNull GameCell cell) {
        for (GameCell gameCell : cellsToActivate) {
            gameCell.setActive();
        }

        cellsToActivate.clear();

        cell.setInactive();

        switch (game.choose(cell.getIndex())) {
            case FIRST_IN_PAIR:
                chosenCell = cell;
                break;

            case MATCHED:
                chosenCell = null;
                break;

            case NOT_MATCHED:
                cellsToActivate.add(cell);
                cellsToActivate.add(chosenCell);
                chosenCell = null;
                break;
        }
    }

    public @NotNull String getContent(final @NotNull Index2 index) {
        return Integer.toString(game.getValue(index));
    }
}
