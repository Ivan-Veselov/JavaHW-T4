package ru.spbau.bachelor2015.veselov.hw04;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.bachelor2015.veselov.hw04.client.Client;
import ru.spbau.bachelor2015.veselov.hw04.client.exceptions.ConnectionWasClosedException;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FileEntry;
import ru.spbau.bachelor2015.veselov.hw04.server.Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ServerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final int foldersInRootFolder = 10;

    private Path[] relativePathToFolderInRoot;

    private Path[] relativePathToFilesInRootSubFolders;

    private Path pathToTrackedFolder;

    private final int port = 10000;

    private Server server;

    private Client client;

    private void folderInitialization() throws Exception {
        String trackedFolderName = "tracked";
        String folderNamePrefix = "dir";
        String fileNamePrefix = "file";

        pathToTrackedFolder = temporaryFolder.newFolder(trackedFolderName).toPath();

        relativePathToFolderInRoot = new Path[foldersInRootFolder];
        relativePathToFilesInRootSubFolders = new Path[foldersInRootFolder];

        for (int i = 0; i < foldersInRootFolder; i++) {
            Path pathToFolder = Files.createDirectory(pathToTrackedFolder.resolve(folderNamePrefix + i));
            Path pathToFile = Files.createFile(pathToFolder.resolve(fileNamePrefix + i));
            Files.write(pathToFile, new byte[] { (byte) i });

            relativePathToFolderInRoot[i] = pathToTrackedFolder.relativize(pathToFolder);
            relativePathToFilesInRootSubFolders[i] = pathToTrackedFolder.relativize(pathToFile);
        }
    }

    private void serverInitialization() {
        server = new Server(pathToTrackedFolder, port);
        server.start();
    }

    private void clientInitialization() {
        client = null;

        while (client == null) {
            try {
                client = new Client("localhost", port);
            } catch (IOException ignore) {
            }
        }
    }

    @Before
    public void before() throws Exception {
        folderInitialization();
        serverInitialization();
        clientInitialization();
    }

    @After
    public void after() throws IOException, InterruptedException {
        client.close();
        server.stop();
    }

    @Test(timeout = 1000)
    public void testFTPListMessage() throws Exception {
        new SubDirTestMessage(0).test(client);
    }

    @Test(timeout = 2000)
    public void testFTPListMessages() throws Exception {
        for (int i = 0; i < foldersInRootFolder; i++) {
            new SubDirTestMessage(i).test(client);
        }
    }

    @Test(timeout = 1000)
    public void testListRequestOfRootFolder() throws Exception {
        List<FileEntry> entries = this.client.list(pathToTrackedFolder.relativize(pathToTrackedFolder));

        Matcher<FileEntry>[] matchers = new Matcher[foldersInRootFolder];
        for (int i = 0; i < foldersInRootFolder; i++) {
            matchers[i] = fileEntry(relativePathToFolderInRoot[i], true);
        }

        assertThat(entries, containsInAnyOrder(matchers));
    }

    @Test(expected = ConnectionWasClosedException.class, timeout = 1000)
    public void testListRequestOfForbiddenFolder() throws Exception {
        client.list(pathToTrackedFolder.relativize(temporaryFolder.getRoot().toPath()));
    }

    @Test(timeout = 1000)
    public void testGetRequestOnSmallFile() throws Exception {
        testGetOnFile(relativePathToFilesInRootSubFolders[0]);
    }

    @Test(timeout = 4000)
    public void testGetRequestOnBigFile() throws Exception {
        final String fileName = "big-file";

        Path pathToFile = pathToTrackedFolder.resolve(fileName);

        Files.createFile(pathToFile);

        byte[] data = new byte[1024 * 1024 * 10]; // 10 Mb
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }

        Files.write(pathToFile, data);

        testGetOnFile(pathToTrackedFolder.relativize(pathToFile));
    }

    @Test(expected = ConnectionWasClosedException.class, timeout = 1000)
    public void serverDisconnects() throws Exception {
        server.stop();

        client.list(pathToTrackedFolder.relativize(pathToTrackedFolder));
    }

    private void testGetOnFile(final @NotNull Path pathToSource) throws Exception {
        final Path pathToDestination = temporaryFolder.newFile().toPath();

        client.get(pathToSource, pathToDestination);

        assertThat(Files.readAllBytes(pathToTrackedFolder.resolve(pathToSource)),
                is(equalTo(Files.readAllBytes(pathToDestination))));
    }

    private @NotNull FileEntryMatcher fileEntry(final @NotNull Path path,
                                                final boolean isDirectory) {
        return new FileEntryMatcher(path, isDirectory);
    }

    private class SubDirTestMessage {
        private final int subDirIndex;

        public SubDirTestMessage(final int index) {
            subDirIndex = index;
        }

        public void test(final @NotNull Client client) throws Exception {
            List<FileEntry> answer =
                client.list(relativePathToFolderInRoot[subDirIndex]);

            assertThat(answer, is(contains(
                fileEntry(relativePathToFilesInRootSubFolders[subDirIndex], false))));
        }
    }

    private static class FileEntryMatcher extends BaseMatcher<FileEntry> {
        private final @NotNull Path path;

        private final boolean isDirectory;

        public FileEntryMatcher(final @NotNull Path path, final boolean isDirectory) {
            this.path = path;
            this.isDirectory = isDirectory;
        }

        @Override
        public boolean matches(final @NotNull Object item) {
            if (!(item instanceof FileEntry)) {
                return false;
            }

            FileEntry entry = (FileEntry) item;

            return entry.getPath().equals(path) && entry.isDirectory() == isDirectory;
        }

        @Override
        public void describeTo(final @NotNull Description description) {
            description.appendText("expected path: ").appendValue(path)
                       .appendText(", expected isDirectory: ").appendValue(isDirectory);
        }
    }
}