package ru.spbau.bachelor2015.veselov.pairs.ui;

import javafx.scene.control.Button;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.pairs.ClientModel;
import ru.spbau.bachelor2015.veselov.pairs.Index2;

/**
 * Game cell graphic representation.
 */
public class GameCell {
    private final @NotNull Index2 index;

    private final @NotNull ClientModel model;

    private final @NotNull Button button;

    private final @NotNull String content;

    /**
     * Creates a game cell.
     *
     * @param index an index of game cell.
     * @param content a content of game cell.
     * @param model a model of application.
     */
    public GameCell(final @NotNull Index2 index, final @NotNull String content, final @NotNull ClientModel model) {
        this.index = index;
        this.content = content;
        this.model = model;

        button = new Button();
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        button.setOnAction(event -> model.clickCell(GameCell.this));
    }

    /**
     * Returns an index of this game cell.
     */
    public @NotNull Index2 getIndex() {
        return index;
    }

    /**
     * Sets this cell active.
     */
    public void setActive() {
        button.setText("");
        button.setDisable(false);
    }

    /**
     * Sets this cell inactive.
     */
    public void setInactive() {
        button.setText(content);
        button.setDisable(true);
    }

    /**
     * Returns a JavaFX widget for this game cell.
     */
    public @NotNull Button getButton() {
        return button;
    }
}
