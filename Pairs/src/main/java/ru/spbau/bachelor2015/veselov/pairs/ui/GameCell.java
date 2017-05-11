package ru.spbau.bachelor2015.veselov.pairs.ui;

import javafx.scene.control.Button;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.pairs.ClientModel;
import ru.spbau.bachelor2015.veselov.pairs.Index2;

public class GameCell {
    private final @NotNull Index2 index;

    private final @NotNull ClientModel model;

    private final @NotNull Button button;

    private final @NotNull String content;

    public GameCell(final @NotNull Index2 index, final @NotNull String content, final @NotNull ClientModel model) {
        this.index = index;
        this.content = content;
        this.model = model;

        button = new Button();
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        button.setOnAction(event -> model.clickCell(GameCell.this));
    }

    public @NotNull Index2 getIndex() {
        return index;
    }

    public void setActive() {
        button.setText("");
        button.setDisable(false);
    }

    public void setInactive() {
        button.setText(content);
        button.setDisable(true);
    }

    public @NotNull Button getButton() {
        return button;
    }
}
