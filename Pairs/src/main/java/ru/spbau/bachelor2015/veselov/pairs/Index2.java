package ru.spbau.bachelor2015.veselov.pairs;

import org.jetbrains.annotations.Nullable;

/**
 * Objects of this class represents a 2-dimensional index.
 */
public class Index2 {
    private final int x;

    private final int y;

    /**
     * Creates an index.
     *
     * @param x an x coordinate.
     * @param y an y coordinate.
     */
    public Index2(final int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns an x coordinate of this index.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns an y coordinate of this index.
     */
    public int getY() {
        return y;
    }

    /**
     * Compares two index of equality.
     *
     * @param object an object to compare with.
     * @return true only if object is instance of Index2 class and it's x and y coordinates are equal to
     *         x and y coordinates of this index respectively.
     */
    @Override
    public boolean equals(final @Nullable Object object) {
        if (!(object instanceof Index2)) {
            return false;
        }

        Index2 other = (Index2) object;
        return this.x == other.x && this.y == other.y;
    }
}
