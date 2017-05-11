package ru.spbau.bachelor2015.veselov.pairs.ui;

import com.sun.istack.internal.NotNull;
import javafx.scene.control.Button;
import ru.spbau.bachelor2015.veselov.pairs.Index2;

public class GameCell {
    private final @NotNull Index2 index;

    private final @NotNull Button button;

    public GameCell(final @NotNull Index2 index) {
        this.index = index;

        button = new Button();
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    @NotNull Button getButton() {
        return button;
    }
}
