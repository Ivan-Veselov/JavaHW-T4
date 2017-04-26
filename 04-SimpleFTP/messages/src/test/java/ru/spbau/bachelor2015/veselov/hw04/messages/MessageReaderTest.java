package ru.spbau.bachelor2015.veselov.hw04.messages;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.spbau.bachelor2015.veselov.hw04.TestUtils;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.MessageNotReadException;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.MessageWithNegativeLengthException;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageReaderTest {
    @Test(expected = MessageWithNegativeLengthException.class)
    public void testNegativeLengthMessage() throws Exception {
        ReadableByteChannel channel = mock(ReadableByteChannel.class);
        when(channel.read(any())).thenAnswer(write(TestUtils.toLengthByteArray(-1)));

        MessageReader reader = new MessageReader(channel);

        reader.read();
    }

    @Test(expected = MessageNotReadException.class)
    public void testGettingOfUnreadMessage() throws Exception {
        ReadableByteChannel channel = mock(ReadableByteChannel.class);

        MessageReader reader = new MessageReader(channel);

        reader.getMessage();
    }

    private @NotNull byte[][] extrude(final @NotNull byte[] array) {
        byte[][] result = new byte[array.length][1];
        for (int i = 0; i < array.length; i++) {
            result[i][0] = array[i];
        }

        return result;
    }

    private @NotNull Writer write(final @NotNull byte[]... dataToWrite) {
        return new Writer(dataToWrite);
    }

    private static class Writer implements Answer<Integer> {
        private final @NotNull List<ByteBuffer> dataToWrite;

        public Writer(final @NotNull byte[][] dataToWrite) {
            this.dataToWrite = Arrays.stream(dataToWrite)
                                     .map(bytes -> (ByteBuffer) ByteBuffer.allocate(bytes.length).put(bytes).flip())
                                     .collect(Collectors.toList());
        }

        @Override
        public @NotNull Integer answer(final @NotNull InvocationOnMock invocation) throws Throwable {
            ByteBuffer buffer = invocation.getArgument(0);

            if (dataToWrite.isEmpty()) {
                return 0;
            }

            ByteBuffer currentDataBuffer = dataToWrite.get(0);
            if (!currentDataBuffer.hasRemaining()) {
                if (buffer.hasRemaining()) {
                    return 0;
                }

                dataToWrite.remove(0);
                return 0;
            }

            int bytesToWrite = Math.min(currentDataBuffer.remaining(), buffer.remaining());

            byte[] data = new byte[bytesToWrite];
            currentDataBuffer.get(data);
            if (!currentDataBuffer.hasRemaining()) {
                dataToWrite.remove(0);
            }

            buffer.put(data);

            return bytesToWrite;
        }
    }
}