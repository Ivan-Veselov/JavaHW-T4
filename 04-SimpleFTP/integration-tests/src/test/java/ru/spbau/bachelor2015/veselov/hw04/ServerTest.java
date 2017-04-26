package ru.spbau.bachelor2015.veselov.hw04;

import org.apache.commons.lang3.SerializationUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPListAnswerMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.apache.commons.lang3.ArrayUtils.addAll;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class ServerTest {
    @Rule
    public TemporaryFolder serverFolder = new TemporaryFolder();

    private final int directoriesInRootFolder = 10;

    private final String fileNamePrefix = "file";

    private final String directoryNamePrefix = "dir";

    private Path pathToRoot;

    @Before
    public void folderInitialization() throws Exception {
        pathToRoot = serverFolder.getRoot().toPath();

        for (int i = 0; i < directoriesInRootFolder; i++) {
            String stringedIndex = Integer.toString(i);
            Path path = serverFolder.newFolder(directoryNamePrefix + stringedIndex).toPath();
            Files.createFile(path.resolve(fileNamePrefix + stringedIndex));
        }
    }

    @Test
    public void testFTPListMessage() throws Exception {
        processMessages(new MessageTest[] { new SubDirTest(0) });
    }

    @Test
    public void testFTPListMessages() throws Exception {
        MessageTest[] messages = new MessageTest[directoriesInRootFolder];
        for (int i = 0; i < directoriesInRootFolder; i++) {
            messages[i] = new SubDirTest(i);
        }

        processMessages(messages);
    }

    private void processMessages(final @NotNull MessageTest[] messages) throws Exception {
        final int port = 10000;
        Server server = new Server(pathToRoot, port);
        server.start();

        Client client = null;

        while (client == null) {
            try {
                client = new Client("localhost", port);
            } catch (IOException ignore) {
            }
        }

        for (MessageTest testMessage : messages) {
            testMessage.test(client);
        }

        client.close();
        server.stop();
    }

    private @NotNull byte[] prepareMessage(final @NotNull FTPMessage message) {
        byte[] content = SerializationUtils.serialize(message);

        return addAll(toLengthByteArray(content.length), content);
    }

    private @NotNull byte[] readMessage(final @NotNull InputStream inputStream) throws IOException {
        byte[] rawLength = new byte[Integer.BYTES];

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

    public static @NotNull byte[] toLengthByteArray(int integer) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(integer).array().clone();
    }

    private class SubDirTest implements MessageTest {
        private final int subDirIndex;

        public SubDirTest(final int index) {
            subDirIndex = index;
        }

        @Override
        public void test(final @NotNull Client client) throws Exception {
            String stringedIndex = Integer.toString(subDirIndex);
            String directoryName = directoryNamePrefix + stringedIndex;
            String fileName = fileNamePrefix + stringedIndex;

            List<FTPListAnswerMessage.Entry> answer =
                    client.list(Paths.get("").resolve(directoryName).toString());

            assertThat(answer, is(contains(
                                    fileEntry(pathToRoot.relativize(pathToRoot.resolve(directoryName)
                                                                              .resolve(fileName)),
                    false))));
        }
    }

    private interface MessageTest {
        void test(final @NotNull Client client) throws Exception;
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