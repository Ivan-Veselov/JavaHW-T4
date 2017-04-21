package ru.spbau.bachelor2015.veselov.hw04;

import org.apache.commons.lang3.SerializationUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.bachelor2015.veselov.hw04.ftpmessages.FTPListAnswerMessage;
import ru.spbau.bachelor2015.veselov.hw04.ftpmessages.FTPListMessage;
import ru.spbau.bachelor2015.veselov.hw04.ftpmessages.FTPMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.Message;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.lang3.ArrayUtils.addAll;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static ru.spbau.bachelor2015.veselov.hw04.TestUtils.toLengthByteArray;

public class ServerTest {
    @Rule
    public TemporaryFolder serverFolder = new TemporaryFolder();

    @Test
    public void testFTPListMessage() throws Exception {
        Path pathToRoot = serverFolder.getRoot().toPath();

        final String fileName = "file";
        Path pathToFile = serverFolder.newFile(fileName).toPath();

        final int port = 10000;
        Server server = new Server(pathToRoot, port);
        server.start();

        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket((String) null, port);
            } catch (IOException e) {
            }
        }

        FTPListMessage message = new FTPListMessage(Paths.get("").toString());

        socket.getOutputStream().write(prepareMessage(message));

        FTPListAnswerMessage answer = SerializationUtils.deserialize(readMessage(socket.getInputStream()));

        assertThat(answer.getContent(), is(contains(fileEntry(pathToRoot.relativize(pathToFile),
                                                             false))));
    }

    private @NotNull byte[] prepareMessage(final @NotNull FTPMessage message) {
        byte[] content = SerializationUtils.serialize(message);

        return addAll(toLengthByteArray(content.length), content);
    }

    private @NotNull byte[] readMessage(final @NotNull InputStream inputStream) throws IOException {
        byte[] rawLength = new byte[Message.LENGTH_BYTES];
        if (inputStream.read(rawLength) != rawLength.length) {
            throw new RuntimeException();
        }

        ByteBuffer buffer = ByteBuffer.allocate(rawLength.length);
        buffer.put(rawLength).flip();
        int length = buffer.getInt();

        byte[] message = new byte[length];
        if (inputStream.read(message) != message.length) {
            throw new RuntimeException();
        }

        return message;
    }

    private @NotNull FileEntryMatcher fileEntry(final @NotNull Path path,
                                                final boolean isDirectory) {
        return new FileEntryMatcher(path, isDirectory);
    }

    private static class FileEntryMatcher extends BaseMatcher<FTPListAnswerMessage.Entry> {
        private final @NotNull Path path;

        private final boolean isDirectory;

        public FileEntryMatcher(final @NotNull Path path, final boolean isDirectory) {
            this.path = path;
            this.isDirectory = isDirectory;
        }

        @Override
        public boolean matches(final @NotNull Object item) {
            if (!(item instanceof FTPListAnswerMessage.Entry)) {
                return false;
            }

            FTPListAnswerMessage.Entry entry = (FTPListAnswerMessage.Entry) item;

            return Paths.get(entry.getPath()).equals(path) && entry.isDirectory() == isDirectory;
        }

        @Override
        public void describeTo(final @NotNull Description description) {
            description.appendText("expected path: ").appendValue(path)
                       .appendText(", expected isDirectory: ").appendValue(isDirectory);
        }
    }
}