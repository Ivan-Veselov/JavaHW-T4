package ru.spbau.bachelor2015.veselov.hw04;

import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.messages.Message;

import java.nio.ByteBuffer;
import java.util.Arrays;

public final class TestUtils {
    public static @NotNull byte[] toLengthByteArray(int integer) {
        return ByteBuffer.allocate(Message.LENGTH_BYTES).putInt(integer).array().clone();
    }

    public static @NotNull int[] intArrayOfOnes(final int size) {
        int[] array = new int[size];
        Arrays.fill(array, 1);

        return array;
    }
}