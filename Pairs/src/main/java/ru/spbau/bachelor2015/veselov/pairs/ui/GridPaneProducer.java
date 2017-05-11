package ru.spbau.bachelor2015.veselov.pairs.ui;

import com.sun.istack.internal.NotNull;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import ru.spbau.bachelor2015.veselov.pairs.ClientModel;
import ru.spbau.bachelor2015.veselov.pairs.Index2;

public class GridPaneProducer {
    public static @NotNull GridPane produce(final @NotNull ClientModel model) {
        GridPane gridPane = new GridPane();

        for (int i = 0; i < model.getFieldSize(); i++) {
            for (int j = 0; j < model.getFieldSize(); j++) {
                GameCell cell = new GameCell(new Index2(i, j));

                GridPane.setConstraints(cell.getButton(), i, j);
                gridPane.getChildren().add(cell.getButton());
            }
        }

        for (int i = 0; i < model.getFieldSize(); i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setHgrow(Priority.ALWAYS);
            gridPane.getColumnConstraints().add(columnConstraints);

            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.ALWAYS);
            gridPane.getRowConstraints().add(rowConstraints);
        }

        return gridPane;
    }
}
