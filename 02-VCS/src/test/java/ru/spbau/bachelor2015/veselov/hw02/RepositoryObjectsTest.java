package ru.spbau.bachelor2015.veselov.hw02;

import org.apache.commons.io.FileUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.bachelor2015.veselov.hw02.exceptions.FileFromWorkingDirectoryExpected;
import ru.spbau.bachelor2015.veselov.hw02.exceptions.NamesContainsDuplicates;
import ru.spbau.bachelor2015.veselov.hw02.exceptions.NoSuchElement;
import ru.spbau.bachelor2015.veselov.hw02.exceptions.RegularFileExpected;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.*;

/**
 * TODO: reconsider tests one more time
 * TODO: split this class
 */
public class RepositoryObjectsTest {
    @Rule
    public final @NotNull TemporaryFolder rootDirectory = new TemporaryFolder();

    // TODO: Maybe add some kind of a special rule
    private Repository repository;

    @Before
    public void initializeEnvironment() throws Exception {
        repository = Repository.initializeVCS(rootDirectory.getRoot().toPath());
    }

    @Test
    public void blobConstruction() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();
        Files.write(pathToFile, new byte[] {1, 2, 3});

        Repository.Blob blob = repository.new Blob(pathToFile);

        // TODO: Find or write matcher
        assertThat(FileUtils.contentEquals(pathToFile.toFile(), blob.getPathInStorage().getPath().toFile()), is(true));
    }

    @Test(expected = RegularFileExpected.class)
    public void blobConstructionFromFolder() throws Exception {
        File folder = rootDirectory.newFolder();

        repository.new Blob(folder.toPath());
    }

    @Test
    public void multipleBlobsForSingleFile() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();
        Files.write(pathToFile, new byte[] {1, 2, 3});

        Repository.Blob blob1 = repository.new Blob(pathToFile);
        Repository.Blob blob2 = repository.new Blob(pathToFile);

        assertThat(blob2, is(similarTo(blob1)));
        // TODO: Find or write matcher
        assertThat(FileUtils.contentEquals(pathToFile.toFile(), blob2.getPathInStorage().getPath().toFile()), is(true));
    }

    @Test
    public void blobConstructionFromHash() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();
        Repository.Blob blob1 = repository.new Blob(pathToFile);

        Repository.Blob blob2 = repository.new Blob(blob1.getVCSHash());

        assertThat(blob2, is(similarTo(blob1)));
    }

    @Test(expected = NoSuchElement.class)
    public void blobConstructionFromNonExistingHash() throws Exception {
        repository.new Blob(mockedHash("hash"));
    }

    @Test(expected = FileFromWorkingDirectoryExpected.class)
    public void blobConstructionFromVCSFile() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();
        Repository.Blob blob = repository.new Blob(pathToFile);

        repository.new Blob(blob.getPathInStorage().getPath());
    }

    @Test
    public void treeConstruction() throws Exception {
        repository.new Tree(Collections.emptyList(),
                            Collections.singletonList(mockedNamed(mockedBlob("hash"), "name")));
    }

    @Test
    public void multipleTreesForSingleFolder() throws Exception {
        repository.new Tree(Collections.emptyList(), Collections.emptyList());
        repository.new Tree(Collections.emptyList(), Collections.emptyList());
    }

    @Test(expected = NamesContainsDuplicates.class)
    public void testTreeConstructionWithNameDuplicates() throws Exception {
        final String name = "name";

        repository.new Tree(Collections.emptyList(),
                Arrays.asList(mockedNamed(mockedBlob("0"), name), mockedNamed(mockedBlob("1"), name)));
    }

    @Test
    public void treeConstructionFromHash() throws Exception {
        Repository.Tree tree1 = repository.new Tree(Collections.emptyList(), Collections.emptyList());
        Repository.Tree tree2 = repository.new Tree(tree1.getVCSHash());

        assertThat(tree2, is(similarTo(tree1)));
    }

    @Test(expected = NoSuchElement.class)
    public void treeConstructionFromNonExistingHash() throws Exception {
        repository.new Tree(mockedHash("hash"));
    }

    @Test
    public void treeTreeChildrenIterable() throws Exception {
        final String treeName = "name";

        Repository.Tree tree1 = repository.new Tree(Collections.emptyList(), Collections.emptyList());
        Repository.Tree tree2 = repository.new Tree(Collections.singletonList(mockedNamed(tree1, treeName)),
                                                               Collections.emptyList());

        assertThat(tree2.treeChildren(), contains(allOf(name(is(equalTo(treeName))),
                                                        underlyingObject(is(similarTo(tree1))))));
    }

    @Test
    public void treeBlobChildrenIterable() throws Exception {
        final String blobName = "name";
        Path pathToFile = rootDirectory.newFile().toPath();
        Files.write(pathToFile, new byte[] {1, 2, 3});
        Repository.Blob blob = repository.new Blob(pathToFile);

        Repository.Tree tree = repository.new Tree(Collections.emptyList(),
                                                              Collections.singletonList(mockedNamed(blob, blobName)));

        assertThat(tree.blobChildren(), contains(allOf(name(is(equalTo(blobName))),
                                                 underlyingObject(is(similarTo(blob))))));
    }

    @Test
    public void commitConstruction() throws Exception {
        repository.new Commit("message", Collections.emptyList(), mockedTree("hash"));
    }

    @Test
    public void commitConstructionFromHash() throws Exception {
        Repository.Commit commit1 = repository.new Commit("message",
                                                           Collections.emptyList(),
                                                           mockedTree("hash"));

        Repository.Commit commit2 = repository.new Commit(commit1.getVCSHash());

        assertThat(commit2, is(similarTo(commit1)));
    }

    @Test
    public void commitGetTree() throws Exception {
        Repository.Tree tree = repository.new Tree(Collections.emptyList(), Collections.emptyList());
        Repository.Commit commit = repository.new Commit("message",
                                                          Collections.emptyList(),
                                                          tree);

        assertThat(commit.getTree(), is(similarTo(tree)));
    }

    @Test
    public void commitParentCommits() throws Exception {
        Repository.Commit parentCommit = repository.new Commit("message1",
                                                                Collections.emptyList(),
                                                                mockedTree("hash1"));

        Repository.Commit commit = repository.new Commit("message2",
                                                          Collections.singletonList(parentCommit),
                                                          mockedTree("hash2"));

        assertThat(commit.parentCommits(), contains(similarTo(parentCommit)));
    }

    @Test
    public void referenceConstruction() throws Exception {
        repository.createReference("reference", mockedCommit("hash"));
    }

    @Test
    public void referenceGetCommit() throws Exception {
        Repository.Commit commit = repository.new Commit("message",
                                                          Collections.emptyList(),
                                                          mockedTree("hash"));

        final String referenceName = "name";
        repository.createReference(referenceName, commit);

        assertThat(repository.getCommitByReference(referenceName), is(similarTo(commit)));
    }

    @Test
    public void updateFileState() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();
        repository.updateFileStateInIndex(pathToFile);
    }

    @Test
    public void addNewCommitFromIndex() throws Exception {
        final String file1Name = "file1";
        final String folderName = "folder";
        final String file2Name = "file2";

        Path pathToFile1 = rootDirectory.newFile(file1Name).toPath();
        Path pathToFolder = rootDirectory.newFolder(folderName).toPath();
        Path pathToFile2 = pathToFolder.resolve(file2Name);
        Files.createFile(pathToFile2);

        repository.updateFileStateInIndex(pathToFile1);
        repository.updateFileStateInIndex(pathToFile2);

        final String message = "message";
        Repository.Commit commit = repository.newCommitFromIndex(message);

        assertThat(commit.getMessage(), is(equalTo(message)));

        Repository.Tree outerTree = commit.getTree();
        assertThat(outerTree,
            allOf(
                blobChildren(
                    contains(
                        allOf(
                            name(is(equalTo(file1Name))),
                            underlyingObject(is(similarTo(repository.new Blob(pathToFile1))))
                        )
                    )
                ),
                treeChildren(
                    contains(
                        allOf(
                            name(is(equalTo(folderName))),
                            underlyingObject(
                                allOf(
                                    blobChildren(
                                        contains(
                                            allOf(
                                                name(is(equalTo(file2Name))),
                                                underlyingObject(is(similarTo(repository.new Blob(pathToFile2))))
                                            )
                                        )
                                    ),
                                    treeChildren(emptyIterable())
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    @Test
    public void restoreStateTest() throws Exception {
        final String file1Name = "name1";
        final String file2Name = "name2";

        File file1 = rootDirectory.newFile(file1Name);
        Path path1 = file1.toPath();

        byte[] content1 = new byte[] {1};
        Files.write(path1, content1);
        repository.updateFileStateInIndex(path1);
        Repository.Commit commit1 = repository.newCommitFromIndex("message1");

        Files.write(path1, new byte[] {2});
        repository.updateFileStateInIndex(path1);

        File file2 = rootDirectory.newFile(file2Name);
        Path path2 = file2.toPath();
        repository.updateFileStateInIndex(path2);

        repository.newCommitFromIndex("message2");

        repository.restoreState(commit1);

        // TODO: matchers
        assertThat(Files.exists(path1), is(true));
        assertThat(Files.exists(path2), is(false));

        assertThat(Files.readAllBytes(path1), is(equalTo(content1)));
    }

    @Test
    public void testCurrentCommitHistory() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();

        repository.updateFileStateInIndex(pathToFile);
        Repository.Commit commit = repository.newCommitFromIndex("message");

        assertThat(repository.getHistoryForCurrentCommit(), contains(anything(), equalTo(commit)));
    }

    @Test
    public void testRemoveUntrackedFiles() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();

        repository.removeUntrackedFiles();

        assertThat(Files.exists(pathToFile), is(false));
    }

    @Test
    public void testMerge() throws Exception {
        final String file1Name = "file1";
        final String file2Name = "file2";
        final String branchName = "branch";

        repository.createReference(branchName, repository.getCurrentCommit());
        repository.restoreState(branchName);
        repository.updateFileStateInIndex(rootDirectory.newFile(file1Name).toPath());
        Repository.Commit commitInBranch = repository.newCommitFromIndex("message");

        repository.restoreState("master");
        repository.updateFileStateInIndex(rootDirectory.newFile(file2Name).toPath());
        repository.newCommitFromIndex("message");

        repository.mergeCommitWithCurrent(commitInBranch);
    }

    @Test
    public void testReferenceDeletion() throws Exception {
        final String referenceName = "reference";

        repository.createReference(referenceName, repository.getCurrentCommit());
        repository.deleteReference(referenceName);
    }

    @Test
    public void testReset() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();

        repository.updateFileStateInIndex(pathToFile);
        repository.resetFileState(pathToFile);
    }

    private @NotNull SHA1Hash mockedHash(final @NotNull String hashHex) {
        SHA1Hash hash = mock(SHA1Hash.class, withSettings().serializable());
        when(hash.getHex()).thenReturn(hashHex);
        return hash;
    }

    private @NotNull Repository.Blob mockedBlob(final @NotNull String hashHex) {
        SHA1Hash hash = mockedHash(hashHex);

        Repository.Blob blob = mock(Repository.Blob.class);
        when(blob.getVCSHash()).thenReturn(hash);

        return blob;
    }

    private @NotNull Repository.Tree mockedTree(final @NotNull String hashHex) {
        SHA1Hash hash = mockedHash(hashHex);

        Repository.Tree tree = mock(Repository.Tree.class);
        when(tree.getVCSHash()).thenReturn(hash);

        return tree;
    }

    private @NotNull Repository.Commit mockedCommit(final @NotNull String hashHex) {
        SHA1Hash hash = mockedHash(hashHex);

        Repository.Commit commit = mock(Repository.Commit.class);
        when(commit.getVCSHash()).thenReturn(hash);

        return commit;
    }

    private <T> @NotNull Named<T> mockedNamed(final @NotNull T object, final @NotNull String name) {
        Named<T> named = mock(Named.class);
        when(named.getName()).thenReturn(name);
        when(named.getObject()).thenReturn(object);

        return named;
    }

    /* Hamcrest matchers */

    private Matcher<Repository.StoredObject> similarTo(Repository.StoredObject expected) {
        return new StoredObjectMatcher<>(expected);
    }

    private Matcher<Repository.Commit> similarTo(Repository.Commit expected) {
        return new StoredObjectMatcher<Repository.Commit>(expected) {
            @Override
            public boolean matches(Object o) {
                if (!(o instanceof Repository.Commit)) {
                    return false;
                }

                Repository.Commit actual = (Repository.Commit) o;

                return super.matches(o) &&
                       actual.getAuthor().equals(expected.getAuthor()) &&
                       actual.getMessage().equals(expected.getMessage()) &&
                       actual.getDate().equals(expected.getDate());
            }

            @Override
            public void describeTo(Description description) {
                super.describeTo(description);

                description.appendText(", expected author: ").appendValue(expected.getAuthor())
                           .appendText(", expected message:" ).appendValue(expected.getMessage())
                           .appendText(", expected date: ").appendValue(expected.getDate());
            }
        };
    }

    private FeatureMatcher<Named, String> name(final @NotNull Matcher<? super String> subMatcher) {
        return new FeatureMatcher<Named, String>(subMatcher,
                                                "name field of Named class",
                                                "name") {
            @Override
            protected String featureValueOf(final @NotNull Named actual) {
                return actual.getName();
            }
        };
    }

    private <T> FeatureMatcher<Named<T>, T> underlyingObject(final @NotNull Matcher<? super T> subMatcher) {
        return new FeatureMatcher<Named<T>, T>(subMatcher,
                                              "object field of Named class",
                                              "underlying object") {
            @Override
            protected T featureValueOf(final @NotNull Named<T> actual) {
                return actual.getObject();
            }
        };
    }

    private FeatureMatcher<Repository.Tree, Iterable<Named<Repository.Blob>>> blobChildren(
            final @NotNull Matcher<? super Iterable<Named<Repository.Blob>>> subMatcher) {
        return new FeatureMatcher<Repository.Tree, Iterable<Named<Repository.Blob>>>(
                                                            subMatcher,
                                                           "named blob children of Tree",
                                                           "blobChildren") {
            @Override
            protected Iterable<Named<Repository.Blob>> featureValueOf(final @NotNull Repository.Tree actual) {
                return actual.blobChildren();
            }
        };
    }

    private FeatureMatcher<Repository.Tree, Iterable<Named<Repository.Tree>>> treeChildren(
            final @NotNull Matcher<? super Iterable<Named<Repository.Tree>>> subMatcher) {
        return new FeatureMatcher<Repository.Tree, Iterable<Named<Repository.Tree>>>(
                                                                        subMatcher,
                                                                       "named tree children of Tree",
                                                                       "treeChildren") {
            @Override
            protected Iterable<Named<Repository.Tree>> featureValueOf(final @NotNull Repository.Tree actual) {
                return actual.treeChildren();
            }
        };
    }

    private class StoredObjectMatcher<T extends Repository.StoredObject> extends BaseMatcher<T> {
        private final @NotNull Repository.StoredObject expected;

        public StoredObjectMatcher(final @NotNull Repository.StoredObject expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object o) {
            if (!(o instanceof Repository.StoredObject)) {
                return false;
            }

            Repository.StoredObject actual = (Repository.StoredObject) o;

            return actual.getVCSHash().equals(expected.getVCSHash()) &&
                    actual.getPathInStorage().equals(expected.getPathInStorage());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("expected VCSHash: ").appendValue(expected.getVCSHash())
                       .appendText(", expected pathInStorage: ").appendValue(expected.getPathInStorage());
        }
    }
}