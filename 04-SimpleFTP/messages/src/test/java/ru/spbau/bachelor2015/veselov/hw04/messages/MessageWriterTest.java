package ru.spbau.bachelor2015.veselov.hw04.messages;

import org.jetbrains.annotations.NotNull;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MessageWriterTest {
    private @NotNull Reader read(final @NotNull int... bytesToRead) {
        return new Reader(bytesToRead);
    }

    private static class Reader implements Answer<Integer> {
        private final @NotNull List<Integer> bytesToReadList;

        public Reader(final @NotNull int[] bytesToRead) {
            this.bytesToReadList = Arrays.stream(bytesToRead)
                                         .mapToObj(n -> n)
                                         .collect(Collectors.toList());
        }

        @Override
        public @NotNull Integer answer(final @NotNull InvocationOnMock invocation) throws Throwable {
            ByteBuffer buffer = invocation.getArgument(0);

            if (bytesToReadList.isEmpty()) {
                return 0;
            }

            int currentBytesToRead = bytesToReadList.get(0);
            if (currentBytesToRead == 0) {
                if (buffer.hasRemaining()) {
                    return 0;
                }

                bytesToReadList.remove(0);
                return 0;
            }

            int bytesToRead = Math.min(currentBytesToRead, buffer.remaining());

            if (currentBytesToRead == bytesToRead) {
                bytesToReadList.remove(0);
            }

            for (int i = 0; i < bytesToRead; i++) {
                buffer.get();
            }

            return bytesToRead;
        }
    }
}