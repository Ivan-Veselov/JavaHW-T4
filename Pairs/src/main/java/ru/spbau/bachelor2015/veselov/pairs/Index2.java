package ru.spbau.bachelor2015.veselov.pairs;

import org.jetbrains.annotations.Nullable;

public class Index2 {
    private final int x;

    private final int y;

    public Index2(final int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(final @Nullable Object object) {
        if (!(object instanceof Index2)) {
            return false;
        }

        Index2 other = (Index2) object;
        return this.x == other.x && this.y == other.y;
    }
}
