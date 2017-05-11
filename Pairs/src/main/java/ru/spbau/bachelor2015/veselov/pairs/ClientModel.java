package ru.spbau.bachelor2015.veselov.pairs;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.pairs.ui.GameCell;

import java.util.ArrayList;
import java.util.List;

/**
 * A model of a graphic client.
 * TODO: possibly need to merge this class with Client.
 */
public class ClientModel {
    private final static int FIELD_SIZE = 4;

    private final @NotNull Stage primaryStage;

    private final @NotNull Game game;

    private @Nullable GameCell chosenCell;

    private final @NotNull List<GameCell> cellsToActivate = new ArrayList<>();

    /**
     * Creates a model.
     *
     * @param primaryStage a primary stage of an application.
     */
    public ClientModel(final @NotNull Stage primaryStage) {
        this.primaryStage = primaryStage;

        game = new Game(FIELD_SIZE);
    }

    /**
     * Returns a size of a field.
     */
    public int getFieldSize() {
        return FIELD_SIZE;
    }

    /**
     * Performs a click action on a given cell.
     *
     * @param cell a game cell which was clicked.
     */
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

        if (!game.isOver()) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("You have won!");
        alert.setHeaderText(null);
        alert.setContentText("Congratulations!");
        alert.showAndWait();

        primaryStage.close();
    }

    /**
     * Returns a content of a particular cell.
     *
     * @param index an index of a cell.
     * @return a string representation of cell content.
     */
    public @NotNull String getContent(final @NotNull Index2 index) {
        return Integer.toString(game.getValue(index));
    }
}
