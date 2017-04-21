package ru.spbau.bachelor2015.veselov.hw04;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.spbau.bachelor2015.veselov.hw04.messages.Message;
import ru.spbau.bachelor2015.veselov.hw04.messages.MessageWriter;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageWriterTest {
    @Test
    public void testSingleWriting() throws Exception {
        final byte[] data = new byte[] {1, 2, 3};

        WritableByteChannel channel = mock(WritableByteChannel.class);
        when(channel.write(any())).thenAnswer(read(data.length + Message.LENGTH_BYTES));

        MessageWriter writer = new MessageWriter(channel, data);

        assertThat(writer.write(), is(true));
    }

    @Test
    public void testMultipleWritings() throws Exception {
        final byte[] data = new byte[] {1, 2, 3};

        WritableByteChannel channel = mock(WritableByteChannel.class);
        when(channel.write(any())).thenAnswer(read(intArrayOfOnes(data.length + Message.LENGTH_BYTES)));

        MessageWriter writer = new MessageWriter(channel, data);

        while (!writer.write());
    }

    private @NotNull int[] intArrayOfOnes(final int size) {
        int[] array = new int[size];
        Arrays.fill(array, 1);

        return array;
    }

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